package org.jboss.unimbus.jwt.auth;

import java.net.URL;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Created by bob on 3/27/18.
 */
public class JWTAuthTCKArchiveProcessor implements ApplicationArchiveProcessor {
    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if (!(archive instanceof WebArchive)) {
            return;
        }
        System.err.println("Preparing archive: " + archive);
        // Only augment archives with a publicKey indicating a MP-JWT test
        WebArchive war = WebArchive.class.cast(archive);
        Node publicKeyNode = war.get("/WEB-INF/classes/publicKey.pem");
        if (publicKeyNode == null) {
            return;
        }

        // This allows for test specific web.xml files. Generally this should not be needed.
        String warName = war.getName();
        String webXmlName = "/WEB-INF/" + warName + ".xml";
        URL webXml = getClass().getResource(webXmlName);
        if (webXml != null) {
            war.setWebXML(webXml);
        }
        //war.addAsResource("project-defaults.yml", "/project-defaults.yml");
        //war.addAsWebInfResource("jwt-roles.properties", "classes/jwt-roles.properties");
        war.addAsManifestResource(publicKeyNode.getAsset(), "/MP-JWT-SIGNER");
        System.err.println("Augmented war: \n" + war.toString(true));
    }
}
