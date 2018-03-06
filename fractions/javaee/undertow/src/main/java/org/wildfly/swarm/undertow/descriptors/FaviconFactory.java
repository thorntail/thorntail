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
package org.wildfly.swarm.undertow.descriptors;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.wildfly.swarm.undertow.internal.FaviconErrorHandler;
import org.wildfly.swarm.undertow.internal.FaviconServletExtension;

/**
 * @author Ken Finnigan
 */
public final class FaviconFactory {

    private FaviconFactory() {
    }

    static byte[] createFaviconServletExtension(String name) throws IOException {
        ClassReader reader = new ClassReader(FaviconServletExtension.class.getClassLoader().getResourceAsStream(FaviconServletExtension.class.getName().replace('.', '/') + ".class"));

        String slashName = name.replace('.', '/');

        ClassWriter writer = new ClassWriter(0);
        Remapper remapper = new Remapper() {
            @Override
            public String map(String typeName) {
                if (typeName.equals("org/wildfly/swarm/undertow/internal/FaviconServletExtension")) {
                    return slashName;
                }
                return super.map(typeName);
            }
        };

        RemappingClassAdapter adapter = new RemappingClassAdapter(writer, remapper);
        reader.accept(adapter, ClassReader.EXPAND_FRAMES);

        writer.visitEnd();

        return writer.toByteArray();
    }

    static byte[] createFaviconErrorHandler(String name) throws IOException {
        ClassReader reader = new ClassReader(FaviconErrorHandler.class.getClassLoader().getResourceAsStream(FaviconErrorHandler.class.getName().replace('.', '/') + ".class"));

        String slashName = name.replace('.', '/');

        ClassWriter writer = new ClassWriter(0);
        Remapper remapper = new Remapper() {
            @Override
            public String map(String typeName) {
                if (typeName.equals("org/wildfly/swarm/undertow/internal/FaviconErrorHandler")) {
                    return slashName;
                }
                return super.map(typeName);
            }
        };

        RemappingClassAdapter adapter = new RemappingClassAdapter(writer, remapper);
        reader.accept(adapter, ClassReader.EXPAND_FRAMES);

        writer.visitEnd();

        return writer.toByteArray();
    }
}
