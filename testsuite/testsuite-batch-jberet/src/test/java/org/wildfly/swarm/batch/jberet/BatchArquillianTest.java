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
import java.util.concurrent.TimeUnit;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.JobExecution;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
@DefaultDeployment
public class BatchArquillianTest {

    @Test
    public void testChunk() {
        final JobOperator jobOperator = BatchRuntime.getJobOperator();
        final Properties jobProperties = new Properties();
        jobProperties.setProperty("chunk.start", "0");
        jobProperties.setProperty("chunk.end", "10");

        final long id = jobOperator.start("simple", jobProperties);

        // Wait for the job to complete
        waitForCompletion(id, 5, TimeUnit.SECONDS);
    }

    private void waitForCompletion(final long executionId, final int timeout, final TimeUnit unit) {
        final JobExecution jobExecution = BatchRuntime.getJobOperator().getJobExecution(executionId);
        long time = unit.toMillis(timeout);
        long sleep = 100;
        boolean complete = false;
        while (!complete && time > 0) {
            switch (jobExecution.getBatchStatus()) {
                case STARTED:
                case STARTING:
                case STOPPING:
                    try {
                        TimeUnit.MILLISECONDS.sleep(sleep);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    time -= sleep;
                    sleep = Math.max(sleep / 2, 100L);
                    break;
                default:
                    complete = true;
                    break;
            }
        }
        Assert.assertTrue("Batch job did not complete withing allotted time.", complete);
    }

}
