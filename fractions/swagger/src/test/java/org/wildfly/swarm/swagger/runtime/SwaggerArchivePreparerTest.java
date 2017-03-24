package org.wildfly.swarm.swagger.runtime;

import com.myapp.MyResource;
import com.myapp.mysubstuff.MyOtherResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.junit.Test;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.swagger.SwaggerArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class SwaggerArchivePreparerTest {

    @Test
    public void testWithoutSwaggerConf() {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);

        archive.addResource(MyResource.class);
        archive.addResource(MyOtherResource.class);

        SwaggerArchivePreparer preparer = new SwaggerArchivePreparer();
        preparer.prepareArchive(archive);

        SwaggerArchive swaggerArchive = archive.as(SwaggerArchive.class);

        assertThat(swaggerArchive.getResourcePackages()).containsOnly("com.myapp");
    }

    @Test
    public void testWithSwaggerConfInRoot() {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);

        archive.addResource(MyResource.class);
        archive.addResource(MyOtherResource.class);
        archive.add(new ByteArrayAsset("packages: com.myapp.mysubstuff".getBytes()), "META-INF/swarm.swagger.conf");

        SwaggerArchivePreparer preparer = new SwaggerArchivePreparer();
        preparer.prepareArchive(archive);

        SwaggerArchive swaggerArchive = archive.as(SwaggerArchive.class);

        assertThat(swaggerArchive.getResourcePackages()).containsOnly("com.myapp.mysubstuff");
    }

    @Test
    public void testWithSwaggerConfInWebInfClasses() {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);

        archive.addResource(MyResource.class);
        archive.addResource(MyOtherResource.class);
        archive.add(new ByteArrayAsset("packages: com.myapp.mysubstuff".getBytes()), "WEB-INF/classes/META-INF/swarm.swagger.conf");

        SwaggerArchivePreparer preparer = new SwaggerArchivePreparer();
        preparer.prepareArchive(archive);

        SwaggerArchive swaggerArchive = archive.as(SwaggerArchive.class);

        assertThat(swaggerArchive.getResourcePackages()).containsOnly("com.myapp.mysubstuff");
    }
}
