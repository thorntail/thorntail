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
package org.wildfly.swarm.microprofile_metrics.runtime.exporters;

import java.util.ArrayList;
import java.util.List;
import org.wildfly.swarm.microprofile_metrics.runtime.Tag;

/**
 * @author hrupp
 */
public abstract class AbstractExporter implements Exporter {

  private List<Tag> tags = new ArrayList<>();

  @Override
  public void setGlobalTags(String tagsString) {

    String[] singleTags = tagsString.split(",");
        for (String singleTag : singleTags) {
          addTag(singleTag.trim());
        }
  }

  public void addTag(String kvString) {
     if (kvString == null || kvString.isEmpty() || !kvString.contains("=")) {
       return;
     }
     tags.add(new Tag(kvString));
   }

  public List<Tag> getTags() {
    return tags;
  }
}
