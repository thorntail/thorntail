package io.thorntail.servlet.impl.undertow;

import java.io.IOException;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

/**
 * Created by bob on 2/5/18.
 */
@ApplicationScoped
public class XnioWorkerProducer {

    @Produces
    @Undertow
    XnioWorker xnioWorker() throws IOException {
        Xnio xnio = Xnio.getInstance(io.undertow.Undertow.class.getClassLoader());

        int ioThreads = this.ioThreads.orElseGet( ()-> Math.max(Runtime.getRuntime().availableProcessors(), 2));
        int workerThreads = this.workerThreads.orElseGet( ()-> ioThreads * 8);


        return xnio.createWorker(OptionMap.builder()
                                           .set(Options.WORKER_IO_THREADS, ioThreads)
                                           .set(Options.CONNECTION_HIGH_WATER, this.highWater)
                                           .set(Options.CONNECTION_LOW_WATER, this.lowWater)
                                           .set(Options.WORKER_TASK_CORE_THREADS, workerThreads)
                                           .set(Options.WORKER_TASK_MAX_THREADS, workerThreads)
                                           .set(Options.TCP_NODELAY, this.tcpNoDelay)
                                           .set(Options.CORK, this.cork)
                                           .getMap());

    }


    @Inject
    @ConfigProperty(name="undertow.io-threads")
    Optional<Integer> ioThreads;

    @Inject
    @ConfigProperty(name="undertow.worker-threads")
    Optional<Integer> workerThreads;

    @Inject
    @ConfigProperty(name="undertow.high-water")
    int highWater;

    @Inject
    @ConfigProperty(name="undertow.low-water")
    int lowWater;

    @Inject
    @ConfigProperty(name="undertow.tcp-nodelay")
    boolean tcpNoDelay;

    @Inject
    @ConfigProperty(name="undertow.cork")
    boolean cork;
}
