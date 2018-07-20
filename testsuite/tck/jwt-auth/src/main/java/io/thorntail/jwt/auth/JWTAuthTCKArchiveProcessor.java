package io.thorntail.jwt.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Created by bob on 3/27/18.
 */
public class JWTAuthTCKArchiveProcessor implements ApplicationArchiveProcessor {
    private static Logger log = Logger.getLogger(JWTAuthTCKArchiveProcessor.class);

    @Override
    public void process(Archive<?> appArchive, TestClass testClass) {
        if (!(appArchive instanceof WebArchive)) {
            return;
        }
        log.info("Preparing archive: "+appArchive);
        // Only augment archives with a publicKey indicating a MP-JWT test
        WebArchive war = WebArchive.class.cast(appArchive);
        Node configProps = war.get("/META-INF/microprofile-config.properties");
        Node publicKeyNode = war.get("/WEB-INF/classes/publicKey.pem");
        Node publicKey4kNode = war.get("/WEB-INF/classes/publicKey4k.pem");
        Node mpJWT = war.get("MP-JWT");
        if (configProps == null && publicKeyNode == null && publicKey4kNode == null && mpJWT == null) {
            return;
        }

        if (configProps != null) {
            StringWriter sw = new StringWriter();
            InputStream is = configProps.getAsset().openStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line = reader.readLine();
                while(line != null) {
                    sw.write(line);
                    sw.write('\n');
                    line = reader.readLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            // Add the
            sw.write("config_ordinal=1000\n");
            StringAsset mpConfigAsset = new StringAsset(sw.toString());
            war.addAsManifestResource(mpConfigAsset, "microprofile-config.properties");
            log.info("mp-config.props: "+sw.toString());
        } else {
            log.info("NO mp-config.props, adding /META-INF/MP-JWT-SIGNER");
            // Add a microprofile-config.properties to ensure no classpath entry leaks into the war
            String key = readKeyValue(publicKeyNode);
            StringWriter mpConfig = new StringWriter();
            mpConfig.write("config_ordinal=1000\n");
            mpConfig.write("mp.jwt.verify.publickey=");
            mpConfig.write(key);
            mpConfig.write("\n");
            mpConfig.write("mp.jwt.verify.issuer=https://server.example.com\n");
            StringAsset mpConfigAsset = new StringAsset(mpConfig.toString());
            war.addAsManifestResource(mpConfigAsset, "microprofile-config.properties");
            if(publicKey4kNode != null) {
                war.addAsManifestResource(publicKey4kNode.getAsset(), "MP-JWT-SIGNER");
            } else if(publicKeyNode != null) {
                war.addAsManifestResource(publicKeyNode.getAsset(), "MP-JWT-SIGNER");
            }
        }
        // This allows for test specific web.xml files. Generally this should not be needed.
        String warName = war.getName();
        String webXmlName = "/WEB-INF/" + warName + ".xml";
        URL webXml = JWTAuthTCKArchiveProcessor.class.getResource(webXmlName);
        if (webXml != null) {
            war.setWebXML(webXml);
        }
        war.addAsWebInfResource("jwt-roles.properties", "classes/jwt-roles.properties")
        ;
        log.info("Augmented war: \n"+war.toString(true));
    }

    private String readKeyValue(Node publicKeyNode) {
        StringWriter key = new StringWriter();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(publicKeyNode.getAsset().openStream()))) {
            String line = reader.readLine();
            while(line != null) {
                if (!line.startsWith("----")) {
                    key.write(line);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            log.warn("Failed to read publicKey: "+publicKeyNode.getPath(), e);
        }
        return key.toString();
    }
}
