package org.wildfly.swarm.datasources;

/**
 * @author Bob McWhirter
 */
public class DatasourceDeployment { //implements Deployment {

    /*
    private final Datasource ds;

    public DatasourceDeployment(Datasource ds) {
        this.ds = ds;
    }

    @Override
    public String getName() {
        return this.ds.getName() + "-ds.xml";
    }

    @Override
    public VirtualFile getContent() throws IOException {
        VirtualFile mountPoint = VFS.getRootVirtualFile().getChild(getName());
        File dsXml = File.createTempFile(getName(), "-ds.xml");
        dsXml.delete();

        try (XmlWriter out = new XmlWriter(new FileWriter(dsXml))) {

            XmlWriter.Element datasources = out.element("datasources")
                    .attr("xmlns", "http://www.jboss.org/ironjacamar/schema")
                    .attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                    .attr("xsi:schemaLocation", "http://www.jboss.org/ironjacamar/schema http://docs.jboss.org/ironjacamar/schema/datasources_1_0.xsd");

            XmlWriter.Element datasource = datasources.element("datasource")
                    .attr("jndi-name", this.ds.getJNDIName())
                    .attr("enabled", "true")
                    .attr("use-java-context", "true")
                    .attr("pool-name", this.ds.getName());

            datasource.element("connection-url")
                    .content(this.ds.getConnectionURL())
                    .end();

            datasource.element("driver")
                    .content(this.ds.getDriver())
                    .end();

            XmlWriter.Element security = datasource.element("security");

            security.element("user-name")
                    .content(this.ds.getUserName())
                    .end();

            security.element("password")
                    .content(this.ds.getPassword())
                    .end();

            security.end();
            datasource.end();
            datasources.end();

            out.close();
        }

        VFS.mountReal(dsXml, mountPoint);
        return mountPoint;
    }
    */
}
