package org.wildfly.swarm.container.runtime.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.enterprise.inject.Vetoed;

import org.jboss.as.controller.Extension;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.container.runtime.GenericParserFactory;
import org.wildfly.swarm.spi.api.annotations.Configuration;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;
import org.wildfly.swarm.spi.api.annotations.ExtensionClassName;
import org.wildfly.swarm.spi.api.annotations.ExtensionModule;
import org.wildfly.swarm.spi.api.annotations.Ignorable;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.runtime.ServerConfiguration;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Vetoed
public class ServerConfigurationBuilder {

    private Class fractionClass;

    private boolean marshal = false;

    private boolean ignorable = false;

    private String extensionModule = null;

    private String extensionClassName = null;

    private List<String> deploymentModules = new ArrayList<>();

    public ServerConfigurationBuilder(Class fractionClass) {
        this.fractionClass = fractionClass;
    }

    @SuppressWarnings("deprecation")
    protected void determineMarshal() {
        Annotation anno = this.fractionClass.getAnnotation(MarshalDMR.class);
        if (anno != null) {
            this.marshal = true;
            return;
        }

        Configuration configAnno = (Configuration) this.fractionClass.getAnnotation(Configuration.class);

        if (configAnno != null) {
            this.marshal = configAnno.marshal();
        }
    }

    @SuppressWarnings("deprecation")
    protected void determineIgnorable() {
        Annotation anno = this.fractionClass.getAnnotation(Ignorable.class);
        if (anno != null) {
            this.ignorable = true;
            return;
        }

        Configuration configAnno = (Configuration) this.fractionClass.getAnnotation(Configuration.class);

        if (configAnno != null) {
            this.ignorable = configAnno.ignorable();
        }
    }

    @SuppressWarnings("deprecation")
    protected void determineExtensionModule() {
        ExtensionModule anno = (ExtensionModule) this.fractionClass.getAnnotation(ExtensionModule.class);
        if (anno != null) {
            this.extensionModule = anno.value();
            return;
        }

        Configuration configAnno = (Configuration) this.fractionClass.getAnnotation(Configuration.class);

        if (configAnno != null) {
            this.extensionModule = configAnno.extension();
        }
    }

    @SuppressWarnings("deprecation")
    protected void determineExtensionClassName() {
        ExtensionClassName anno = (ExtensionClassName) this.fractionClass.getAnnotation(ExtensionClassName.class);
        if (anno != null) {
            this.extensionClassName = anno.value();
            return;
        }

        Configuration configAnno = (Configuration) this.fractionClass.getAnnotation(Configuration.class);

        if (configAnno != null) {
            this.extensionClassName = configAnno.extensionClassName();
        }
    }

    @SuppressWarnings("deprecation")
    protected void determineDeploymentModules() {
        DeploymentModules anno = (DeploymentModules) this.fractionClass.getAnnotation(DeploymentModules.class);

        if (anno != null) {
            for (DeploymentModule module : anno.value()) {
                String name = module.name();
                String slot = (module.slot() != null ? module.slot() : "main");

                this.deploymentModules.add(name + ":" + slot);
            }
        } else {
            Annotation[] annos = this.fractionClass.getAnnotationsByType(DeploymentModule.class);

            if (annos.length > 0) {
                for (Annotation each : annos) {
                    DeploymentModule module = (DeploymentModule) each;

                    String name = module.name();
                    String slot = (module.slot() != null ? module.slot() : "main");

                    this.deploymentModules.add(name + ":" + slot);
                }
                return;
            }
        }

        Configuration configAnno = (Configuration) this.fractionClass.getAnnotation(Configuration.class);

        if (configAnno != null) {
            Collections.addAll(this.deploymentModules, configAnno.deploymentModules());
        }
    }

    @SuppressWarnings("unchecked")
    protected ServerConfiguration internalBuild() throws ClassNotFoundException, ModuleLoadException {

        determineMarshal();
        determineIgnorable();
        determineExtensionModule();
        determineExtensionClassName();
        determineDeploymentModules();

        AnnotationBasedServerConfiguration serverConfig = new AnnotationBasedServerConfiguration(this.fractionClass);

        serverConfig.ignorable(this.ignorable);
        serverConfig.extension(this.extensionModule);
        serverConfig.marshal(this.marshal);

        if (this.extensionModule != null) {
            Module extensionModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(this.extensionModule));

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
        for (Class<?> annotationClass : CLASS_ANNOTATIONS) {
            if (this.fractionClass.getAnnotation(annotationClass) != null) {
                return true;
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
