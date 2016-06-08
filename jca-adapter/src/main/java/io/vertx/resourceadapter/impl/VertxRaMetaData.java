package io.vertx.resourceadapter.impl;

import javax.resource.cci.ResourceAdapterMetaData;

/**
 * VertxRaMetaData
 *
 * @version $Revision: $
 */
public class VertxRaMetaData implements ResourceAdapterMetaData {
  /**
   * Default constructor
   */
  public VertxRaMetaData() {

  }

  /**
   * Gets the version of the resource adapter.
   *
   * @return String representing version of the resource adapter
   */
  @Override
  public String getAdapterVersion() {
    return "1.0";
  }

  /**
   * Gets the name of the vendor that has provided the resource adapter.
   *
   * @return String representing name of the vendor
   */
  @Override
  public String getAdapterVendorName() {
    return "Red Hat";
  }

  /**
   * Gets a tool displayable name of the resource adapter.
   *
   * @return String representing the name of the resource adapter
   */
  @Override
  public String getAdapterName() {
    return "Vertx Resource Adapter";
  }

  /**
   * Gets a tool displayable short desription of the resource adapter.
   *
   * @return String describing the resource adapter
   */
  @Override
  public String getAdapterShortDescription() {
    return "Vertx Resource Adapter";
  }

  /**
   * Returns a string representation of the version
   *
   * @return String representing the supported version of the connector
   *         architecture
   */
  @Override
  public String getSpecVersion() {
    return "1.6";
  }

  /**
   * Returns an array of fully-qualified names of InteractionSpec
   *
   * @return Array of fully-qualified class names of InteractionSpec classes
   */
  @Override
  public String[] getInteractionSpecsSupported() {
    return null;
  }

  /**
   * Returns true if the implementation class for the Interaction
   *
   * @return boolean Depending on method support
   */
  @Override
  public boolean supportsExecuteWithInputAndOutputRecord() {
    return false;
  }

  /**
   * Returns true if the implementation class for the Interaction
   *
   * @return boolean Depending on method support
   */
  @Override
  public boolean supportsExecuteWithInputRecordOnly() {
    return false;
  }

  /**
   * Returns true if the resource adapter implements the LocalTransaction
   *
   * @return true If resource adapter supports resource manager local
   *         transaction demarcation
   */
  @Override
  public boolean supportsLocalTransactionDemarcation() {
    return false;
  }

}
