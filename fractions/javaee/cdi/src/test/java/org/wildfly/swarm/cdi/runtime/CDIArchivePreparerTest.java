package org.wildfly.swarm.cdi.runtime;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Test;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class CDIArchivePreparerTest {

    @Test
    public void testJarWithBeansXml() {
        JARArchive jar = ShrinkWrap.create(JARArchive.class, "my.jar");

        StringAsset beansXml = new StringAsset("beans.xml content");
        jar.add(beansXml, "META-INF/beans.xml");

        CDIArchivePreparer preparer = new CDIArchivePreparer();

        preparer.prepareArchive(jar);

        Node fetched = jar.get("META-INF/beans.xml");
        assertThat(fetched).isNotNull();
        assertThat(fetched.getAsset()).isNotNull();
        assertThat(((StringAsset) fetched.getAsset()).getSource()).isEqualTo("beans.xml content");
    }

    @Test
    public void testJarWithoutBeansXml() {
        JARArchive jar = ShrinkWrap.create(JARArchive.class, "my.jar");

        CDIArchivePreparer preparer = new CDIArchivePreparer();

        preparer.prepareArchive(jar);

        Node fetched = jar.get("META-INF/beans.xml");
        assertThat(fetched).isNotNull();
        assertThat(fetched.getAsset()).isNotNull();
        assertThat(fetched.getAsset()).isInstanceOf(EmptyAsset.class);
    }

    @Test
    public void testWarWithBeansXml() {
        JARArchive jar = ShrinkWrap.create(JARArchive.class, "my.war");

        StringAsset beansXml = new StringAsset("beans.xml content");
        jar.add(beansXml, "WEB-INF/beans.xml");

        CDIArchivePreparer preparer = new CDIArchivePreparer();

        preparer.prepareArchive(jar);

        Node fetched = jar.get("WEB-INF/beans.xml");
        assertThat(fetched).isNotNull();
        assertThat(fetched.getAsset()).isNotNull();
        assertThat(((StringAsset) fetched.getAsset()).getSource()).isEqualTo("beans.xml content");
    }

    @Test
    public void testWarWithoutBeansXml() {
        JARArchive jar = ShrinkWrap.create(JARArchive.class, "my.war");

        CDIArchivePreparer preparer = new CDIArchivePreparer();

        preparer.prepareArchive(jar);

        Node fetched = jar.get("WEB-INF/beans.xml");
        assertThat(fetched).isNotNull();
        assertThat(fetched.getAsset()).isNotNull();
        assertThat(fetched.getAsset()).isInstanceOf(EmptyAsset.class);
    }
}
