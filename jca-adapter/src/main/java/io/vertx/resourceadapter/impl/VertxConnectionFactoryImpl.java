package io.vertx.resourceadapter.impl;

import io.vertx.resourceadapter.VertxConnection;
import io.vertx.resourceadapter.VertxConnectionFactory;

import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

/**
 * 
 * Implementation of VertxPlatformFactory.
 * 
 * It delegates the ConnectionManager to allocate the connection to the Vertx
 * Platform.
 * 
 * @author Lin Gao <lgao@redhat.com>
 *
 */
public class VertxConnectionFactoryImpl implements VertxConnectionFactory {

  private static final long serialVersionUID = -3548251896235579548L;

  private static Logger log = Logger.getLogger(VertxConnectionFactoryImpl.class.getName());

  private Reference reference;

  private VertxManagedConnectionFactory mcf;

  private ConnectionManager connectionManager;

  public VertxConnectionFactoryImpl() {
  }

  public VertxConnectionFactoryImpl(VertxManagedConnectionFactory mcf,
      ConnectionManager cxManager) {
    this.mcf = mcf;
    this.connectionManager = cxManager;
  }

  @Override
  public VertxConnection getVertxConnection() throws ResourceException {
    log.finest("Get VertxPlatform");
    return (VertxConnection) connectionManager.allocateConnection(mcf, null);
  }

  /**
   * Get the Reference instance.
   *
   * @return Reference instance
   * @exception NamingException
   *              Thrown if a reference can't be obtained
   */
  @Override
  public Reference getReference() throws NamingException {
    log.finest("getReference()");
    return reference;
  }

  /**
   * Set the Reference instance.
   *
   * @param reference
   *          A Reference instance
   */
  @Override
  public void setReference(Reference reference) {
    log.finest("setReference()");
    this.reference = reference;
  }

}
