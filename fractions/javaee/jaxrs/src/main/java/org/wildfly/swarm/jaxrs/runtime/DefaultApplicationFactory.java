/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.jaxrs.runtime;

import java.io.IOException;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * @author Bob McWhirter
 */
public class DefaultApplicationFactory implements Opcodes {

    protected DefaultApplicationFactory() {
    }

    static byte[] basicClassBytes() {
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, "org/wildfly/swarm/jaxrs/runtime/DefaultApplication", null, "javax/ws/rs/core/Application", null);

        cw.visitSource("DefaultApplication.java", null);
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(23, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "javax/ws/rs/core/Application", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "Lorg/wildfly/swarm/jaxrs/runtime/DefaultApplication;", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();

    }

    public static byte[] create(String name, String path) throws IOException {
        ClassReader reader = new ClassReader(basicClassBytes());

        String slashName = name.replace('.', '/');

        ClassWriter writer = new ClassWriter(0);
        Remapper remapper = new Remapper() {
            @Override
            public String map(String typeName) {
                if (typeName.equals("org/wildfly/swarm/jaxrs/runtime/DefaultApplication")) {
                    return slashName;
                }
                return super.map(typeName);
            }
        };

        RemappingClassAdapter adapter = new RemappingClassAdapter(writer, remapper);
        reader.accept(adapter, 0);

        AnnotationVisitor ann = writer.visitAnnotation("Ljavax/ws/rs/ApplicationPath;", true);
        ann.visit("value", path);
        ann.visitEnd();
        writer.visitEnd();

        return writer.toByteArray();
    }
}
