package monitor.plugin.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

/**
 * Created by ZhouKeWen on 17/3/17.
 */
class AsmHandler {

    public static byte[] handleClass(File file) {
        def optClass = new File(file.getParent(), file.name + ".opt")

        FileInputStream inputStream = new FileInputStream(file);
        FileOutputStream outputStream = new FileOutputStream(optClass)

        def bytes = injectMonitorCode(inputStream);
        outputStream.write(bytes)
        inputStream.close()
        outputStream.close()
        if (file.exists()) {
            file.delete()
        }
        optClass.renameTo(file)
        return bytes
    }

    private static byte[] injectMonitorCode(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream);
        //使用COMPUTE_MAXS，自动计算栈帧大小，略微影响性能
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ActivityClassVisitor(cw)
//        ClassVisitor cv = new ClassVisitor(Opcodes.ASM4, cw) {
//            @Override
//            public MethodVisitor visitMethod(int access, String name, String desc,
//                    String signature, String[] exceptions) {
//
//                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
//                mv = new MethodVisitor(Opcodes.ASM4, mv) {
//                    @Override
//                    void visitInsn(int opcode) {
//                        AsmHandler.printLog("name: ${name}, opcode: ${opcode}")
//                        if ("someM" == name && (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
//                            super.visitLdcInsn(Type.getType("Ljava/lang/String;"));
//                        }
//                        super.visitInsn(opcode);
//                    }
//                }
//                return mv;
//            }
//
//        };
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    private static void printLog(String content) {
        System.out.println(content)
    }

}
