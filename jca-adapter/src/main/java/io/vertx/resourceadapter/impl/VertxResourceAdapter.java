package io.vertx.resourceadapter.impl;

import io.vertx.resourceadapter.inflow.impl.VertxActivation;
import io.vertx.resourceadapter.inflow.impl.VertxActivationSpec;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

/**
 * VertxResourceAdapter is the Resource Adapter used to interact with the Vert.x
 * platform.
 * 
 * It uses a distributed event bus from Vert.x to send message, and receives
 * message by registering a Vert.x Handler.
 *
 */
@SuppressWarnings("rawtypes")
@Connector(reauthenticationSupport = false, displayName = { "Vert.x Resource Adapter" }, description = { "VertxResourceAdapter is the Resource Adapter used to interact with a Vert.x cluster." }, eisType = "vertx", transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction)
public class VertxResourceAdapter implements ResourceAdapter,
    java.io.Serializable {

  private static final long serialVersionUID = 1130617878526175034L;

  private static Logger log = Logger.getLogger(VertxResourceAdapter.class.getName());

  private final ConcurrentMap<VertxActivationSpec, VertxActivation> activations = new ConcurrentHashMap<>();

  private WorkManager workManager;

  public VertxResourceAdapter() {
  }

  /**
   * This is called during the activation of a message endpoint.
   *
   * @param endpointFactory
   *          A message endpoint factory instance.
   * @param spec
   *          An activation spec JavaBean instance.
   * @throws ResourceException
   *           generic exception
   */
  public void endpointActivation(MessageEndpointFactory endpointFactory,
      ActivationSpec spec) throws ResourceException {
    VertxActivation activation = new VertxActivation(this, endpointFactory,
        (VertxActivationSpec) spec);
    activations.put((VertxActivationSpec) spec, activation);
    activation.start();

    log.finest("endpointActivation()");

  }

  /**
   * This is called when a message endpoint is deactivated.
   *
   * @param endpointFactory
   *          A message endpoint factory instance.
   * @param spec
   *          An activation spec JavaBean instance.
   */
  public void endpointDeactivation(MessageEndpointFactory endpointFactory,
      ActivationSpec spec) {
    VertxActivation activation = activations.remove(spec);
    if (activation != null)
      activation.stop();

    log.finest("endpointDeactivation()");

  }

  /**
   * This is called when a resource adapter instance is bootstrapped.
   *
   * @param ctx
   *          A bootstrap context containing references
   * @throws ResourceAdapterInternalException
   *           indicates bootstrap failure.
   */
  public void start(BootstrapContext ctx)
      throws ResourceAdapterInternalException {
    log.finest("sets up configuration.");
    this.workManager = ctx.getWorkManager();
  }

  public WorkManager getWorkManager() {
    return workManager;
  }

  /**
   * This is called when a resource adapter instance is undeployed or during
   * application server shutdown.
   * 
   * It will stop all Vert.x embedded platform.
   * 
   */
  public void stop() {
    log.finest("stop()");
    this.activations.clear();
    VertxPlatformFactory.instance().closeAllPlatforms();
  }

  /**
   * This method is called by the application server during crash recovery.
   *
   * @param specs
   *          An array of ActivationSpec JavaBeans
   * @throws ResourceException
   *           generic exception
   * @return An array of XAResource objects
   */
  public XAResource[] getXAResources(ActivationSpec[] specs)
      throws ResourceException {
    log.finest("getXAResources()");
    return null;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    VertxResourceAdapter other = (VertxResourceAdapter) obj;
    return super.equals(other);
  }

}
