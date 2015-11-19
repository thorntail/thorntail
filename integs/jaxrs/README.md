# JAX-RS .war Example

This example takes a normal JAX-RS build, and wraps it into
a `-swarm` runnable jar.

> Please raise any issues found with this example in this repo:
> https://github.com/wildfly-swarm/wildfly-swarm-examples
>
> Issues related to WildFly Swarm core should be raised in the main repo:
> https://github.com/wildfly-swarm/wildfly-swarm/issues

## Project `pom.xml`

This project is a traditional JAX-RS project, with maven packaging
of `war` in the `pom.xml`

    <packaging>war</packaging>

The project adds a `<plugin>` to configure `wildfly-swarm-plugin` to
create the runnable `.jar`.

    <plugin>
      <groupId>org.wildfly.swarm</groupId>
      <artifactId>wildfly-swarm-plugin</artifactId>
      <version>${version.wildfly-swarm}</version>
      <executions>
        <execution>
          <goals>
            <goal>package</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

To define the needed parts of WildFly Swarm, a dependency is added

    <dependency>
        <groupId>org.wildfly.swarm</groupId>
        <artifactId>wildfly-swarm-jaxrs</artifactId>
        <version>${version.wildfly-swarm}</version>
    </dependency>

This dependency provides the JAX-RS APIs to your application, so the
project does *not* need to specify those.

Additional application dependencies (in this case `joda-time`) can be
specified and will be included in the normal `.war` construction and available
within the WildFly Swarm application `.jar`.

## Run

* mvn package && java -jar ./target/wildfly-swarm-example-jaxrs-swarm.jar
* mvn wildfly-swarm:run
* From your IDE, run class `org.wildfly.swarm.Swarm`

## Use

Since WildFly Swarm apps tend to support one deployment per executable, it
automatically adds a `jboss-web.xml` to the deployment if it doesn't already
exist.  This is used to bind the deployment to the root of the web-server,
instead of using the `.war`'s own name as the application context.

    http://localhost:8080/

Be aware that you will notice an exception in the logs when accessing the page.
This is simply an overly verbose message from WildFly that the 'favicon.ico' file couldn't be found.

