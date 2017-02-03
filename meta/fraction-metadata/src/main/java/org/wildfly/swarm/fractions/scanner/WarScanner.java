package org.wildfly.swarm.fractions.scanner;

/**
 * @author Ken Finnigan
 */
public class WarScanner implements Scanner {
    @Override
    public String extension() {
        return "war";
    }
}
