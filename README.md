
# Wildfly-Swarm

Wildfly-Swarm aims to provide a mechanism for building
applications as *fat jars*, with just enough of the
Wildfly application server wrapped around it to support
each application's use-case.

> Note: WildFly Swarm requires Maven 3.1.x or higher for building your application.

# Project Configuration

In a normal WAR-based maven `pom.xml`, simply add the following

    <plugin>
      <groupId>org.wildfly.swarm</groupId>
      <artifactId>wildfly-swarm-plugin</artifactId>
      <version>${version.wildfly-swarm}</version>
      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>package</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

This will take the `.war` file normally created by your build, and wrap
it in the wildfly-swarm mechanisms.

If you normally produce `myapp-1.0.war`, in your `target/` directory will
then also be present a `myapp-1.0-swarm.jar`.

In order to specify the portions of the Wildfly AS your application needs,
your `pom.xml` should specify some of the following dependencies within
the `org.wildfly.swarm` Maven group-id:

* bean-validation
* ee
* io
* jaxrs
* logging
* naming
* request-controller
* security
* transactions
* undertow
* weld

# Server Configuration

## Defaults

Each module above can provide a default configuration.   If you are satisfied
with the default configuration, no changes to your project's source files are
required.

## Customizing

### Write your own `main()`

If the default is unsatisfactory,
you may completely control the server configuration through a `main()` method in a class
specified through your `MANIFEST.MF` inside your `.war`.

    package org.mycompany.myapp;

    import org.wildfly.swarm.container.Container;
    import org.wildfly.swarm.container.SocketBindingGroup;
    import org.wildfly.swarm.logging.LoggingFraction;
    import org.wildfly.swarm.undertow.UndertowFraction;
    
    public class MyMain {
    
        public static void main(String[] args) {
            new Container()
                .subsystem( new LoggingFraction()...
                )
                .subsystem( new UndertowFraction()...
                )
                .socketBindingGroup( new SocketBindingGroup()... 
                )
                .start();
        }
    }

If you wish to simply override a portion of the default configuration, any
subsystem that is involved in your application but is otherwise not explicitly
configured will inherit the defaults.

For instance, if you application uses `jaxrs`, and you simply want to override the 
default logging configuration:

    package org.mycompany.myapp;

    import org.wildfly.swarm.container.Container;
    import org.wildfly.swarm.logging.LoggingFraction;

    public class MyMain {
    
        public static void main(String[] args) {
            new Container()
                .subsystem( new LoggingFraction()...
                )
                .start();
        }
    }

All other subsystems do not need to be configured if the defaults are satisfactory.

### Specify the main class in your `MANIFEST.MF`

To your POM, you need to add configuration of the `maven-war-plugin` to specify
the desired `Main-Class` inside your `.war.


      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <configuration>
          <archive>
            <manifest>
              <mainClass>com.mycompany.myapp.MyMain</addClasspath>
            </manifest>
          </archive>
        </configuration>
      </plugin>

# Running the `-swarm.jar`

The resulting `-swarm.jar` is fully self-contained and can be executed using a
`java -jar ...` commandline such as:

    java -jar myapp-1.0-swarm.jar



