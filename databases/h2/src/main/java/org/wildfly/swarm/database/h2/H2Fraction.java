package org.wildfly.swarm.database.h2;

import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.DefaultFraction;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Ken Finnigan
 */
@Singleton
@DefaultFraction
public class H2Fraction implements Fraction {
}
