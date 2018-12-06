package org.wildfly.swarm.microprofile.faulttolerance.deployment;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import org.jboss.logging.Logger;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;

/**
 * Copy of SmallRye's {@code DefaultHystrixConcurrencyStrategy} that uses a different JNDI name.
 * Looking up {@code java:comp/DefaultManagedThreadFactory} requires the EE naming context,
 * while looking up {@code java:jboss/ee/concurrency/factory/default} doesn't.
 * Serves as a workaround for https://issues.jboss.org/browse/WFLY-11373.
 */
@Priority(1000)
@Alternative
@Dependent
public class ThorntailHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    private static final Logger LOGGER = Logger.getLogger(ThorntailHystrixConcurrencyStrategy.class);

    @Resource(lookup = "java:jboss/ee/concurrency/factory/default")
    ManagedThreadFactory managedThreadFactory;

    ThreadFactory threadFactory;

    @PostConstruct
    public void initTreadManagerFactory() {
        if (managedThreadFactory != null) {
            threadFactory = managedThreadFactory;
            LOGGER.info("### Managed Thread Factory used ###");
        } else {
            threadFactory = Executors.privilegedThreadFactory();
            LOGGER.info("### Privilleged Thread Factory used ###");
        }
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixProperty<Integer> corePoolSize, HystrixProperty<Integer> maximumPoolSize,
                                            HystrixProperty<Integer> keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        int dynamicCoreSize = corePoolSize.get();
        int dynamicMaximumSize = maximumPoolSize.get();

        LOGGER.debugf("Get thread pool executor for %s [core: %s, max: %s]", threadPoolKey.name(), dynamicCoreSize, dynamicMaximumSize);

        return new ThreadPoolExecutor(dynamicCoreSize, dynamicCoreSize > dynamicMaximumSize ? dynamicCoreSize : dynamicMaximumSize, keepAliveTime.get(), unit,
                workQueue, threadFactory);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolProperties threadPoolProperties) {

        boolean allowMaximumSizeToDivergeFromCoreSize = threadPoolProperties.getAllowMaximumSizeToDivergeFromCoreSize().get();
        int dynamicCoreSize = threadPoolProperties.coreSize().get();
        int dynamicMaximumSize = threadPoolProperties.maximumSize().get();
        int keepAliveTime = threadPoolProperties.keepAliveTimeMinutes().get();
        int maxQueueSize = threadPoolProperties.maxQueueSize().get();
        BlockingQueue<Runnable> workQueue = getBlockingQueue(maxQueueSize);

        LOGGER.debugf("Get thread pool executor for %s [allowMaximumSizeToDivergeFromCoreSize: %s, core: %s, max: %s]", threadPoolKey.name(), allowMaximumSizeToDivergeFromCoreSize,
                dynamicCoreSize, dynamicMaximumSize);

        if (allowMaximumSizeToDivergeFromCoreSize) {
            return new ThreadPoolExecutor(dynamicCoreSize, dynamicCoreSize > dynamicMaximumSize ? dynamicCoreSize : dynamicMaximumSize, keepAliveTime,
                    TimeUnit.MINUTES, workQueue, threadFactory);
        } else {
            return new ThreadPoolExecutor(dynamicCoreSize, dynamicCoreSize, keepAliveTime, TimeUnit.MINUTES, workQueue, threadFactory);
        }
    }

}
