package org.wildfly.swarm.container.runtime.internal;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.enterprise.inject.Vetoed;

import org.jboss.as.controller.Extension;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.container.runtime.GenericParserFactory;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configuration;
import org.wildfly.swarm.spi.api.annotations.Default;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;
import org.wildfly.swarm.spi.api.annotations.ExtensionClassName;
import org.wildfly.swarm.spi.api.annotations.ExtensionModule;
import org.wildfly.swarm.spi.api.annotations.Ignorable;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.runtime.ServerConfiguration;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class ServerConfigurationBuilder {

    private final Module module;

    private final ClassInfo classInfo;

    private boolean marshal = false;

    private boolean ignorable = false;

    private String extensionModule = null;

    private String extensionClassName = null;

    private List<String> deploymentModules = new ArrayList<>();

    public ServerConfigurationBuilder(Module module, ClassInfo classInfo) {
        this.module = module;
        this.classInfo = classInfo;
    }

    protected List<AnnotationInstance> findAnnotations(Class<?> annotationType) {
        Collection<AnnotationInstance> all = this.classInfo.classAnnotations();
        List<AnnotationInstance> selected = new ArrayList<>();

        for (AnnotationInstance each : all) {
            if (each.name().toString().equals(annotationType.getName())) {
                selected.add(each);
            }
        }

        return selected;
    }

    protected AnnotationInstance findAnnotation(Class<?> annotationType) {
        Collection<AnnotationInstance> all = this.classInfo.classAnnotations();

        for (AnnotationInstance each : all) {
            if (each.name().toString().equals(annotationType.getName())) {
                return each;
            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    protected void determineMarshal() {
        AnnotationInstance anno = findAnnotation(MarshalDMR.class);
        if (anno != null) {
            this.marshal = true;
            return;
        }

        anno = findAnnotation(Configuration.class);

        if (anno != null) {
            AnnotationValue value = anno.value("marshal");
            if (value != null) {
                this.marshal = value.asBoolean();
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected void determineIgnorable() {
        AnnotationInstance anno = findAnnotation(Ignorable.class);
        if (anno != null) {
            this.ignorable = true;
            return;
        }

        anno = findAnnotation(Configuration.class);

        if (anno != null) {
            AnnotationValue value = anno.value("ignorable");
            if (value != null) {
                this.ignorable = value.asBoolean();
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected void determineExtensionModule() {
        AnnotationInstance anno = findAnnotation(ExtensionModule.class);
        if (anno != null) {
            AnnotationValue value = anno.value();
            if (value != null) {
                this.extensionModule = value.asString();
            }
            return;
        }

        anno = findAnnotation(Configuration.class);

        if (anno != null) {
            AnnotationValue value = anno.value("extension");
            if (value != null) {
                this.extensionModule = value.asString();
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected void determineExtensionClassName() {
        AnnotationInstance anno = findAnnotation(ExtensionClassName.class);
        if (anno != null) {
            AnnotationValue value = anno.value();
            if (value != null) {
                this.extensionClassName = value.asString();
            }
            return;
        }

        anno = findAnnotation(Configuration.class);

        if (anno != null) {
            AnnotationValue value = anno.value("extension");
            if (value != null) {
                this.extensionClassName = value.asString();
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected void determineDeploymentModules() {
        AnnotationInstance anno = findAnnotation(DeploymentModules.class);

        if ( anno != null ) {
            AnnotationInstance[] modules = anno.value().asNestedArray();
            for (AnnotationInstance each : modules) {
                AnnotationValue nameValue = each.value("name");
                AnnotationValue slotValue = each.value("slot");

                String name = nameValue.asString();
                String slot = (slotValue != null ? slotValue.asString() : "main");

                this.deploymentModules.add(name + ":" + slot);
            }
        } else {
            List<AnnotationInstance> annos = findAnnotations(DeploymentModule.class);

            if (!annos.isEmpty()) {
                for (AnnotationInstance each : annos) {
                    AnnotationValue nameValue = each.value("name");
                    AnnotationValue slotValue = each.value("slot");

                    String name = nameValue.asString();
                    String slot = (slotValue != null ? slotValue.asString() : "main");

                    this.deploymentModules.add(name + ":" + slot);
                }
                return;
            }
        }

        anno = findAnnotation(Configuration.class);

        if (anno != null) {
            AnnotationValue value = anno.value("deploymentModules");
            if (value != null) {
                String[] descs = value.asStringArray();

                Collections.addAll(this.deploymentModules, descs);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected ServerConfiguration internalBuild() throws ClassNotFoundException, ModuleLoadException {

        determineMarshal();
        determineIgnorable();
        determineExtensionModule();
        determineExtensionClassName();
        determineDeploymentModules();

        Module mainModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(this.module.getIdentifier().getName(), "main"));

        Class<? extends Fraction> fractionClass = (Class<? extends Fraction>) mainModule.getClassLoader().loadClass(this.classInfo.name().toString());

        AnnotationBasedServerConfiguration serverConfig = new AnnotationBasedServerConfiguration(fractionClass);

        serverConfig.ignorable(this.ignorable);
        serverConfig.extension(this.extensionModule);
        serverConfig.marshal(this.marshal);

        if (this.extensionModule != null) {
            Module extensionModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(this.extensionModule));
            //Class<? extends AbstractParserFactory> parserFractoryClass = (Class<? extends AbstractParserFactory>) runtimeModule.getClassLoader().loadClass(parserFactoryClass);


            if (this.extensionClassName != null && this.extensionClassName.equalsIgnoreCase("none")) {
                // skip it all
            } else if (this.extensionClassName != null && !this.extensionClassName.equals("")) {
                Class<?> extCls = extensionModule.getClassLoader().loadClass(this.extensionClassName);
                try {
                    Extension ext = (Extension) extCls.newInstance();
                    GenericParserFactory parserFactory = new GenericParserFactory(ext);
                    serverConfig.parserFactory(parserFactory);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }

            } else {
                ServiceLoader<Extension> extensions = extensionModule.loadService(Extension.class);

                Iterator<Extension> extensionIter = extensions.iterator();
                if (extensionIter.hasNext()) {
                    Extension ext = extensionIter.next();
                    GenericParserFactory parserFactory = new GenericParserFactory(ext);
                    serverConfig.parserFactory(parserFactory);
                }

                if (extensionIter.hasNext()) {
                    throw new RuntimeException("Fraction \"" + fractionClass.getName() + "\" was configured using @Configuration with an extension='',"
                            + " but has multiple extension classes.  Please use extensionClassName='' to specify exactly one.");
                }
            }

        }

        List<MethodInfo> fractionMethods = this.classInfo.methods();

        DotName defaultAnno = DotName.createSimple(Default.class.getName());

        boolean foundDefault = false;

        for (MethodInfo each : fractionMethods) {
            if (each.hasAnnotation(defaultAnno)) {
                if (!each.parameters().isEmpty()) {
                    throw new RuntimeException("Method marked @Default must require zero parameters");
                }

                if (!Modifier.isStatic(each.flags())) {
                    throw new RuntimeException("Method marked @Default must be static");
                }

                if (foundDefault) {
                    throw new RuntimeException("Multiple methods found marked as @Default");
                }

                foundDefault = true;

                serverConfig.defaultFraction(each.name());
            }
        }

        serverConfig.setDeploymentModules(this.deploymentModules.toArray(new String[this.deploymentModules.size()]));

        return serverConfig;
    }

    public ServerConfiguration build() throws ModuleLoadException, ClassNotFoundException {
        if (!isAnnotated()) {
            return null;
        }

        return internalBuild();
    }

    boolean isAnnotated() {
        Collection<AnnotationInstance> annotations = this.classInfo.classAnnotations();

        for (AnnotationInstance annotation : annotations) {
            for (Class<?> annotationClass : CLASS_ANNOTATIONS) {
                if (annotation.name().toString().equals(annotationClass.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private static Class<?>[] CLASS_ANNOTATIONS = {
            Configuration.class,
            DeploymentModule.class,
            DeploymentModules.class,
            ExtensionClassName.class,
            ExtensionModule.class,
            Ignorable.class,
            MarshalDMR.class,
    };
}
