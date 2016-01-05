package org.wildfly.swarm.swagger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Test;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.msc.ServiceActivatorArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Lance Ball
 */
public class SwaggerArchiveTest {

    //@Test
    public void testRegister() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "myapp.war");
        archive.as(SwaggerArchive.class).register("com.tester.resource");

        Asset asset = archive.get(SwaggerArchive.SWAGGER_CONFIGURATION_PATH).getAsset();

        assertThat(asset).isNotNull();
        assertThat(asset).isInstanceOf(StringAsset.class);
        assertThat(((StringAsset) asset).getSource().trim()).isEqualTo("com.tester.resource");

        assertThat( archive.as(ServiceActivatorArchive.class).containsServiceActivator( SwaggerArchiveImpl.SERVICE_ACTIVATOR_CLASS_NAME )).isTrue();
    }
}
