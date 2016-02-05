/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.jaxrs.internal;

import java.io.IOException;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * @author Bob McWhirter
 */
public class ApplicationFactory2 implements Opcodes {

    static byte[] create(String name, String path) throws IOException {
        ClassReader reader = new ClassReader(DefaultApplication.class.getClassLoader().getResourceAsStream(DefaultApplication.class.getName().replace('.', '/') + ".class"));

        String slashName = name.replace('.', '/');

        ClassWriter writer = new ClassWriter(0);
        Remapper remapper = new Remapper() {
            @Override
            public String map(String typeName) {
                if (typeName.equals("org/wildfly/swarm/jaxrs/internal/DefaultApplication")) {
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
