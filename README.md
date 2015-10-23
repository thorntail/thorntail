
# WildFly Swarm

WildFly Swarm provides a mechanism for building
applications as *uber jars*, with just enough of the
WildFly application server wrapped around it to support
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

In order to specify the portions of the WildFly AS your application needs,
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
* _and many more!_

# Server Configuration

## Defaults

Each module above can provide a default configuration.   If you are satisfied
with the default configuration, no changes to your project's source files are
required.

# Running the `-swarm.jar`

The resulting `-swarm.jar` is fully self-contained and can be executed using a
`java -jar ...` commandline such as:

    java -jar myapp-1.0-swarm.jar


# Documentation

For a more complete set of documentation, go to the [WildFly Swarm User's
Guide](https://wildfly-swarm.gitbooks.io/wildfly-swarm-users-guide/).

# Community

* We hang out in `#wildfly-swarm` on irc.freenode.net.
* Logs can be found [here](http://transcripts.jboss.org/channel/irc.freenode.org/%23wildfly-swarm/)
