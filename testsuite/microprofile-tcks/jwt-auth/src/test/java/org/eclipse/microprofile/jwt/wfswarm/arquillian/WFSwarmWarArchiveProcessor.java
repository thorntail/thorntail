package org.eclipse.microprofile.jwt.wfswarm.arquillian;

import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * An ApplicationArchiveProcessor for the MP-JWT TCK that includes:
 * - an appropriate project-defaults.yml that sets up the required security domain supporting MP-JWT auth
 * - a jwt-roles.properties that does the group1 to Group1MappedRole mapping
 * - copies /WEB-INF/classes/publicKey.pem to /MP-JWT-SIGNER
 */
public class WFSwarmWarArchiveProcessor implements ApplicationArchiveProcessor {
    private static Logger log = Logger.getLogger(WFSwarmWarArchiveProcessor.class.getName());

    @Override
    public void process(Archive<?> appArchive, TestClass testClass) {
        if (!(appArchive instanceof WebArchive)) {
            return;
        }
        log.info("Preparing archive: " + appArchive);
        // Only augment archives with a publicKey indicating a MP-JWT test
        WebArchive war = WebArchive.class.cast(appArchive);
        Node publicKeyNode = war.get("/WEB-INF/classes/publicKey.pem");
        if (publicKeyNode == null) {
            return;
        }

        // This allows for test specific web.xml files. Generally this should not be needed.
        String warName = war.getName();
        String webXmlName = "/WEB-INF/" + warName + ".xml";
        URL webXml = WFSwarmWarArchiveProcessor.class.getResource(webXmlName);
        if (webXml != null) {
            war.setWebXML(webXml);
        }
        if (!war.contains("/WEB-INF/classes/project-defaults.yml")) {
            war.addAsResource("project-defaults.yml", "/project-defaults.yml");
        }

        war.addAsWebInfResource("jwt-roles.properties", "classes/jwt-roles.properties").addAsManifestResource(publicKeyNode.getAsset(), "/MP-JWT-SIGNER");
        log.fine("Augmented war: \n" + war.toString(true));
    }
}
