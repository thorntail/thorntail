package org.wildfly.swarm.jaxrs;

import org.objectweb.asm.*;

/**
 * @author Bob McWhirter
 */
public class AnnotationSeekingClassVisitor extends ClassVisitor {

    private boolean found = false;

    public AnnotationSeekingClassVisitor() {
        super(Opcodes.ASM5);
    }

    public boolean isFound() {
        return this.found;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if ( desc.equals( "Ljavax/ws/rs/ApplicationPath;" ) ) {
            found = true;
        }
        return super.visitAnnotation(desc, visible);
    }

}
