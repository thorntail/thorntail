[![Build Status](https://ci.wildfly-swarm.io/buildStatus/icon?job=wildfly-swarm-linux)](https://ci.wildfly-swarm.io/job/wildfly-swarm-linux)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.wildfly.swarm/wildfly-swarm/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.wildfly.swarm/wildfly-swarm)
[![Join the chat at freenode:thorntail](https://img.shields.io/badge/irc-freenode%3A%20%23thorntail-blue.svg)](http://webchat.freenode.net/?channels=%23thorntail)

We rely on the [Java profiler from EJ Technologies](http://www.ej-technologies.com/products/jprofiler/overview.html)
![Supported by JProfiler](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png)

![Thorntail: Rightsize your JavaEE Applications](http://wildfly-swarm.io/images/swarm_logo_final.png)

> Issues for v2/master are being tracked using the [JBoss issue tracking system](https://issues.jboss.org/projects/THORN/issues?filter=allopenissues) (JIRA).
> Issues for v4 are being tracked in GitHub Issues.
> Bug reports and feature requests are greatly appreciated.

# Thorntail Core

Thorntail provides a mechanism for building
applications as *uber jars*, with just enough of the
WildFly application server wrapped around it to support
each application's use-case.

> Note: Thorntail requires Maven 3.2.5 or higher for building your application.

> Note: Thorntail requires JDK 8 or higher for building your application
> or for building WildFly Swarm itself.

# Project Configuration

In a normal WAR-based maven `pom.xml`, simply add the following
```xml
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
```

This will take the `.war` file normally created by your build, and wrap
it in the wildfly-swarm mechanisms.

If you normally produce `myapp-1.0.war`, in your `target/` directory will
then also be present a `myapp-1.0-swarm.jar`.

In order to specify the portions of the WildFly AS your application needs,
your `pom.xml` should specify some of the following dependencies within
the `org.wildfly.swarm` Maven group-id:

* bean-validation
* cdi
* ee
* io
* jaxrs
* logging
* naming
* request-controller
* security
* transactions
* undertow
* _and many more!_

# How To Build WildFly Swarm Itself

Thorntail attempts to be a well-behaved Maven project. To install to your local repository for usage:
```bash
mvn clean install
```

If you're running short on time:

```bash
mvn clean install -DskipTests
```

# Documentation

For a more complete set of documentation, go to the [WildFly Swarm User's
Guide](https://wildfly-swarm.gitbooks.io/wildfly-swarm-users-guide/).

# Community

* We hang out in `#thorntail` on irc.freenode.net.
