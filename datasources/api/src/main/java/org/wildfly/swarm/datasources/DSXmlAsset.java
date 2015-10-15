package org.wildfly.swarm.datasources;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.container.util.XmlWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class DSXmlAsset implements Asset {

    private final DataSource ds;

    public DSXmlAsset(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public InputStream openStream() {

        StringWriter str = new StringWriter();

        try (XmlWriter out = new XmlWriter(str)) {

            XmlWriter.Element datasources = out.element("datasources")
                    .attr("xmlns", "http://www.jboss.org/ironjacamar/schema")
                    .attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                    .attr("xsi:schemaLocation", "http://www.jboss.org/ironjacamar/schema http://docs.jboss.org/ironjacamar/schema/datasources_1_0.xsd");

            XmlWriter.Element datasource = datasources.element("datasource")
                    .attr("jndi-name", this.ds.jndiName())
                    .attr("enabled", "true")
                    .attr("use-java-context", "true")
                    .attr("pool-name", this.ds.getKey());

            datasource.element("connection-url")
                    .content(this.ds.connectionUrl())
                    .end();

            datasource.element("driver")
                    .content(this.ds.driverName())
                    .end();

            XmlWriter.Element security = datasource.element("security");

            if (this.ds.userName() != null) {
                security.element("user-name")
                        .content(this.ds.userName())
                        .end();
            }

            if (this.ds.password() != null) {
                security.element("password")
                        .content(this.ds.password())
                        .end();

            }
            security.end();
            datasource.end();
            datasources.end();

            out.close();
            return new ByteArrayInputStream( str.toString().getBytes() );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
