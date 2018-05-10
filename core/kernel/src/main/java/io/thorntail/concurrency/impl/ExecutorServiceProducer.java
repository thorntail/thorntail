package io.thorntail.concurrency.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

/**
 * @author George Gastaldi
 */
@ApplicationScoped
public class ExecutorServiceProducer {

    @Produces
    @Dependent
    ExecutorService createExecutor() {
        return ForkJoinPool.commonPool();
    }
}
