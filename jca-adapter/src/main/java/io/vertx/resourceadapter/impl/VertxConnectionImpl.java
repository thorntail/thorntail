package io.vertx.resourceadapter.impl;

import io.vertx.resourceadapter.VertxConnection;
import io.vertx.resourceadapter.VertxEventBus;

import java.util.logging.Logger;

import javax.resource.ResourceException;

public class VertxConnectionImpl implements VertxConnection {

  private static final Logger log = Logger.getLogger(VertxConnectionImpl.class.getName());

  private VertxManagedConnection mc;

  public VertxConnectionImpl(VertxManagedConnection mc) {
    this.mc = mc;
  }

  /**
   * Get connection from factory
   *
   * @return VertxConnection instance
   * @exception ResourceException
   *              Thrown if a connection can't be obtained
   */
  @Override
  public VertxEventBus vertxEventBus() throws ResourceException {
    log.finest("getConnection()");
    if (this.mc != null) {
      return this.mc.getVertxEventBus();
    }
    throw new ResourceException("Vertx Managed Connection has been closed.");
  }

  @Override
  public void close() throws ResourceException {
    this.mc.closeHandle(this);
    this.mc = null;
  }

}
