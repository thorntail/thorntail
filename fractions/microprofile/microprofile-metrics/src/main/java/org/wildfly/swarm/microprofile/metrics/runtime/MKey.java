/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

import java.util.Objects;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricType;

/**
 * @author hrupp
 */
public class MKey {
    private String name;
    private MetricType type;

  public MKey(String name, MetricType type) {
    this.name = name;
    this.type = type;
  }

  public MKey(Metadata metadata) {
    this (metadata.getName(),metadata.getTypeRaw());
  }

  public String getName() {
    return name;
  }

  public MetricType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MKey mKey = (MKey) o;
    return Objects.equals(name, mKey.name) &&
        type == mKey.type;
  }

  @Override
  public int hashCode() {

    return Objects.hash(name, type);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("MKey{");
    sb.append("name='").append(name).append('\'');
    sb.append(", type=").append(type);
    sb.append('}');
    return sb.toString();
  }
}
