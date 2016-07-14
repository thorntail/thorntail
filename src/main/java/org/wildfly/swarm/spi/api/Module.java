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
package org.wildfly.swarm.spi.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ken Finnigan
 */
public class Module {
    public Module(String name) {
        this(name, "main");
    }

    public Module(String name, String slot) {
        this.name = name;
        this.slot = slot;
    }

    public String name() {
        return this.name;
    }

    public Module withName(String name) {
        this.name = name;
        return this;
    }

    public String slot() {
        return this.slot;
    }

    public Module withSlot(String slot) {
        this.slot = slot;
        return this;
    }

    public Boolean export() {
        return this.export;
    }

    public Module withExport(Boolean export) {
        this.export = export;
        return this;
    }

    public ServiceHandling services() {
        return this.services;
    }

    public Module withServices(ServiceHandling services) {
        this.services = services;
        return this;
    }

    public Boolean optional() {
        return this.optional;
    }

    public Module withOptional(Boolean optional) {
        this.optional = optional;
        return this;
    }

    public String metaInf() {
        return this.metaInf;
    }

    public Module withMetaInf(String metaInf) {
        this.metaInf = metaInf;
        return this;
    }

    public List<String> importIncludePaths() {
        return this.imports.get(INCLUDE);
    }

    public List<String> importExcludePaths() {
        return this.imports.get(EXCLUDE);
    }

    public Module withImportIncludePath(String path) {
        checkList(this.imports, INCLUDE);
        this.imports.get(INCLUDE).add(path);
        return this;
    }

    public Module withImportExcludePath(String path) {
        checkList(this.imports, EXCLUDE);
        this.imports.get(EXCLUDE).add(path);
        return this;
    }

    public List<String> exportIncludePaths() {
        return this.exports.get(INCLUDE);
    }

    public List<String> exportExcludePaths() {
        return this.exports.get(EXCLUDE);
    }

    public Module withExportIncludePath(String path) {
        checkList(this.exports, INCLUDE);
        this.exports.get(INCLUDE).add(path);
        return this;
    }

    public Module withExportExcludePath(String path) {
        checkList(this.exports, EXCLUDE);
        this.exports.get(EXCLUDE).add(path);
        return this;
    }

    private void checkList(Map<String, List<String>> map, String key) {
        if (map.get(key) == null) {
            map.put(key, new ArrayList<>());
        }
    }

    private static final String INCLUDE = "include";

    private static final String EXCLUDE = "exclude";

    private String name;

    private String slot;

    private Boolean export;

    private ServiceHandling services;

    private Boolean optional = Boolean.FALSE;

    private String metaInf;

    private Map<String, List<String>> imports = new HashMap<>();

    private Map<String, List<String>> exports = new HashMap<>();

    public enum ServiceHandling {
        NONE("none"),
        IMPORT("import"),
        EXPORT("export");

        ServiceHandling(String value) {
            this.value = value;
        }

        private String value;

        public String value() {
            return this.value;
        }
    }
}
