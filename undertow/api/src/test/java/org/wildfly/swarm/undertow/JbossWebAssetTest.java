package org.wildfly.swarm.undertow;

import org.junit.Test;
import org.wildfly.swarm.undertow.descriptors.JBossWebAsset;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class JbossWebAssetTest {

    @Test
    public void testEmpty() throws Exception {
        JBossWebAsset asset = new JBossWebAsset();

        assertThat(asset.isRootSet()).isFalse();
        assertThat(asset.getContextRoot()).isNull();

        asset.setContextRoot("/myRoot");
        assertThat(asset.isRootSet()).isTrue();
        assertThat(asset.getContextRoot()).isEqualTo("/myRoot");

        asset.setContextRoot("/anotherRoot");
        assertThat(asset.isRootSet()).isTrue();
        assertThat(asset.getContextRoot()).isEqualTo("/anotherRoot");

    }
}
