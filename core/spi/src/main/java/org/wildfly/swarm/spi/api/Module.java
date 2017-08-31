/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
 * JBoss Modules module descriptor.
 *
 * <p>If a module is added to an archive, this descriptor is returned in order
 * to allow specific customization regarding exports, services, etc.</p>
 *
 * @author Ken Finnigan
 * @see JBossDeploymentStructureContainer#addModule(String)
 * @see JBossDeploymentStructureContainer#addModule(String, String)
 */
public class Module {

    Module(String name) {
        this(name, "main");
    }

    Module(String name, String slot) {
        this.name = name;
        if (slot == null) {
            slot = "main";
        }
        this.slot = slot;
    }

    /**
     * The name of the module.
     *
     * @return The name of the module.
     */
    public String name() {
        return this.name;
    }

    Module withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * The slot of the module.
     *
     * @return The slot of the module.
     */
    public String slot() {
        return this.slot;
    }

    Module withSlot(String slot) {
        this.slot = slot;
        return this;
    }

    /**
     * Determine if this module should be exported.
     *
     * @return {@code null} if undefined, otherwise {@code true} if the module is to be exported, otherwise {@code false}.
     */
    public Boolean export() {
        return this.export;
    }

    /**
     * Set the flag to determine if this module should be exported.
     *
     * @param export The flag.
     * @return this module descriptor.
     */
    public Module withExport(Boolean export) {
        this.export = export;
        return this;
    }

    /**
     * Retrieve the services flag.
     *
     * @return The services flag.
     */
    public ServiceHandling services() {
        return this.services;
    }

    /**
     * Set the services flag.
     *
     * @param services The services flag.
     * @return this module descriptor.
     */
    public Module withServices(ServiceHandling services) {
        this.services = services;
        return this;
    }

    /**
     * Determine if this module is considered optional.
     *
     * @return {@code null} if undefined, otherwise {@code true} if the module is optional, otherwise {@code false}.
     */
    public Boolean optional() {
        return this.optional;
    }

    /**
     * Set the optional flag.
     *
     * @param optional The optional flag.
     * @return this module.
     */
    public Module withOptional(Boolean optional) {
        this.optional = optional;
        return this;
    }

    /**
     * Retrieve the meta-inf disposition.
     *
     * @return The meta-inf disposition.
     */
    public String metaInf() {
        return this.metaInf;
    }

    /**
     * Set the meta-inf disposition.
     *
     * @param metaInf The meta-inf disposition.
     * @return this module.
     */
    public Module withMetaInf(String metaInf) {
        this.metaInf = metaInf;
        return this;
    }

    /**
     * Retrieve the list of paths imported from this module.
     *
     * @return The list of paths imported from this module.
     */
    public List<String> importIncludePaths() {
        return this.imports.get(INCLUDE);
    }

    /**
     * Retreive the list of paths excluded from importation from this module.
     *
     * @return The list of paths excluded from importation from this module.
     */
    public List<String> importExcludePaths() {
        return this.imports.get(EXCLUDE);
    }

    /**
     * Add a path to import from this module.
     *
     * @param path The path to add.
     * @return this module descriptor.
     */
    public Module withImportIncludePath(String path) {
        checkList(this.imports, INCLUDE);
        this.imports.get(INCLUDE).add(path);
        return this;
    }

    /**
     * Add a path to exclude from importing from this module.
     *
     * @param path The excluded path to add.
     * @return this module descriptor.
     */
    public Module withImportExcludePath(String path) {
        checkList(this.imports, EXCLUDE);
        this.imports.get(EXCLUDE).add(path);
        return this;
    }

    /**
     * Retrieve this list of paths exported from this module.
     *
     * @return The list of paths exported from this module.
     */
    public List<String> exportIncludePaths() {
        return this.exports.get(INCLUDE);
    }

    /**
     * Retrieve the list of paths excluded from exportation from this module.
     *
     * @return The list of paths excluded from exportation from this module.
     */
    public List<String> exportExcludePaths() {
        return this.exports.get(EXCLUDE);
    }

    /**
     * Add a path to export from this module.
     *
     * @param path The path to add.
     * @return this module descriptor.
     */
    public Module withExportIncludePath(String path) {
        checkList(this.exports, INCLUDE);
        this.exports.get(INCLUDE).add(path);
        return this;
    }

    /**
     * Add a path to exclude from exporting from this module.
     *
     * @param path The excluded path to add.
     * @return this module descriptor.
     */
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
