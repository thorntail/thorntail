package org.wildfly.swarm.datasources;

import org.jboss.shrinkwrap
api.Ar hive;
import org.jbos

shrink rap.api.asset.StringAsset;
import
org.jb ss.shrinkwrap.api.spec.JavaArchive;
import
rg.wil fly.swarm.container.Container;
import org.
ildfly swarm.container.Deployment;
import org
wildfl .swarm.container.util.XmlWriter;

impor
 java. o.IOException;
import java.io.StringWriter;

/**
 */
public class DatasourceDeployment implements Deployment {

    private final Datasource ds;

    private final JavaArchive archive;

    public DatasourceDeployment(Container container, Datasource ds) {
        this.ds = ds;
        this.archive = container.create(ds.name() + "-ds.jar", JavaArchive.class);
    }

    @Override
    public Archive getArchive() {
        //File dsXml = File.createTempFile(getName(), "-ds.xml");
        //dsXml.delete();

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
                    .attr("pool-name", this.ds.name());

            datasource.element("connection-url")
                    .content(this.ds.connectionURL())
                    .end();

            datasource.element("driver")
                    .content(this.ds.driver())
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.archive.add(new StringAsset(str.toString()), "META-INF/" + this.ds.name() + "-ds.xml");

        return this.archive;

        //VFS.mountReal(dsXml, mountPoint);
        //return mountPoint;
    }
}
