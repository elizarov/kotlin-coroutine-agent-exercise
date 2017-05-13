package agent

import java.lang.instrument.Instrumentation
import java.lang.instrument.ClassFileTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.security.ProtectionDomain


class TestCallModifier(api: Int, mv: MethodVisitor?) : MethodVisitor(api, mv) {
    val desiredDescription = "(Lkotlin/coroutines/experimental/Continuation;)Ljava/lang/Object;"
    val desiredOpcode = Opcodes.INVOKESTATIC
    val desiredOwner = "example/CoroutineExampleKt"
    val desiredName = "test"


    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
        if (opcode == desiredOpcode && desc == desiredDescription && owner == desiredOwner && name == desiredName) {
            // Code taken http://asm.ow2.org/current/asm-transformations.pdf
            super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            super.visitLdcInsn("Test detected")
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V",
                    false)
            super.visitMaxs(-1, -1)
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }
}

class TestCallTransformer : ClassFileTransformer {
    override fun transform(loader: ClassLoader?,
                           className: String?,
                           classBeingRedefined: Class<*>?,
                           protectionDomain: ProtectionDomain?,
                           classfileBuffer: ByteArray?): ByteArray {
        val classReader = ClassReader(classfileBuffer)
        // COMPUTE_MAXS needed to recompute maximum stack size semi-automatically.
        // However, `visitMaxs` call is still needed: http://stackoverflow.com/a/28989866/5338270
        val classWriter = ClassWriter(classReader, COMPUTE_MAXS)
        val classVisitor = object: ClassVisitor(Opcodes.ASM5, classWriter) {
            override fun visitMethod(access: Int, name: String?,
                                     desc: String?, signature: String?,
                                     exceptions: Array<out String>?): MethodVisitor {
                val original_visitor = super.visitMethod(access, name, desc, signature, exceptions)
                return TestCallModifier(Opcodes.ASM5, original_visitor)
            }
        }
        classReader.accept(classVisitor, 0)
        // Benchmark (loading 500+ classes) shows that
        // there is no point in returning "null" (as suggested in `transform` javadoc)
        // explicitly - writer.toByteArray() is fast enough.
        return classWriter.toByteArray()
    }

}


class Agent {
    companion object {
        @JvmStatic
        fun premain(agentArgs: String?, inst: Instrumentation) {
            // Second parameter should be "true" if we want to be able to instrument retransformed classes.
            // Though I do not provide a testcase, it can be done something like that
            // https://sleeplessinslc.blogspot.ru/2008/09/java-instrumentation-with-jdk-16x-class.html
            inst.addTransformer(TestCallTransformer(), true)
            println("Agent started.")
        }
    }
}
