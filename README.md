# Wildfly Swarm SPI

[![Build Status](https://projectodd.ci.cloudbees.com/buildStatus/icon?job=wildfly-swarm-spi)](https://projectodd.ci.cloudbees.com/job/wildfly-swarm-spi/)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.wildfly.swarm/spi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.wildfly.swarm/spi)

SPI that defines what the WildFly Swarm container implements and what fractions can call in the expectation that the container will execute it. This is needed to prevent fractions depending on the container directly, making inter dependencies complicated.

