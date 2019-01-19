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
package org.wildfly.swarm.fractions;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author Juan Gonzalez
 */
public class ArquillianClientAnnotationSeekingClassVisitor extends ClassVisitor {

    static final String DEPLOYMENT_ANNOTATION = "org/jboss/arquillian/container/test/api/Deployment";
    static final String RUN_AS_CLIENT_ANNOTATION = "org/jboss/arquillian/container/test/api/RunAsClient";

    private int clientCounter;
    private int containerCounter;

    public ArquillianClientAnnotationSeekingClassVisitor() {
        super(Opcodes.ASM7);
    }

    public boolean isClient() {
        return this.clientCounter > 0 && this.containerCounter == 0;
    }

    @Override
    public MethodVisitor visitMethod(final int __,
                                     final String ___,
                                     final String desc,
                                     final String signature,
                                     final String[] exceptions) {

        return new MethodVisitor(Opcodes.ASM7) {
            @Override
            public AnnotationVisitor visitAnnotation(final String desc,
                                                     final boolean __) {

                Type type = Type.getType(desc);
                String className = type.getInternalName();

                if (className.equals(DEPLOYMENT_ANNOTATION)) {
                    containerCounter++;
                    return new ArquillianAnnotationVisitor();
                }

                if (className.equals(RUN_AS_CLIENT_ANNOTATION)) {
                    clientCounter++;
                }

                return null;
            }
       };
    }

    private final class ArquillianAnnotationVisitor extends AnnotationVisitor {

            public ArquillianAnnotationVisitor() {
                super(Opcodes.ASM7);
            }

            @Override
            public void visit(final String name, final Object value) {

                if (name.equals("testable") && value.equals(Boolean.FALSE)) {
                    clientCounter++;
                    containerCounter--;
                }
            }
    };
}
