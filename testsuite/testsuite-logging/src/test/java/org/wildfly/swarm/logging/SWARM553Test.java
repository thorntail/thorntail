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
package org.wildfly.swarm.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.junit.Assert.assertTrue;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
public class SWARM553Test {

    final static String logFile = System.getProperty("user.dir") + File.separator +
            "target" + File.separator +
            "Swarm553.log";

    @Deployment
    public static Archive deployment() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "empty.jar");
        archive.addPackage(SWARM553Test.class.getPackage());
        return archive;
    }

    @Test
    public void doLogging() throws FileNotFoundException {
        String message = "testing: " + UUID.randomUUID().toString();
        Logger logger = Logger.getLogger("br.org.sistemafieg.cliente");
        logger.info(message);
        assertTrue("File not found: " + logFile, new File(logFile).exists());

        BufferedReader reader = new BufferedReader(new FileReader(logFile));
        List<String> lines = reader.lines().collect(Collectors.toList());

        boolean found = false;

        for (String line : lines) {
            if (line.contains(message)) {
                found = true;
                break;
            }
        }

        assertTrue("Expected message " + message, found);


    }

    // Unable to remove as we're relying on a specific log file name in the test
    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        return new Swarm()
                .fraction(
                        new LoggingFraction().periodicSizeRotatingFileHandler("FILE", (h) -> {
                            h.level(Level.INFO)
                                    .append(true)
                                    .suffix(".yyyy-MM-dd")
                                    .rotateSize("30m")
                                    .enabled(true)
                                    .encoding("UTF-8")
                                    .maxBackupIndex(2);
                            Map<String, String> fileSpec = new HashMap<>();
                            fileSpec.put("path", logFile);
                            h.file(fileSpec);
                        }).logger("br.org.sistemafieg.cliente", (l) -> {
                            l.level(Level.INFO)
                                    .handler("FILE");
                        }));
    }
}
