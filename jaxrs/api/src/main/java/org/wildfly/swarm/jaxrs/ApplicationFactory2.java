package org.wildfly.swarm.jaxrs;

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

        String slashName = name.replace('.', '/' );

        ClassWriter writer = new ClassWriter(0);
        Remapper remapper = new Remapper() {
            @Override
            public String map(String typeName) {
                if (typeName.equals("org/wildfly/swarm/jaxrs/DefaultApplication")) {
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
