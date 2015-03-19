
# Wildfly-Boot

Wildfly-Boot aims to provide a mechanism for building
applications as *fat jars*, with just enough of the
Wildfly application server wrapped around it to support
each application's use-case.

# Project Configuration

In a normal WAR-based maven `pom.xml`, simply add the following

    <plugin>
      <groupId>org.wildfly.boot</groupId>
      <artifactId>wildfly-boot-plugin</artifactId>
      <version>${version.wildfly-boot}</version>
      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>create</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

This will take the `.war` file normally created by your build, and wrap
it in the wildfly-boot mechanisms.

If you normally produce `myapp-1.0.war`, in your `target/` directory will
then also be present a `myapp-1.0-boot.jar`.

In order to specify the portions of the Wildfly AS your application needs,
your `pom.xml` should specify some of the following dependencies within
the `org.wildfly.boot` Maven group-id:

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

    import org.wildfly.boot.container.Container;
    import org.wildfly.boot.container.SocketBindingGroup;
    import org.wildfly.boot.logging.LoggingSubsystem;
    import org.wildfly.boot.undertow.UndertowSubsystem;
    
    public class MyMain {
    
        public static void main(String[] args) {
            new Container()
                .subsystem( new LoggingSubsystem()... 
                )
                .subsystem( new UndertowSubsystem()... 
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

    import org.wildfly.boot.container.Container;
    import org.wildfly.boot.logging.LoggingSubsystem;

    public class MyMain {
    
        public static void main(String[] args) {
            new Container()
                .subsystem( new LoggingSubsystem()...
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

# Running the `-boot.jar`

The resulting `-boot.jar` is fully self-contained and can be executed using a
`java -jar ...` commandline such as:

    java -jar myapp-1.0-boot.jar


