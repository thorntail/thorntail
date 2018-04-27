/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.batch.jberet;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.wildfly.swarm.config.BatchJBeret;
import org.wildfly.swarm.config.batch.jberet.InMemoryJobRepository;
import org.wildfly.swarm.config.batch.jberet.JDBCJobRepository;
import org.wildfly.swarm.config.batch.jberet.ThreadPool;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * A batch (JSR-352) fraction implemented by JBeret.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@WildFlyExtension(module = "org.wildfly.extension.batch.jberet")
@MarshalDMR
public class BatchFraction extends BatchJBeret<BatchFraction> implements Fraction<BatchFraction> {
    public static final String DEFAULT_JOB_REPOSITORY_NAME = "in-memory";

    public static final String DEFAULT_THREAD_POOL_NAME = "batch";

    public BatchFraction() {

    }

    @PostConstruct
    public void postConstruct() {
        applyDefaults();
    }

    /**
     * Creates a default batch fraction.
     * <p>
     * Uses an {@code in-memory} job repository with the {@linkplain #DEFAULT_JOB_REPOSITORY_NAME default name}.
     * </p>
     *
     * <p>
     * Uses a default thread-pool with a calculated maximum number of threads based on the available number of processors. A
     * keep alive time of 30 seconds is used for the thread-pool.
     * </p>
     *
     * @return a new default batch fraction
     */
    public static BatchFraction createDefaultFraction() {
        return new BatchFraction().applyDefaults();
    }

    public BatchFraction applyDefaults() {
        final InMemoryJobRepository<?> jobRepository = new InMemoryJobRepository<>(DEFAULT_JOB_REPOSITORY_NAME);

        // Default thread-pool
        final ThreadPool<?> threadPool = new ThreadPool<>(DEFAULT_THREAD_POOL_NAME);
        threadPool.maxThreads(10)
                .keepaliveTime("time", 30L)
                .keepaliveTime("unit", "SECONDS");

        return inMemoryJobRepository(jobRepository)
                .defaultJobRepository(jobRepository.getKey())
                .threadPool(threadPool)
                .defaultThreadPool(threadPool.getKey());
    }

    /**
     * Adds the in-memory job repository as the default job repository.
     *
     * @param jobRepository the job repository to use as the default
     * @return this fraction
     */
    public BatchFraction defaultJobRepository(final InMemoryJobRepository<?> jobRepository) {
        return defaultJobRepository(jobRepository.getKey());
    }

    /**
     * Adds the JDBC job repository as the default job repository.
     *
     * @param jobRepository the job repository to use as the default
     * @return this fraction
     */
    public BatchFraction defaultJobRepository(final JDBCJobRepository<?> jobRepository) {
        return defaultJobRepository(jobRepository.getKey());
    }

    /**
     * Adds a new JDBC job repository using the datasource name as the job repository name and sets it as the default job
     * repository.
     *
     * @param datasource the datasource to use to connect to the database
     * @return this fraction
     */
    public BatchFraction defaultJobRepository(final DatasourcesFraction datasource) {
        return defaultJobRepository(datasource.getKey(), datasource);
    }

    /**
     * Adds a new JDBC job repository and sets it as the default job repository.
     *
     * @param name       the name for the JDBC job repository
     * @param datasource the datasource to use to connect to the database
     * @return this fraction
     */
    public BatchFraction defaultJobRepository(final String name, final DatasourcesFraction datasource) {
        jdbcJobRepository(name, datasource);
        return defaultJobRepository(name);
    }

    /**
     * Creates a new JDBC job repository using the name of the datasource for the job repository name.
     *
     * @param datasource the datasource to use to connect to the database
     * @return this fraction
     */
    public BatchFraction jdbcJobRepository(final DatasourcesFraction datasource) {
        return jdbcJobRepository(datasource.getKey(), datasource);
    }

    /**
     * Creates a new JDBC job repository.
     *
     * @param name       the name for the job repository
     * @param datasource the datasource to use to connect to the database
     * @return this fraction
     */
    public BatchFraction jdbcJobRepository(final String name, final DatasourcesFraction datasource) {
        return jdbcJobRepository(new JDBCJobRepository<>(name).dataSource(datasource.getKey()));
    }

    /**
     * Creates a new thread-pool using the {@linkplain #DEFAULT_THREAD_POOL_NAME default name}. The thread-pool is then set as
     * the default thread-pool for bath jobs.
     *
     * @param maxThreads     the maximum number of threads to set the pool to
     * @param keepAliveTime  the time to keep threads alive
     * @param keepAliveUnits the time unit for the keep alive time
     * @return this fraction
     */
    public BatchFraction defaultThreadPool(final int maxThreads, final int keepAliveTime, final TimeUnit keepAliveUnits) {
        return defaultThreadPool(DEFAULT_THREAD_POOL_NAME, maxThreads, keepAliveTime, keepAliveUnits);
    }

    /**
     * Creates a new thread-pool and sets the created thread-pool as the default thread-pool for batch jobs.
     *
     * @param name           the maximum number of threads to set the pool to
     * @param keepAliveTime  the time to keep threads alive
     * @param keepAliveUnits the time unit for the keep alive time
     * @return this fraction
     */
    public BatchFraction defaultThreadPool(final String name, final int maxThreads, final int keepAliveTime, final TimeUnit keepAliveUnits) {
        threadPool(name, maxThreads, keepAliveTime, keepAliveUnits);
        return defaultThreadPool(name);
    }

    /**
     * Creates a new thread-pool using the {@linkplain #DEFAULT_THREAD_POOL_NAME default name} that can be used for batch jobs.
     *
     * @param keepAliveTime  the time to keep threads alive
     * @param keepAliveUnits the time unit for the keep alive time
     * @return this fraction
     */
    public BatchFraction threadPool(final int maxThreads, final int keepAliveTime, final TimeUnit keepAliveUnits) {
        return threadPool(DEFAULT_THREAD_POOL_NAME, maxThreads, keepAliveTime, keepAliveUnits);
    }

    /**
     * Creates a new thread-pool that can be used for batch jobs.
     *
     * @param name           the maximum number of threads to set the pool to
     * @param keepAliveTime  the time to keep threads alive
     * @param keepAliveUnits the time unit for the keep alive time
     * @return this fraction
     */
    public BatchFraction threadPool(final String name, final int maxThreads, final int keepAliveTime, final TimeUnit keepAliveUnits) {
        final ThreadPool<?> threadPool = new ThreadPool<>(name);
        threadPool.maxThreads(maxThreads)
                .keepaliveTime("time", Integer.toBinaryString(keepAliveTime))
                .keepaliveTime("unit", keepAliveUnits.name().toLowerCase(Locale.ROOT));
        return threadPool(threadPool);
    }
}
