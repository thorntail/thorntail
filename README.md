
# WildFly Swarm Core

[![Build Status](https://projectodd.ci.cloudbees.com/buildStatus/icon?job=wildfly-swarm-core)](https://projectodd.ci.cloudbees.com/job/wildfly-swarm-core)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.wildfly.swarm/wildfly-swarm-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.wildfly.swarm/wildfly-swarm-plugin)

WildFly Swarm provides a mechanism for building
applications as *uber jars*, with just enough of the
WildFly application server wrapped around it to support
each application's use-case.

> Note: WildFly Swarm requires Maven 3.2.5 or higher for building your application.

> Note: WildFly Swarm requires JDK 8 or higher for building your application
> or for building WildFly Swarm itself.

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

# How To Build WildFly Swarm Itself

WildFly Swarm attempts to be a well-behaved Maven project. To install to your local repository for usage:

    mvn install

If you're running short on time:

    mvn install -DskipTests

# Issue Tracking

Issues are being tracked using the [JBoss issue tracking system](https://issues.jboss.org/secure/RapidBoard.jspa?rapidView=2972) (JIRA). Bug reports and feature requests are greatly appreciated.

# Documentation

For a more complete set of documentation, go to the [WildFly Swarm User's
Guide](https://wildfly-swarm.gitbooks.io/wildfly-swarm-users-guide/).

# Community

* We hang out in `#wildfly-swarm` on irc.freenode.net.
* Logs can be found [here](http://transcripts.jboss.org/channel/irc.freenode.org/%23wildfly-swarm/)



