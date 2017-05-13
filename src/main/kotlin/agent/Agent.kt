package agent

import jdk.internal.org.objectweb.asm.*
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain


class Agent : ClassFileTransformer {

    companion object {
        @JvmStatic
        fun premain(agentArgs: String?, inst: Instrumentation) {
            println("Agent started.")
            inst.addTransformer(Agent())
        }
    }

    override fun transform(
            loader: ClassLoader?, className: String?, classBeingRedefined: Class<*>?,
            protectionDomain: ProtectionDomain?, classfileBuffer: ByteArray?
    ): ByteArray? {
        val writer = ClassWriter(0)
        val target = Target(
                "example/CoroutineExampleKt",
                //                "example/AnotherDetectExample\$Companion",
                "test",
                "(Lkotlin/coroutines/experimental/Continuation;)Ljava/lang/Object;"
        )
        val adapter = ClassInstructionInserter(writer, target) {
            it.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            it.visitLdcInsn("Test detected")
            it.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
        }
        val reader = ClassReader(classfileBuffer)
        reader.accept(adapter, 0)
        return writer.toByteArray()
    }


    class ClassInstructionInserter(
            class_visitor: ClassWriter,
            val target: Target,
            val insert: (MethodVisitor) -> Unit  //  type: (MethodWriter) -> Unit
    ) : ClassVisitor(Opcodes.ASM5, class_visitor) {

        override fun visitMethod(
                access: Int, name: String?, desc: String?,
                signature: String?, exceptions: Array<String>?
        ): MethodVisitor {
            val method_visitor = super.visitMethod(access, name, desc, signature, exceptions)
            return MethodInstructionInserter(method_visitor, api, target, insert)
        }
    }

    class MethodInstructionInserter(
            val method_visitor: MethodVisitor, //  type: MethodWriter
            api: Int,
            val target: Target,
            val inserter: (MethodVisitor) -> Unit  //  type: (MethodWriter) -> Unit
    ) : MethodVisitor(api, method_visitor) {

        override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
            if (owner == target.owner && name == target.name && desc == target.desc) {
                inserter(method_visitor)
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf)
        }
    }

    data class Target(val owner: String, val name: String, val desc: String)
}