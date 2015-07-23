package org.wildfly.swarm.keycloak;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.wildfly.swarm.container.util.XmlWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @author Bob McWhirter
 */
public class SecuredWebXmlAsset implements Asset {
    @Override
    public InputStream openStream() {
        StringWriter out = new StringWriter();
        XmlWriter writer = new XmlWriter(out);
        try {
            XmlWriter.Element webApp = writer.element("web-app");
            webApp.attr("xmlns", "http://java.sun.com/xml/ns/javaee");
            webApp.attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            webApp.attr("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd");
            webApp.attr("version", "3.0");

            XmlWriter.Element securityConstraint = webApp.element("security-constraint");
            XmlWriter.Element webResourceCollection = securityConstraint.element("web-resource-collection");
            webResourceCollection.element( "url-pattern" ).content( "/*" ).end();
            webResourceCollection.end();

            XmlWriter.Element authConstraint = securityConstraint.element("auth-constraint");
            authConstraint.element( "role-name" ).content( "admin" ).end();
            authConstraint.end();
            securityConstraint.end();

            XmlWriter.Element loginConfig = webApp.element("login-config");
            loginConfig.element( "auth-method" ).content( "KEYCLOAK" ).end();
            loginConfig.element( "realm-name" ).content( "ignored" ).end();
            loginConfig.end();

            XmlWriter.Element securityRole = webApp.element("security-role");
            securityRole.element( "role-name" ).content( "admin" ).end();
            securityRole.end();
            webApp.end();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println( out.toString() );
        return new ByteArrayInputStream(out.toString().getBytes());
    }
}
