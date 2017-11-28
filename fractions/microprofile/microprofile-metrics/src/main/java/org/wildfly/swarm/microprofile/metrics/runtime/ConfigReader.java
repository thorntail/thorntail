/*
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * /
 */
package org.wildfly.swarm.microprofile.metrics.runtime;

import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.parser.ParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author hrupp
 */
public class ConfigReader {

    private static Logger log = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");

    public MetadataList readConfig(String mappingFile) {
        try {


            File file = new File(mappingFile);
            log.info("Loading mapping file from " + file.getAbsolutePath());
            InputStream configStream = new FileInputStream(file);

            return readConfig(configStream);
        } catch (FileNotFoundException e) {
            log.warn("No configuration found");
        } catch (ParserException pe) {
            log.error(pe);
        }
        return null;
    }

    public MetadataList readConfig(InputStream configStream) {

        Yaml yaml = new Yaml();

        MetadataList config = yaml.loadAs(configStream, MetadataList.class);
        log.info("Loaded config");
        return config;
    }


}
