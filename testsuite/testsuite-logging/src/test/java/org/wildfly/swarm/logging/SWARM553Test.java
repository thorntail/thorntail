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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author Heiko Braun
 */
public class SWARM553Test {

    final static String logFile = System.getProperty("user.dir") + File.separator+
            "target"+File.separator+
            "Swarm553.log";

    @Test
    public void testPeriodicSizeRotatingFileHandler() throws Exception {

        Container container = new Container();
        container.fraction(customLoggingConfig());

        container.start();

        Logger logger  = Logger.getLogger("br.org.sistemafieg.cliente");
        logger.info("test message");

        JARArchive archive = ShrinkWrap.create(JARArchive.class, "empty.jar");
        archive.addPackage(SWARM553Test.class.getPackage());
        container.deploy(archive);

        // verify the log file exists
        Assert.assertTrue("File not found: "+logFile ,new File(logFile).exists());

        container.stop();
    }

    private static LoggingFraction customLoggingConfig() {

        System.out.println("log logFile: "+ logFile);

        LoggingFraction loggingFraction = new LoggingFraction()
                .periodicSizeRotatingFileHandler("FILE",(h)->{
                    h.level(Level.INFO)
                            .append(true)
                            .suffix(".yyyy-MM-dd")
                            .rotateSize("30m")
                            .enabled(true)
                            .encoding("UTF-8")
                            .maxBackupIndex(2);
                    Map<String,String> fileSpec = new HashMap<>();
                    fileSpec.put("path", logFile);
                    h.file(fileSpec);
                }).logger("br.org.sistemafieg.cliente",(l)->{
                    l.level(Level.INFO)
                            .handler("FILE");
                });
        return loggingFraction;
    }
}
