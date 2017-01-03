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
package org.wildfly.swarm.cdi.jaxrsapi.runtime;

import java.util.List;

import javax.inject.Singleton;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.wildfly.swarm.client.jaxrs.ServiceClient;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.spi.api.ArchiveMetadataProcessor;

/**
 * @author Ken Finnigan
 */
@Singleton
public class ServiceClientProcessor implements ArchiveMetadataProcessor {
    @Override
    public void processArchive(Archive<?> archive, Index index) {
        List<ClassInfo> serviceClients = index.getKnownDirectImplementors((DotName.createSimple(ServiceClient.class.getName())));

        serviceClients.forEach(info -> {
            String name = info.name().toString() + "_generated";
            String path = "WEB-INF/classes/" + name.replace('.', '/') + ".class";
            archive.as(JAXRSArchive.class).add(new ByteArrayAsset(ClientServiceFactory.createImpl(name, info)), path);
        });
    }

    private static class ClientServiceFactory implements Opcodes {
        static byte[] createImpl(String implName, ClassInfo classInfo) {
            ClassWriter cw = new ClassWriter(0);
            MethodVisitor mv;
            AnnotationVisitor av0;

            cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER,
                     implName.replace('.', '/'),
                     null,
                     "java/lang/Object",
                     new String[]{classInfo.name().toString().replace('.', '/')}
            );

            int lastDot = implName.lastIndexOf('.');
            String simpleName = implName.substring(lastDot + 1);

            cw.visitSource(simpleName + ".java", null);
            {
                av0 = cw.visitAnnotation("Ljavax/enterprise/context/ApplicationScoped;", true);
                av0.visitEnd();
            }
            cw.visitInnerClass("javax/ws/rs/client/Invocation$Builder", "javax/ws/rs/client/Invocation", "Builder", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);

            {
                mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                mv.visitCode();
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitLineNumber(14, l0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLineNumber(15, l1);
                mv.visitInsn(RETURN);
                Label l2 = new Label();
                mv.visitLabel(l2);
                mv.visitLocalVariable("this",
                                      buildTypeDef(implName),
                                      null,
                                      l0,
                                      l2,
                                      0);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }

            List<AnnotationInstance> annotations = classInfo.annotations().get(DotName.createSimple("org.wildfly.swarm.client.jaxrs.Service"));
            String baseUrl = (String) annotations.get(0).value("baseUrl").value();
            int lineNum = 18;

            classInfo.asClass().methods()
                    .stream()
                    .forEachOrdered(method -> {
                        createMethod(cw, implName, classInfo.name().toString(), method, lineNum, baseUrl);
                    });
            cw.visitEnd();

            return cw.toByteArray();
        }

        static void createMethod(ClassWriter cw, String implName, String clientInterfaceName, MethodInfo method, int lineNum, String baseUrl) {
            MethodVisitor mv;

            {
                mv = cw.visitMethod(ACC_PUBLIC, method.name(), buildMethodDef(method), null, null);
                mv.visitCode();
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitLineNumber(lineNum++, l0);
                mv.visitLdcInsn(Type.getType(buildTypeDef(clientInterfaceName)));
                mv.visitTypeInsn(NEW, "org/jboss/resteasy/client/jaxrs/ResteasyClientBuilder");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "org/jboss/resteasy/client/jaxrs/ResteasyClientBuilder", "<init>", "()V", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/jboss/resteasy/client/jaxrs/ResteasyClientBuilder", "build", "()Lorg/jboss/resteasy/client/jaxrs/ResteasyClient;", false);
                mv.visitLdcInsn(baseUrl);
                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLineNumber(lineNum++, l1);
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/jboss/resteasy/client/jaxrs/ResteasyClient", "target", "(Ljava/lang/String;)Lorg/jboss/resteasy/client/jaxrs/ResteasyWebTarget;", false);
                Label l2 = new Label();
                mv.visitLabel(l2);
                mv.visitLineNumber(lineNum - 2, l2);
                mv.visitMethodInsn(INVOKESTATIC, "org/wildfly/swarm/cdi/jaxrsapi/deployment/ProxyBuilder", "builder", "(Ljava/lang/Class;Ljavax/ws/rs/client/WebTarget;)Lorg/wildfly/swarm/cdi/jaxrsapi/deployment/ProxyBuilder;", false);
                Label l3 = new Label();
                mv.visitLabel(l3);
                mv.visitLineNumber(lineNum++, l3);
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/wildfly/swarm/cdi/jaxrsapi/deployment/ProxyBuilder", "build", "()Ljava/lang/Object;", false);
                mv.visitTypeInsn(CHECKCAST, clientInterfaceName.replace('.', '/'));
                for (int i = 1; i <= method.parameters().size(); i++) {
                    mv.visitVarInsn(ALOAD, i);
                }
                Label l4 = new Label();
                mv.visitLabel(l4);
                mv.visitLineNumber(lineNum++, l4);
                mv.visitMethodInsn(INVOKEINTERFACE, clientInterfaceName.replace('.', '/'), method.name(), buildMethodDef(method), true);
                Label l5 = new Label();
                mv.visitLabel(l5);
                if (method.returnType().kind().equals(org.jboss.jandex.Type.Kind.VOID)) {
                    mv.visitLineNumber(lineNum++, l5);
                    mv.visitInsn(RETURN);
                } else {
                    mv.visitLineNumber(lineNum - 4, l5);
                    mv.visitInsn(ARETURN);
                }
                Label l6 = new Label();
                mv.visitLabel(l6);
                int methodParams = 0;
                mv.visitLocalVariable("this", buildTypeDef(implName), null, l0, l6, methodParams++);
                for (AnnotationInstance anno : method.annotations()) {
                    if (anno.name().toString().contains("QueryParam") || anno.name().toString().contains("PathParam")) {
                        short position = anno.target().asMethodParameter().position();
                        org.jboss.jandex.Type parameterType = anno.target().asMethodParameter().method().parameters().get(position);
                        mv.visitLocalVariable(String.valueOf(anno.value().value()), buildTypeDef(parameterType.name().toString()), null, l0, l6, methodParams++);
                    }
                }
                mv.visitMaxs(3, methodParams);
                lineNum += 4;
                mv.visitEnd();
            }
        }

        static String buildTypeDef(String name) {
            return "L" + name.replace('.', '/') + ";";
        }

        static String buildMethodDef(MethodInfo method) {
            StringBuilder builder = new StringBuilder();

            // Method Parameters
            builder.append("(");
            for (org.jboss.jandex.Type type : method.parameters()) {
                builder.append(buildTypeDef(type.name().toString()));
            }
            builder.append(")");

            // Method Return Type
            if (method.returnType().kind().equals(org.jboss.jandex.Type.Kind.VOID)) {
                builder.append("V");
            } else {
                builder.append(buildTypeDef(method.returnType().name().toString()));
            }

            return builder.toString();
        }
    }
}
