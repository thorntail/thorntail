package org.wildfly.swarm.resourceadapters;

import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Ralf Battenfeld
 */
@Path("/")
public class MyResource {

    @GET
    @Produces("text/plain")
    public String get() throws NamingException, SQLException {
    	Context ctx = new InitialContext();
    	FileIOBean fileIOBean = (FileIOBean) ctx.lookup("java:module/FileIOBean");    	
    	return "Howdy using connection: " + fileIOBean.getConnectionFactory();
    }
}
