package org.wildfly.swarm.jaxrs;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Bob McWhirter
 */
public class FaviconExceptionMapperFactory implements Opcodes {

    static byte[] create() throws IOException {
        ClassReader reader = new ClassReader(FaviconExceptionMapper.class.getClassLoader().getResourceAsStream(FaviconExceptionMapper.class.getName().replace('.', '/') + ".class"));

        ClassWriter writer = new ClassWriter(0);
        Remapper remapper = new Remapper() {
            @Override
            public String map(String typeName) {
                if (typeName.equals("org/wildfly/swarm/jaxrs/FaviconExceptionMapper")) {
                    return "org/wildfly/swarm/generated/FaviconExceptionMapper";
                }
                return super.map(typeName);
            }
        };

        RemappingClassAdapter adapter = new RemappingClassAdapter(writer, remapper);
        reader.accept(adapter, 0);

        writer.visitAnnotation("Ljavax/ws/rs/ext/Provider;", true).visitEnd();
        writer.visitEnd();

        return writer.toByteArray();
    }
}
