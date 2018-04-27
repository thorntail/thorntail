package io.thorntail.concurrency.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class ThreadFactoryProducer {

    @Produces
    @Dependent
    ThreadFactory createExecutor() {
        return Executors.defaultThreadFactory();
    }
}
