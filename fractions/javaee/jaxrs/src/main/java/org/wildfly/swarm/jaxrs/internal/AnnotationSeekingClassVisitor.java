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
package org.wildfly.swarm.jaxrs.internal;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Bob McWhirter
 */
public abstract class AnnotationSeekingClassVisitor extends ClassVisitor {

    private final String[] annotations;

    public AnnotationSeekingClassVisitor(String... annotations) {
        super(Opcodes.ASM7);
        this.annotations = annotations;
    }

    public boolean isFound() {
        return this.found;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

        if (matches(desc)) {
            found = true;
        }

        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM7) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (matches(desc)) {
                    found = true;
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }

    private boolean matches(String desc) {
        for (String annotation : this.annotations) {
            if (annotation.endsWith("*")) {
                if (desc.startsWith("L" + annotation.substring(0, annotation.length() - 1))) {
                    return true;
                }
            } else {
                if (desc.equals("L" + annotation + ";")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean found = false;

}
