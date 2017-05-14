package agent

import jdk.internal.org.objectweb.asm.*
import java.lang.instrument.*
import java.security.ProtectionDomain

class Agent {

    companion object {
        @JvmStatic
        fun premain(agentArgs: String?, inst: Instrumentation) {
            println("Agent started.")
            inst.addTransformer(MyClassTransformer())
        }
    }
}

class MyClassTransformer : ClassFileTransformer {

    override fun transform(loader: ClassLoader?, className: String?, classBeingRedefined: Class<*>?, protectionDomain: ProtectionDomain?, classfileBuffer: ByteArray?): ByteArray? {
        val cw = ClassWriter(0)
        val cv = MyClassVisitor(Opcodes.ASM5, cw)
        val cr = ClassReader(classfileBuffer)
        cr.accept(cv, 0)
        return cw.toByteArray()
    }
}

class MyClassVisitor(val apii: Int, cv: ClassVisitor) : ClassVisitor(apii, cv) {

    override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<String>?): MethodVisitor {
        val mv = super.visitMethod(access, name, desc, signature, exceptions)
        return MyMethodVisitor(apii, mv)
    }
}

class MyMethodVisitor(api: Int, mv: MethodVisitor) : MethodVisitor(api, mv) {
    val opcode = Opcodes.INVOKESTATIC
    val owner = "example/CoroutineExampleKt"
    val name = "test"
    val desc = "(Lkotlin/coroutines/experimental/Continuation;)Ljava/lang/Object;"

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
        if (opcode == this.opcode && owner == this.owner && name == this.name && desc == this.desc) {
            super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            super.visitLdcInsn("Test detected")
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }
}
