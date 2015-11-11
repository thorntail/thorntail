package org.wildfly.swarm.bootstrap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class MainIT extends AbstractBootstrapIntegrationTestCase {

    @Test
    public void testNoUserMain() throws Throwable {
        JavaArchive archive = createBootstrapArchive();

        JavaArchive app = ShrinkWrap.create(JavaArchive.class);
        app.addClass(MyMain.class);
        archive.add( app, "_bootstrap/myapp.jar", ZipExporter.class);

        ClassLoader cl = createClassLoader(archive);

        Class<?> mainClass = cl.loadClass(Main.class.getName());

        Constructor<?> ctor = mainClass.getConstructor(String[].class);

        Object main = ctor.newInstance( (Object) new String[] {} );

        Method getMainClassName = mainClass.getMethod("getMainClassName");

        String mainClassName = (String) getMainClassName.invoke( main );

        assertThat( mainClassName ).isEqualTo( Main.DEFAULT_MAIN_CLASS_NAME );
    }

    @Test
    public void testWithUserMain() throws Throwable {
        JavaArchive archive = createBootstrapArchive( MyMain.class.getName() );

        JavaArchive app = ShrinkWrap.create(JavaArchive.class);
        app.addClass(MyMain.class);
        archive.add( app, "_bootstrap/myapp.jar", ZipExporter.class);

        ClassLoader cl = createClassLoader(archive);

        Class<?> mainClass = cl.loadClass(Main.class.getName());

        Constructor<?> ctor = mainClass.getConstructor(String[].class);

        Object main = ctor.newInstance( (Object) new String[] {} );

        Method getMainClassName = mainClass.getMethod("getMainClassName");

        String mainClassName = (String) getMainClassName.invoke( main );

        assertThat( mainClassName ).isEqualTo( MyMain.class.getName() );
    }


}
