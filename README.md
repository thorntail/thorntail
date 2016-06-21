# WildFly Swarm - Vert.x

[![Build Status](https://projectodd.ci.cloudbees.com/buildStatus/icon?job=wildfly-swarm-vertx)](https://projectodd.ci.cloudbees.com/job/wildfly-swarm-vertx)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.wildfly.swarm/vertx/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.wildfly.swarm/vertx)

This fraction adds support for the Vert.x API in Java EE applications by enabling the [Vert.x JCA adapter](https://github.com/vert-x3/vertx-jca).

## Enable Vert.x

````xml 
<dependency>
    <groupId>org.wildfly.swarm</groupId>
    <artifactId>vertx</artifactId>
</dependency>
````

Overview
--------

The general purpose of a JCA resource adapter is to provide connectivity to an Enterprise Information System (EIS) from a Java EE application server. Specifically, the Vert.x JCA adapter provides both outbound and inbound connectivity with a Vert.x instance.

Outbound Connectivity
---------------------

An application component (e.g Servlet, EJB), can send messages to a Vert.x instance.

Usage:

````java

@Resource(mappedName="java:/eis/VertxConnectionFactory")
VertxConnectionFactory connFactory;

public void sendMessage() throws Exception { 
    try (VertxConnection conn = connFactory.getVertxConnection()) {
        conn.vertxEventBus().send("tacos", "Hello from JCA");
    }
}
````

   * NOTE: as with any JCA resource, always call the close() method when your work is complete to allow the connection to be returned to the pool. This will **not** close the underly Vert.x instance. Please see the JCA specification for my details.

Inbound Connectivity
--------------------

Since the JCA 1.5 specification, inbound connectivity is provided via a listener interface which can be implemented by a Java EE Message Driven Bean (MDB). As opposed to the default JMS listener type, the Vert.x JCA listener interface allows an MDB to receive messages from a Vert.x address.

The endpoint of the MDB implements interface: <b>io.vertx.resourceadapter.inflow.VertxListener</b>.

````java
package io.vertx.resourceadapter.examples.mdb;

import io.vertx.resourceadapter.inflow.VertxListener;
import io.vertx.core.eventbus.Message;

import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import org.jboss.ejb3.annotation.ResourceAdapter;


@MessageDriven(name = "VertxMonitor",
        messageListenerInterface = VertxListener.class,
        activationConfig = {
                @ActivationConfigProperty(propertyName = "address", propertyValue = "tacos"),
                @ActivationConfigProperty(propertyName = "clusterHost", propertyValue = "localhost"),
                @ActivationConfigProperty(propertyName = "clusterPort", propertyValue = "0"),
})
@ResourceAdapter("vertx-ra")
public class VertxMonitor implements VertxListener {

   private static final Logger logger = Logger.getLogger(VertxMonitor.class.getName());

    /**
     * Default constructor.
     */
    public VertxMonitor() {
        logger.info("VertxMonitor started.");
    }

   @Override
   public <T> void onMessage(Message<T> message) {
      logger.info("Get a message from Vert.x at address: " + message.address());

      T body = message.body();

      if (body != null) {
         logger.info("Body of the message: " + body.toString());
      }
   }
}

````
