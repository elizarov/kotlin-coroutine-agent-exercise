package agent

import java.lang.instrument.Instrumentation
import java.lang.instrument.ClassFileTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.security.ProtectionDomain


class TestCallModifier(api: Int, mv: MethodVisitor?) : MethodVisitor(api, mv) {
    val desired_description = "(Lkotlin/coroutines/experimental/Continuation;)Ljava/lang/Object;"
    val desired_opcode = Opcodes.INVOKESTATIC
    val desired_owner = "example/CoroutineExampleKt"
    val desired_name = "test"


    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
        super.visitMethodInsn(opcode, owner, name, desc, itf)
        if (opcode == desired_opcode && desc == desired_description && owner == desired_owner && name == desired_name) {
            println("method call spotted " + name)
            println("\t" + desc)
            println("\t" + owner)
            println("\t" + itf)
        }
    }
}

class TestCallTransformer : ClassFileTransformer {
    override fun transform(loader: ClassLoader?,
                           className: String?,
                           classBeingRedefined: Class<*>?,
                           protectionDomain: ProtectionDomain?,
                           classfileBuffer: ByteArray?): ByteArray {
        val class_reader = ClassReader(classfileBuffer)
        val class_writer = ClassWriter(class_reader, 0)
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
            // TODO: add test when call is in (static) init section
            inst.addTransformer(TestCallTransformer(), true)
            println("Agent started.")
        }
    }
}
