# JAX-RS & Messaging

This examples uses JAX-RS resource implementations and deploys
them through a user-provided `main()` programatically without
construction a `.war` file during the build.

Additionally, it configures a JMS server and sets up some
JMS destinations for use by the JAX-RS resource.

It also deploys an MSC service to consume messages from
the destination.  

> Please raise any issues found with this example in this repo:
> https://github.com/wildfly-swarm/wildfly-swarm-examples
>
> Issues related to WildFly Swarm core should be raised in the main repo:
> https://github.com/wildfly-swarm/wildfly-swarm/issues

## Project `pomx.xml`

The project is a normal maven project with `jar` packaging, not `war`.

    <packaging>jar</packaging>

The project adds a `<plugin>` to configure `wildfly-swarm-plugin` to
create the runnable `.jar`.

    <plugin>
      <groupId>org.wildfly.swarm</groupId>
      <artifactId>wildfly-swarm-plugin</artifactId>
      <version>${version.wildfly-swarm}</version>
      <configuration>
        <mainClass>org.wildfly.swarm.examples.messaging.Main</mainClass>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>package</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

To define the needed parts of WildFly Swarm, some dependencies are added

    <dependency>
        <groupId>org.wildfly.swarm</groupId>
        <artifactId>wildfly-swarm-jaxrs</artifactId>
        <version>${version.wildfly-swarm}</version>
    </dependency>
    <dependency>
        <groupId>org.wildfly.swarm</groupId>
        <artifactId>wildfly-swarm-messaging</artifactId>
        <version>${version.wildfly-swarm}</version>
    </dependency>
    <dependency>
        <groupId>org.wildfly.swarm</groupId>
        <artifactId>wildfly-swarm-msc</artifactId>
        <version>${version.wildfly-swarm}</version>
    </dependency>

## Project `main()`

This project supplies a `main()` in order to configure the messaging
subsystem and deploy all the pieces of the application.

    package org.wildfly.swarm.examples.messaging;
    
    import org.wildfly.swarm.container.Container;
    import org.wildfly.swarm.jaxrs.JAXRSDeployment;
    import org.wildfly.swarm.messaging.MessagingFraction;
    import org.wildfly.swarm.messaging.MessagingServer;
    import org.wildfly.swarm.msc.ServiceActivatorDeployment;
    
    /**
     * @author Bob McWhirter
     */
    public class Main {
    
        public static void main(String[] args) throws Exception {
            Container container = new Container();
    
            container.subsystem(new MessagingFraction()
                            .server(
                                    new MessagingServer()
                                            .enableInVMConnector()
                                            .topic("my-topic")
                                            .queue("my-queue")
                            )
            );
    
            container.start();
    
            JAXRSDeployment appDeployment = new JAXRSDeployment(container);
            appDeployment.addResource(MyResource.class);
    
            container.deploy(appDeployment);
    
            ServiceActivatorDeployment deployment = new ServiceActivatorDeployment(container);
            deployment.addServiceActivator( MyServiceActivator.class );
            deployment.addClass( MyService.class );
    
            container.deploy( deployment );
        }
    }

After the container is instantiated, the Messaging fraction is
configured and installed, enabling the in-vm connector and setting
up a topic and a queue.  

The container is started.

A JAX-RS deployment based on a project class is deployed, as is the
MSC service-activator, which activates a service to consume messages.

You can run it many ways:

* mvn package && java -jar ./target/wildfly-swarm-example-messaging-swarm.jar
* mvn wildfly-swarm:run
* In your IDE run the `org.wildfly.swarm.examples.messaging.Main` class

## Use

    http://localhost:8080/

On the console the MSC service will print the message it received over JMS

    2015-05-04 14:05:11,457 ERROR [stderr] (Thread-2 (HornetQ-client-global-threads-316630753)) received: Hello!
