package org.wildfly.swarm.transactions.detect;

import org.wildfly.swarm.spi.meta.PackageFractionDetector;

/**
 * @author Ken Finnigan
 */
public class TransactionsPackageDetector extends PackageFractionDetector {

    public TransactionsPackageDetector() {
        anyPackageOf("javax.transaction");
    }

    @Override
    public String artifactId() {
        return "transactions";
    }
}
