/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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
 */
package io.thorntail.metrics.impl.jmx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricType;

/**
 * @author hrupp
 */
public class MBeanMetadata extends Metadata {

    private String mbean;

    boolean multi;

    public MBeanMetadata() {
        super("-dummy-", MetricType.INVALID);
    }

    public MBeanMetadata(String name, MetricType type) {
        super(name, type);
    }

    public MBeanMetadata(String name, String displayName, String description, MetricType typeRaw, String unit) {
        super(name, displayName, description, typeRaw, unit);
    }

    public String getMbean() {
        return mbean;
    }

    public void setMbean(String mbean) {
        this.mbean = mbean;
    }

    public boolean isMulti() {
        return multi;
    }

    public void setMulti(boolean multi) {
        this.multi = multi;
    }

    public void setLabels(List<Tag> in) {
        for (Tag tag : in) {
            addTag(tag.toKVString());
        }
    }

    public List<Tag> getLabels() {
        List<Tag> out = new ArrayList<>(getTags().size());
        for (Map.Entry<String, String> entity : getTags().entrySet()) {
            Tag t = new Tag(entity.getKey(), entity.getValue());
            out.add(t);
        }
        return out;
    }

    public void processTags(List<Tag> globalTags) {

        List<Tag> tags = new ArrayList<>(globalTags);
        tags.addAll(getLabels());
        for (Tag tag : tags) {
            getTags().put(tag.key, tag.value);
        }
    }
}
