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

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Startup
@Singleton
public class StartBatchJob {

    @PostConstruct
    public void startInitialBatchJob() {
        Logger.getLogger(StartBatchJob.class).info("In PostConstruct");
        final JobOperator jobOperator = BatchRuntime.getJobOperator();
        final Properties jobProperties = new Properties();
        jobProperties.setProperty("chunk.start", "0");
        jobProperties.setProperty("chunk.end", "10");

        jobOperator.start("simple", jobProperties);
    }
}
