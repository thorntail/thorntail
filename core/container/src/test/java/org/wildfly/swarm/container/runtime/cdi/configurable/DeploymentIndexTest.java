package org.wildfly.swarm.container.runtime.cdi.configurable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class DeploymentIndexTest {

    @Test
    public void testIndexBuilt() {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.addClass(Foo.class);
        JavaArchive lib1 = ShrinkWrap.create(JavaArchive.class).addClass(Bar.class);
        war.addAsLibraries(lib1);
        IndexView index = new DeploymentProducer().createDeploymentIndex(war);
        assertContains(index, Foo.class);
        assertDoesNotContain(index, Baz.class);
        assertContains(index, Bar.class);
        assertDoesNotContain(index, Delta.class);
    }

    @Test
    public void testIndexAttached() throws IOException {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.addClass(Foo.class);
        war.add(createIndexAsset(Foo.class, Baz.class), DeploymentProducer.INDEX_LOCATION);
        JavaArchive lib1 = ShrinkWrap.create(JavaArchive.class).addClass(Bar.class);
        lib1.add(createIndexAsset(Delta.class), DeploymentProducer.INDEX_LOCATION);
        war.addAsLibraries(lib1);
        IndexView index = new DeploymentProducer().createDeploymentIndex(war);
        assertContains(index, Foo.class);
        // Baz should be found in the attached index
        assertContains(index, Baz.class);
        assertContains(index, Delta.class);
        // Bar is not in the attached index
        assertDoesNotContain(index, Bar.class);
    }

    private void assertContains(IndexView index, Class<?> clazz) {
        Assert.assertTrue("Index should contain: " + clazz, index.getKnownClasses().stream().anyMatch(c -> c.name().toString().equals(clazz.getName())));
    }

    private void assertDoesNotContain(IndexView index, Class<?> clazz) {
        Assert.assertTrue("Index should not contain: " + clazz, index.getKnownClasses().stream().noneMatch(c -> c.name().toString().equals(clazz.getName())));
    }

    private Asset createIndexAsset(Class<?>... classes) throws IOException {
        Indexer indexer = new Indexer();
        for (Class<?> clazz : classes) {
            try (InputStream stream = DeploymentIndexTest.class.getClassLoader().getResourceAsStream(clazz.getName().replace(".", "/") + ".class")) {
                if (stream != null) {
                    indexer.index(stream);
                }
            }
        }
        Index index = indexer.complete();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new IndexWriter(out).write(index);
        return new ByteArrayAsset(out.toByteArray());
    }

}
