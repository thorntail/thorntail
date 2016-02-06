package org.wildfly.swarm.undertow;

import org.junit.Test;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class WebXmlAssetTest {

    @Test
    public void testInvalidContextParamName() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getContextParam(null)).isNull();
        assertThat(asset.getContextParam("")).isNull();
        assertThat(asset.getContextParam("   ")).isNull();
    }

    @Test
    public void testUnsetContextParamName() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getContextParam("my-param")).isNull();
    }

    @Test
    public void testSingleContextParamValue() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getContextParam("my-param")).isNull();

        asset.setContextParam("my-param", "myValue");
        String paramValue = asset.getContextParam("my-param");
        assertThat(paramValue).isNotNull();
        assertThat(paramValue).isNotEmpty();
        assertThat(paramValue).isEqualTo("myValue");
    }

    @Test
    public void testMultipleContextParamValue() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getContextParam("my-param")).isNull();

        asset.setContextParam("my-param", "myValue1", "myValue2", "myValue3");
        String paramValue = asset.getContextParam("my-param");
        assertThat(paramValue).isNotNull();
        assertThat(paramValue).isNotEmpty();
        assertThat(paramValue).isEqualTo("myValue1,myValue2,myValue3");
    }

    @Test
    public void testInvalidLoginAuthMethod() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getLoginRealm(null)).isNull();
        assertThat(asset.getLoginRealm("")).isNull();
        assertThat(asset.getLoginRealm("  ")).isNull();
    }

    @Test
    public void testUnsetLoginAuthMethod() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getLoginRealm("keycloak")).isNull();
    }

    @Test
    public void testLoginAuthMethod() throws Exception {
        WebXmlAsset asset = new WebXmlAsset();

        assertThat(asset.getLoginRealm("keycloak")).isNull();

        asset.setLoginConfig("keycloak", "myRealm");
        String realm = asset.getLoginRealm("keycloak");
        assertThat(realm).isNotNull();
        assertThat(realm).isNotEmpty();
        assertThat(realm).isEqualTo("myRealm");
    }
}
