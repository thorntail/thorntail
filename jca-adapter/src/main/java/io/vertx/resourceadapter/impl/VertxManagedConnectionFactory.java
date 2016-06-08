package io.vertx.resourceadapter.impl;

import io.vertx.core.Vertx;
import io.vertx.resourceadapter.VertxConnection;
import io.vertx.resourceadapter.VertxConnectionFactory;
import io.vertx.resourceadapter.impl.VertxPlatformFactory.VertxListener;

import java.io.PrintWriter;
import java.util.Set;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

/**
 * The outbound connection of the resource adapter.
 *
 * Each active *-ra.xml deployment may have different vertx platform.
 *
 * 
 */
@ConnectionDefinition(connectionFactory = VertxConnectionFactory.class, connectionFactoryImpl = VertxConnectionFactoryImpl.class, connection = VertxConnection.class, connectionImpl = VertxConnectionImpl.class)
public class VertxManagedConnectionFactory extends AbstractJcaBase implements
    ManagedConnectionFactory, ResourceAdapterAssociation, VertxListener{

  private static final long serialVersionUID = -4650320398583270937L;

  private static Logger log = Logger.getLogger(VertxManagedConnectionFactory.class.getName());

  private ResourceAdapter ra;

  private PrintWriter logwriter;

  private Vertx vertx;

  /**
   * Default constructor
   */
  public VertxManagedConnectionFactory() {
  }

  /**
   * Creates a Connection Factory instance.
   *
   * @param cxManager
   *          ConnectionManager to be associated with created EIS connection
   *          factory instance
   * @return EIS-specific Connection Factory instance or
   *         javax.resource.cci.ConnectionFactory instance
   * @throws ResourceException
   *           Generic exception
   */
  public Object createConnectionFactory(ConnectionManager cxManager)
      throws ResourceException {
    log.finest("createConnectionFactory()");
    return new VertxConnectionFactoryImpl(this, cxManager);
  }

  /**
   * Creates a Connection Factory instance.
   *
   * @return EIS-specific Connection Factory instance or
   *         javax.resource.cci.ConnectionFactory instance
   * @throws ResourceException
   *           Generic exception
   */
  public Object createConnectionFactory() throws ResourceException {
    throw new ResourceException("Vert.x JCA adapter not supported in non-managed environment");
  }

  /**
   * Creates a new physical connection to the underlying EIS resource manager.
   *
   * @param subject
   *          Caller's security information
   * @param cxRequestInfo
   *          Additional resource adapter specific connection request
   *          information
   * @throws ResourceException
   *           generic exception
   * @return ManagedConnection instance
   */
  public ManagedConnection createManagedConnection(Subject subject,
      ConnectionRequestInfo cxRequestInfo) throws ResourceException {
    VertxPlatformFactory.instance().getOrCreateVertx(getVertxPlatformConfig(), this);
    return new VertxManagedConnection(this, this.vertx);
  }

  /**
   * Returns a matched connection from the candidate set of connections.
   *
   * @param connectionSet
   *          Candidate connection set
   * @param subject
   *          Caller's security information
   * @param cxRequestInfo
   *          Additional resource adapter specific connection request
   *          information
   * @throws ResourceException
   *           generic exception
   * @return ManagedConnection if resource adapter finds an acceptable match
   *         otherwise null
   */
  @SuppressWarnings("rawtypes")
  public ManagedConnection matchManagedConnections(Set connectionSet,
      Subject subject, ConnectionRequestInfo cxRequestInfo)
      throws ResourceException {    
    
    for(Object result : connectionSet){
      if (result instanceof VertxManagedConnection) {
        VertxManagedConnection vertMC = (VertxManagedConnection) result;
        if (this.equals(vertMC.getManagementConnectionFactory())) {
          return vertMC;
        }
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((vertx == null) ? 0 : vertx.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    VertxManagedConnectionFactory other = (VertxManagedConnectionFactory) obj;
    if (vertx == null) {
      if (other.vertx != null)
        return false;
    } else if (!vertx.equals(other.vertx))
      return false;
    return true;
  }

  /**
   * Get the log writer for this ManagedConnectionFactory instance.
   *
   * @return PrintWriter
   * @throws ResourceException
   *           generic exception
   */
  public PrintWriter getLogWriter() throws ResourceException {
    log.finest("getLogWriter()");
    return logwriter;
  }

  /**
   * Set the log writer for this ManagedConnectionFactory instance.
   *
   * @param out
   *          PrintWriter - an out stream for error logging and tracing
   * @throws ResourceException
   *           generic exception
   */
  public void setLogWriter(PrintWriter out) throws ResourceException {
    log.finest("setLogWriter()");
    logwriter = out;
  }

  /**
   * Get the resource adapter
   *
   * @return The handle
   */
  public ResourceAdapter getResourceAdapter() {
    log.finest("getResourceAdapter()");
    return ra;
  }

  /**
   * Set the resource adapter
   *
   * @param ra
   *          The handle
   */
  public void setResourceAdapter(ResourceAdapter ra) {
    log.finest("setResourceAdapter()");
    this.ra = ra;
  }
  @Override
  public void whenReady(Vertx vertx) {
    if(vertx != null){
      this.vertx = vertx;      
    }
  }

}
