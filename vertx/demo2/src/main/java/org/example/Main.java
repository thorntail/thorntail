package org.example;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.vertx.VertxFraction;

public class Main {

	public static void main(String[] args) throws Exception {
		Swarm swarm = new Swarm();
		swarm.start();
		swarm.deploy();
	}
}