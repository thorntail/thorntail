/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.swarm.microprofile.metrics.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.metrics.MetricType;
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

            return readConfig(configStream, new ArrayList<>());
        } catch (FileNotFoundException e) {
            log.warn("No configuration found");
        } catch (ParserException pe) {
            log.error(pe);
        }
        return null;
    }

    public MetadataList readConfig(InputStream configStream, List<Tag> globalTags) {

      Yaml yaml = new Yaml();
      YMetadataList ymlConfig = yaml.loadAs(configStream, YMetadataList.class);

      MetadataList config = new MetadataList();

      List<ExtendedMetadata> metadataList = getExtendedMetadata(globalTags, ymlConfig.base);
      config.setBase(metadataList);
      metadataList = getExtendedMetadata(globalTags, ymlConfig.vendor);
      config.setVendor(metadataList);

      log.info("Loaded config");
      return config;
    }

  private List<ExtendedMetadata> getExtendedMetadata(List<Tag> globalTags, List<YMetadata> ymlConfig) {
    List<ExtendedMetadata> metadataList = new ArrayList<>();
    for (YMetadata ym : ymlConfig) {
      Map<String,String> labels = new HashMap<>();
      if (ym.getLabels() != null) {
          for (Map<String,String> l : ym.getLabels()) {
              labels.put(l.get("key"),l.get("value"));
          }
      }
      // Also add the global tags.
      for (Tag tag : globalTags) {
        labels.put(tag.key,tag.value);
      }

      ExtendedMetadata em = new ExtendedMetadata(ym.getName(),
                                                 ym.getDisplayName(),
                                                 ym.getDescription(),
                                                 MetricType.from(ym.getType()),
                                                 ym.getUnit(),
                                                 labels
      );
      em.setMbean(ym.mbean);
      em.setMulti(ym.multi);

      metadataList.add(em);

    }
    return metadataList;
  }


}
