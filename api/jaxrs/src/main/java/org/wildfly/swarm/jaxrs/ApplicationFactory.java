package org.wildfly.swarm.jaxrs;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Bob McWhirter
 */
public class ApplicationFactory implements Opcodes {

    public static byte[] create(String name, String context) {

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER,
                name.replace('.', '/' ),
                null,
                "javax/ws/rs/core/Application", null);

        int lastDot = name.lastIndexOf('.');
        String simpleName = name.substring( lastDot + 1 );
        cw.visitSource( simpleName + ".java", null);

        {
            av0 = cw.visitAnnotation("Ljavax/ws/rs/ApplicationPath;", true);
            av0.visit("value", "/");
            av0.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(10, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "javax/ws/rs/core/Application", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this",
                    "L" + name.replace('.', '/' ) + ";",
                    null,
                    l0,
                    l1,
                    0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
