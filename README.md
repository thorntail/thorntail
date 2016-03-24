# Wildfly Swarm SPI

[![Build Status](https://projectodd.ci.cloudbees.com/buildStatus/icon?job=wildfly-swarm-spi)](https://projectodd.ci.cloudbees.com/job/wildfly-swarm-spi/)

SPI that defines what the WildFly Swarm container implements and what fractions can call in the expectation that the container will execute it. This is needed to prevent fractions depending on the container directly, making inter dependencies complicated.

