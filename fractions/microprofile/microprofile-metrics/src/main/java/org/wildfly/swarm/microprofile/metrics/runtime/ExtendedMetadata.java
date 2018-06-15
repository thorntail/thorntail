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
package org.wildfly.swarm.microprofile.metrics.runtime;

import java.util.Map;

import org.eclipse.microprofile.metrics.DefaultMetadata;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricType;

/**
 * @author hrupp
 */
public class ExtendedMetadata extends DefaultMetadata implements Metadata {

    private String mbean;
    boolean multi;


    ExtendedMetadata(String name, String displayName, String description, MetricType typeRaw, String unit,
                     Map<String, String> tags) {
        super(name, displayName, description, typeRaw, unit, false, tags);
    }

    String getMbean() {
        return mbean;
    }

    void setMbean(String mbean) {
        this.mbean = mbean;
    }

    boolean isMulti() {
        return multi;
    }

    public void setMulti(boolean multi) {
        this.multi = multi;
    }
}
