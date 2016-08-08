package org.wildfly.swarm.jolokia;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.junit.Assert.assertTrue;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class JolokiaTest {

    @Deployment(testable = false)
    public static Archive deployment() {
        JARArchive deployment = ShrinkWrap.create(JARArchive.class);
        deployment.add(EmptyAsset.INSTANCE, "nothing");
        return deployment;
    }

    @Test
    public void testJolokia() throws Exception {

        HttpClientBuilder builder = HttpClientBuilder.create();
        CloseableHttpClient client = builder.build();

        HttpUriRequest request = new HttpGet("http://localhost:8080/jolokia");
        CloseableHttpResponse response = client.execute(request);

        HttpEntity entity = response.getEntity();

        InputStream content = entity.getContent();

        byte[] buf = new byte[1024];
        int len = 0;

        StringBuilder str = new StringBuilder();

        while ( ( len = content.read(buf)) >= 0 ) {
            str.append( new String(buf, 0, len));
        }

        System.err.println( str );

        assertTrue( str.toString().contains( "{\"request\":{\"type\":\"version\"}" ) );
    }
}
