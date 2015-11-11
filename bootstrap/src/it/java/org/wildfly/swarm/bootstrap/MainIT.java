package org.wildfly.swarm.bootstrap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;


/**
 * @author Bob McWhirter
 */
public class MainIT extends AbstractBootstrapIntegrationTestCase {

    @Test
    public void testNoUserMain() throws Throwable {
        JavaArchive archive = createBootstrapArchive();

        JavaArchive app = ShrinkWrap.create(JavaArchive.class);
        app.addClass(MyMain.class);
        archive.add(new ArchiveAsset(app, ZipExporter.class), "_bootstrap/myapp.jar");

        ClassLoader cl = createClassLoader(archive);

        Class<?> mainClass = cl.loadClass(Main.class.getName());

        Constructor<?> ctor = mainClass.getConstructor(String[].class);

        Object main = ctor.newInstance((Object) new String[]{});

        Method getMainClassName = mainClass.getMethod("getMainClassName");

        String mainClassName = (String) getMainClassName.invoke(main);

        assertThat(mainClassName).isEqualTo(Main.DEFAULT_MAIN_CLASS_NAME);
    }

    @Test
    public void testWithUserMain() throws Throwable {
        JavaArchive archive = createBootstrapArchive(MyMain.class.getName());

        JavaArchive app = ShrinkWrap.create(JavaArchive.class);
        app.addClass(MyMain.class);
        archive.add(new ArchiveAsset(app, ZipExporter.class), "_bootstrap/myapp.jar");

        ClassLoader cl = createClassLoader(archive);

        Class<?> mainClass = cl.loadClass(Main.class.getName());

        Constructor<?> ctor = mainClass.getConstructor(String[].class);

        Object main = ctor.newInstance((Object) new String[]{});

        Method getMainClassName = mainClass.getMethod("getMainClassName");

        String mainClassName = (String) getMainClassName.invoke(main);

        assertThat(mainClassName).isEqualTo(MyMain.class.getName());
    }

    @Test
    public void testRunWithoutError() throws Throwable {
        JavaArchive archive = createBootstrapArchive(MyMain.class.getName(), "_bootstrap/myapp.jar");

        JavaArchive app = ShrinkWrap.create(JavaArchive.class);
        app.addClass(MyMain.class);
        archive.add(new ArchiveAsset(app, ZipExporter.class), "_bootstrap/myapp.jar");

        ClassLoader cl = createClassLoader(archive);

        Class<?> mainClass = cl.loadClass(Main.class.getName());

        Constructor<?> ctor = mainClass.getConstructor(String[].class);

        Object main = ctor.newInstance((Object) new String[]{});

        Method run = mainClass.getMethod("run");

        run.invoke(main);
    }

    @Test
    public void testRunMainWithError() throws Throwable {
        JavaArchive archive = createBootstrapArchive(MyMainThatThrows.class.getName(), "_bootstrap/myapp.jar");

        JavaArchive app = ShrinkWrap.create(JavaArchive.class);
        app.addClass(MyMainThatThrows.class);
        archive.add(new ArchiveAsset(app, ZipExporter.class), "_bootstrap/myapp.jar");

        ClassLoader cl = createClassLoader(archive);

        Class<?> mainClass = cl.loadClass(Main.class.getName());

        Constructor<?> ctor = mainClass.getConstructor(String[].class);

        Object main = ctor.newInstance((Object) new String[]{});

        Method run = mainClass.getMethod("run");


        boolean exceptionFound = false;
        try {
            run.invoke(main);
            fail( "should have thrown" );
        } catch (Throwable t) {
            while ( t != null ) {
                if ( t.getMessage() != null && t.getMessage().equals( "expected to throw" ) ) {
                    exceptionFound = true;
                    break;
                }
                t = t.getCause();
            }
        }

        assertThat( exceptionFound ).isTrue();
    }


}
