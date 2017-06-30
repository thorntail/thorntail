package org.wildfly.swarm.bootstrap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;

/**
 * @author Bob McWhirter
 */
public class MainInvoker {

    private static final String BOOT_MODULE_PROPERTY = "boot.module.loader";

    public MainInvoker(Method mainMethod, String... args) {
        this.mainMethod = mainMethod;
        this.args = args;
        System.setProperty(BOOT_MODULE_PROPERTY, BootModuleLoader.class.getName());
    }

    public MainInvoker(Class<?> mainClass, String... args) throws Exception {
        this(getMainMethod(mainClass), args);
    }

    public MainInvoker(String mainClassName, String... args) throws Exception {
        this(getMainMethod(getMainClass(mainClassName)), args);
    }

    public void invoke() throws Exception {
        this.mainMethod.invoke(null, new Object[]{this.args});
        emitReady();
    }

    public void stop() throws Exception {
        Method stopMethod = null;
        Class<?> mainClass = mainMethod.getDeclaringClass();
        try {
            stopMethod = mainClass.getDeclaredMethod("stopMain");
        } catch (NoSuchMethodException e) {
        }

        if (stopMethod != null) {
            stopMethod.invoke(mainClass, (Object[]) null);
        }
    }

    protected void emitReady() throws Exception {
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.container"));
        Class<?> messagesClass = module.getClassLoader().loadClass("org.wildfly.swarm.internal.SwarmMessages");
        Field field = messagesClass.getDeclaredField("MESSAGES");

        Object messages = field.get(null);

        Method ready = messages.getClass().getMethod("wildflySwarmIsReady");
        ready.invoke(messages);
    }

    public static void main(String... args) throws Exception {
        System.setProperty(BOOT_MODULE_PROPERTY, BootModuleLoader.class.getName());
        List<String> argList = Arrays.asList(args);

        if (argList.isEmpty()) {
            throw new RuntimeException("Invalid usage of MainWrapper; actual main-class must be specified");
        }

        String mainClassName = argList.get(0);
        List<String> actualArgs = argList.subList(1, argList.size());

        Class<?> mainClass = getMainClass(mainClassName);

        Method mainMethod = getMainMethod(mainClass);

        MainInvoker wrapper = new MainInvoker(mainMethod, actualArgs.toArray(new String[]{}));
        wrapper.invoke();
    }

    public static Class<?> getMainClass(String mainClassName) throws IOException, URISyntaxException, ModuleLoadException, ClassNotFoundException {
        Class<?> mainClass = null;
        try {
            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));
            ClassLoader cl = module.getClassLoader();
            mainClass = cl.loadClass(mainClassName);
        } catch (ClassNotFoundException | ModuleLoadException e) {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            mainClass = cl.loadClass(mainClassName);
        }

        if (mainClass == null) {
            throw new ClassNotFoundException(mainClassName);
        }
        return mainClass;
    }

    public static Method getMainMethod(Class<?> mainClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method mainMethod = mainClass.getMethod("main", String[].class);

        if (mainMethod == null) {
            throw new NoSuchMethodException("No method main() found");
        }

        final int modifiers = mainMethod.getModifiers();
        if (!Modifier.isStatic(modifiers)) {
            throw new NoSuchMethodException("Main method is not static for " + mainClass);
        }
        return mainMethod;
    }

    private final Method mainMethod;

    private final String[] args;
}
