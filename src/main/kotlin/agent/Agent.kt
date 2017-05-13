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
    val desired_description = "(Lkotlin/coroutines/experimental/Continuation;)Ljava/lang/Object;"
    val desired_opcode = Opcodes.INVOKESTATIC
    val desired_owner = "example/CoroutineExampleKt"
    val desired_name = "test"


    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
        if (opcode == desired_opcode && desc == desired_description && owner == desired_owner && name == desired_name) {
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
        val class_reader = ClassReader(classfileBuffer)
        // COMPUTE_MAXS needed to recompute maximum stack size semi-automatically.
        // However, `visitMaxs` call is still needed: http://stackoverflow.com/a/28989866/5338270
        val class_writer = ClassWriter(class_reader, COMPUTE_MAXS)
        val class_visitor = object: ClassVisitor(Opcodes.ASM5, class_writer) {
            override fun visitMethod(access: Int, name: String?,
                                     desc: String?, signature: String?,
                                     exceptions: Array<out String>?): MethodVisitor {
                val original_visitor = super.visitMethod(access, name, desc, signature, exceptions)
                return TestCallModifier(Opcodes.ASM5, original_visitor)
            }
        }
        class_reader.accept(class_visitor, 0)
        return class_writer.toByteArray()
    }

}


class Agent {
    companion object {
        @JvmStatic
        fun premain(agentArgs: String?, inst: Instrumentation) {
            // TODO: think about class transformers (load -> use -> transform). Need to add test and see if that works.
            // TODO: Also check that class is not modified if it does not contain required call.
            //      class_writer.toByteArray() always returns non-null - need to optimize. But first, write a benchmark
            // TODO: add test when call is in (static) init section
            inst.addTransformer(TestCallTransformer(), true)
            println("Agent started.")
        }
    }
}
