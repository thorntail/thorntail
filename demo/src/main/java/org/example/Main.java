package org.example;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.logging.LoggingFraction;

public class Main {

	public static void main(String[] args) throws Exception {
		Swarm swarm = new Swarm();
		swarm.fraction(LoggingFraction.createDebugLoggingFraction());
		swarm.start();
		swarm.deploy();
	}
}