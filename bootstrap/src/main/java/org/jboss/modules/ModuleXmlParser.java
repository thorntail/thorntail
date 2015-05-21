/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.modules;

import static org.jboss.modules.xml.XmlPullParser.CDSECT;
import static org.jboss.modules.xml.XmlPullParser.COMMENT;
import static org.jboss.modules.xml.XmlPullParser.DOCDECL;
import static org.jboss.modules.xml.XmlPullParser.END_DOCUMENT;
import static org.jboss.modules.xml.XmlPullParser.END_TAG;
import static org.jboss.modules.xml.XmlPullParser.ENTITY_REF;
import static org.jboss.modules.xml.XmlPullParser.FEATURE_PROCESS_NAMESPACES;
import static org.jboss.modules.xml.XmlPullParser.IGNORABLE_WHITESPACE;
import static org.jboss.modules.xml.XmlPullParser.PROCESSING_INSTRUCTION;
import static org.jboss.modules.xml.XmlPullParser.START_DOCUMENT;
import static org.jboss.modules.xml.XmlPullParser.START_TAG;
import static org.jboss.modules.xml.XmlPullParser.TEXT;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarFile;

import org.jboss.modules.filter.MultiplePathFilterBuilder;
import org.jboss.modules.filter.PathFilter;
import org.jboss.modules.filter.PathFilters;
import org.jboss.modules.security.FactoryPermissionCollection;
import org.jboss.modules.security.ModularPermissionFactory;
import org.jboss.modules.security.PermissionFactory;
import org.jboss.modules.xml.MXParser;
import org.jboss.modules.xml.XmlPullParser;
import org.jboss.modules.xml.XmlPullParserException;

/**
 * A fast, validating module.xml parser.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author thomas.diesler@jboss.com
 */
public final class ModuleXmlParser {

    interface ResourceRootFactory {
        ResourceLoader createResourceLoader(final String rootPath, final String loaderPath, final String loaderName) throws IOException;
    }

    private ModuleXmlParser() {
    }

    private static final String MODULE_1_0 = "urn:jboss:module:1.0";
    private static final String MODULE_1_1 = "urn:jboss:module:1.1";
    private static final String MODULE_1_2 = "urn:jboss:module:1.2";
    private static final String MODULE_1_3 = "urn:jboss:module:1.3";

    private static final String E_MODULE = "module";
    private static final String E_ARTIFACT = "artifact";
    private static final String E_NATIVE_ARTIFACT = "native-artifact";
    private static final String E_DEPENDENCIES = "dependencies";
    private static final String E_RESOURCES = "resources";
    private static final String E_MAIN_CLASS = "main-class";
    private static final String E_RESOURCE_ROOT = "resource-root";
    private static final String E_PATH = "path";
    private static final String E_EXPORTS = "exports";
    private static final String E_IMPORTS = "imports";
    private static final String E_INCLUDE = "include";
    private static final String E_EXCLUDE = "exclude";
    private static final String E_INCLUDE_SET = "include-set";
    private static final String E_EXCLUDE_SET = "exclude-set";
    private static final String E_FILTER = "filter";
    private static final String E_SYSTEM = "system";
    private static final String E_PATHS = "paths";
    private static final String E_MODULE_ALIAS = "module-alias";
    private static final String E_MODULE_ABSENT = "module-absent";
    private static final String E_PROPERTIES = "properties";
    private static final String E_PROPERTY = "property";
    private static final String E_PERMISSIONS = "permissions";
    private static final String E_GRANT = "grant";

    private static final String A_NAME = "name";
    private static final String A_SLOT = "slot";
    private static final String A_EXPORT = "export";
    private static final String A_SERVICES = "services";
    private static final String A_PATH = "path";
    private static final String A_OPTIONAL = "optional";
    private static final String A_TARGET_NAME = "target-name";
    private static final String A_TARGET_SLOT = "target-slot";
    private static final String A_VALUE = "value";
    private static final String A_PERMISSION = "permission";
    private static final String A_ACTIONS = "actions";

    private static final String D_NONE = "none";
    private static final String D_IMPORT = "import";
    private static final String D_EXPORT = "export";

    static ModuleSpec parseModuleXml(final ModuleLoader moduleLoader, final ModuleIdentifier moduleIdentifier, final File root, final File moduleInfoFile, final AccessControlContext context) throws ModuleLoadException, IOException {
        final FileInputStream fis;
        try {
            fis = new FileInputStream(moduleInfoFile);
        } catch (FileNotFoundException e) {
            throw new ModuleLoadException("No module.xml file found at " + moduleInfoFile);
        }
        try {
            return parseModuleXml(new ResourceRootFactory() {
                public ResourceLoader createResourceLoader(final String rootPath, final String loaderPath, final String loaderName) throws IOException {
                    File file = new File(rootPath, loaderPath);
                    if (file.isDirectory()) {
                        return new FileResourceLoader(loaderName, file, context);
                    } else {
                        final JarFile jarFile = new JarFile(file, true);
                        return new JarFileResourceLoader(loaderName, jarFile);
                    }
                }
            }, root.getPath(), new BufferedInputStream(fis), moduleInfoFile.getPath(), moduleLoader, moduleIdentifier);
        } finally {
            StreamUtil.safeClose(fis);
        }
    }

    static ModuleSpec parseModuleXml(final ResourceRootFactory factory, final String rootPath, InputStream source, final String moduleInfoFile, final ModuleLoader moduleLoader, final ModuleIdentifier moduleIdentifier) throws ModuleLoadException, IOException {
        try {
            final MXParser parser = new MXParser();
            parser.setFeature(FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(source, null);
            return parseDocument(factory, rootPath, parser, moduleLoader, moduleIdentifier);
        } catch (XmlPullParserException e) {
            throw new ModuleLoadException("Error loading module from " + moduleInfoFile, e);
        } finally {
            StreamUtil.safeClose(source);
        }
    }

    protected static XmlPullParserException unexpectedContent(final XmlPullParser reader) {
        final String kind;
        switch (reader.getEventType()) {
            case CDSECT:
                kind = "cdata";
                break;
            case COMMENT:
                kind = "comment";
                break;
            case DOCDECL:
                kind = "document decl";
                break;
            case END_DOCUMENT:
                kind = "document end";
                break;
            case END_TAG:
                kind = "element end";
                break;
            case ENTITY_REF:
                kind = "entity ref";
                break;
            case PROCESSING_INSTRUCTION:
                kind = "processing instruction";
                break;
            case IGNORABLE_WHITESPACE:
                kind = "whitespace";
                break;
            case START_DOCUMENT:
                kind = "document start";
                break;
            case START_TAG:
                kind = "element start";
                break;
            case TEXT:
                kind = "text";
                break;
            default:
                kind = "unknown";
                break;
        }
        final StringBuilder b = new StringBuilder("Unexpected content of type '").append(kind).append('\'');
        if (reader.getName() != null) {
            b.append(" named '").append(reader.getName()).append('\'');
        }
        if (reader.getText() != null) {
            b.append(", text is: '").append(reader.getText()).append('\'');
        }
        return new XmlPullParserException(b.toString(), reader, null);
    }

    protected static XmlPullParserException endOfDocument(final XmlPullParser reader) {
        return new XmlPullParserException("Unexpected end of document", reader, null);
    }

    private static XmlPullParserException invalidModuleName(final XmlPullParser reader, final ModuleIdentifier expected) {
        return new XmlPullParserException("Invalid/mismatched module name (expected " + expected + ")", reader, null);
    }

    private static XmlPullParserException missingAttributes(final XmlPullParser reader, final Set<String> required) {
        final StringBuilder b = new StringBuilder("Missing one or more required attributes:");
        for (String attribute : required) {
            b.append(' ').append(attribute);
        }
        return new XmlPullParserException(b.toString(), reader, null);
    }

    private static XmlPullParserException unknownAttribute(final XmlPullParser parser, final int index) {
        final String namespace = parser.getAttributeNamespace(index);
        final String prefix = parser.getAttributePrefix(index);
        final String name = parser.getAttributeName(index);
        final StringBuilder eb = new StringBuilder("Unknown attribute \"");
        if (prefix != null) eb.append(prefix).append(':');
        eb.append(name);
        if (namespace != null) eb.append("\" from namespace \"").append(namespace);
        eb.append('"');
        return new XmlPullParserException(eb.toString(), parser, null);
    }

    private static XmlPullParserException unknownAttributeValue(final XmlPullParser parser, final int index) {
        final String namespace = parser.getAttributeNamespace(index);
        final String prefix = parser.getAttributePrefix(index);
        final String name = parser.getAttributeName(index);
        final StringBuilder eb = new StringBuilder("Unknown value \"");
        eb.append(parser.getAttributeValue(index));
        eb.append("\" for attribute \"");
        if (prefix != null && !prefix.isEmpty()) eb.append(prefix).append(':');
        eb.append(name);
        if (namespace != null && !namespace.isEmpty()) eb.append("\" from namespace \"").append(namespace);
        eb.append('"');
        return new XmlPullParserException(eb.toString(), parser, null);
    }

    private static void validateNamespace(final XmlPullParser reader) throws XmlPullParserException {
        switch (reader.getNamespace()) {
            case MODULE_1_0:
            case MODULE_1_1:
            case MODULE_1_2:
            case MODULE_1_3:
                break;
            default:
                throw unexpectedContent(reader);
        }
    }

    private static void assertNoAttributes(final XmlPullParser reader) throws XmlPullParserException {
        final int attributeCount = reader.getAttributeCount();
        if (attributeCount > 0) {
            throw unknownAttribute(reader, 0);
        }
    }

    private static void validateAttributeNamespace(final XmlPullParser reader, final int index) throws XmlPullParserException {
        if (!reader.getAttributeNamespace(index).isEmpty()) {
            throw unknownAttribute(reader, index);
        }
    }

    private static ModuleSpec parseDocument(final ResourceRootFactory factory, final String rootPath, XmlPullParser reader, final ModuleLoader moduleLoader, final ModuleIdentifier moduleIdentifier) throws XmlPullParserException, IOException {
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case START_DOCUMENT: {
                    return parseRootElement(factory, rootPath, reader, moduleLoader, moduleIdentifier);
                }
                case START_TAG: {
                    final String element = reader.getName();
                    switch (element) {
                        case E_MODULE: {
                            final ModuleSpec.Builder specBuilder = ModuleSpec.build(moduleIdentifier);
                            parseModuleContents(reader, factory, moduleLoader, moduleIdentifier, specBuilder, rootPath);
                            parseEndDocument(reader);
                            return specBuilder.create();
                        }
                        case E_MODULE_ALIAS: {
                            final ModuleSpec moduleSpec = parseModuleAliasContents(reader, moduleIdentifier);
                            parseEndDocument(reader);
                            return moduleSpec;
                        }
                        case E_MODULE_ABSENT: {
                            parseModuleAbsentContents(reader, moduleIdentifier);
                            return null;
                        }
                        default: {
                            throw unexpectedContent(reader);
                        }
                    }
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    private static ModuleSpec parseRootElement(final ResourceRootFactory factory, final String rootPath, final XmlPullParser reader, final ModuleLoader moduleLoader, final ModuleIdentifier moduleIdentifier) throws XmlPullParserException, IOException {
        assertNoAttributes(reader);
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case START_TAG: {
                    validateNamespace(reader);
                    final String element = reader.getName();
                    switch (element) {
                        case E_MODULE: {
                            final ModuleSpec.Builder specBuilder = ModuleSpec.build(moduleIdentifier);
                            parseModuleContents(reader, factory, moduleLoader, moduleIdentifier, specBuilder, rootPath);
                            parseEndDocument(reader);
                            return specBuilder.create();
                        }
                        case E_MODULE_ALIAS: {
                            final ModuleSpec moduleSpec = parseModuleAliasContents(reader, moduleIdentifier);
                            parseEndDocument(reader);
                            return moduleSpec;
                        }
                        case E_MODULE_ABSENT: {
                            parseModuleAbsentContents(reader, moduleIdentifier);
                            return null;
                        }
                        default: {
                            throw unexpectedContent(reader);
                        }
                    }
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    private static ModuleSpec parseModuleAliasContents(final XmlPullParser reader, final ModuleIdentifier moduleIdentifier) throws XmlPullParserException, IOException {
        final int count = reader.getAttributeCount();
        String name = null;
        String slot = null;
        String targetName = null;
        String targetSlot = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_NAME, A_TARGET_NAME));
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case A_SLOT:
                    slot = reader.getAttributeValue(i);
                    break;
                case A_TARGET_NAME:
                    targetName = reader.getAttributeValue(i);
                    break;
                case A_TARGET_SLOT:
                    targetSlot = reader.getAttributeValue(i);
                    break;
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }
        if (!moduleIdentifier.equals(ModuleIdentifier.create(name, slot))) {
            throw invalidModuleName(reader, moduleIdentifier);
        }
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    return ModuleSpec.buildAlias(moduleIdentifier, ModuleIdentifier.create(targetName, targetSlot)).create();
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    private static void parseModuleAbsentContents(final XmlPullParser reader, final ModuleIdentifier moduleIdentifier) throws XmlPullParserException, IOException {
        final int count = reader.getAttributeCount();
        String name = null;
        String slot = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_NAME, A_TARGET_NAME));
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case A_SLOT:
                    slot = reader.getAttributeValue(i);
                    break;
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }
        if (!moduleIdentifier.equals(ModuleIdentifier.create(name, slot))) {
            throw invalidModuleName(reader, moduleIdentifier);
        }
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    return;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    private static void parseModuleContents(final XmlPullParser reader, final ResourceRootFactory factory, final ModuleLoader moduleLoader, final ModuleIdentifier moduleIdentifier, final ModuleSpec.Builder specBuilder, final String rootPath) throws XmlPullParserException, IOException {
        final int count = reader.getAttributeCount();
        String name = null;
        String slot = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_NAME));
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case A_SLOT:
                    slot = reader.getAttributeValue(i);
                    break;
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }
        if (!specBuilder.getIdentifier().equals(ModuleIdentifier.create(name, slot))) {
            throw invalidModuleName(reader, specBuilder.getIdentifier());
        }
        // xsd:all
        MultiplePathFilterBuilder exportsBuilder = PathFilters.multiplePathFilterBuilder(true);
        Set<String> visited = new HashSet<>();
        int eventType;
        boolean gotPerms = false;
        specBuilder.addDependency(DependencySpec.createLocalDependencySpec(PathFilters.acceptAll(), exportsBuilder.create()));
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    if (!gotPerms) specBuilder.setPermissionCollection(ModulesPolicy.DEFAULT_PERMISSION_COLLECTION);
                    return;
                }
                case START_TAG: {
                    validateNamespace(reader);
                    final String element = reader.getName();
                    if (visited.contains(element)) {
                        throw unexpectedContent(reader);
                    }
                    visited.add(element);
                    switch (element) {
                        case E_EXPORTS:
                            parseFilterList(reader, exportsBuilder);
                            break;
                        case E_DEPENDENCIES:
                            parseDependencies(reader, specBuilder);
                            break;
                        case E_MAIN_CLASS:
                            parseMainClass(reader, specBuilder);
                            break;
                        case E_RESOURCES:
                            parseResources(factory, rootPath, reader, specBuilder);
                            break;
                        case E_PROPERTIES:
                            parseProperties(reader, specBuilder);
                            break;
                        case E_PERMISSIONS:
                            parsePermissions(reader, moduleLoader, moduleIdentifier, specBuilder);
                            gotPerms = true;
                            break;
                        default:
                            throw unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    private static void parseDependencies(final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        assertNoAttributes(reader);
        // xsd:choice
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    return;
                }
                case START_TAG: {
                    validateNamespace(reader);
                    switch (reader.getName()) {
                        case E_MODULE:
                            parseModuleDependency(reader, specBuilder);
                            break;
                        case E_SYSTEM:
                            parseSystemDependency(reader, specBuilder);
                            break;
                        default:
                            throw unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    private static void parseModuleDependency(final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        String name = null;
        String slot = null;
        boolean export = false;
        boolean optional = false;
        String services = D_NONE;
        final Set<String> required = new HashSet<>(Arrays.asList(A_NAME));
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case A_SLOT:
                    slot = reader.getAttributeValue(i);
                    break;
                case A_EXPORT:
                    export = Boolean.parseBoolean(reader.getAttributeValue(i));
                    break;
                case A_OPTIONAL:
                    optional = Boolean.parseBoolean(reader.getAttributeValue(i));
                    break;
                case A_SERVICES: {
                    services = reader.getAttributeValue(i);
                    switch (services) {
                        case D_NONE:
                        case D_IMPORT:
                        case D_EXPORT:
                            break;
                        default:
                            throw unknownAttributeValue(reader, i);
                    }
                    break;
                }
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }
        final MultiplePathFilterBuilder importBuilder = PathFilters.multiplePathFilterBuilder(true);
        final MultiplePathFilterBuilder exportBuilder = PathFilters.multiplePathFilterBuilder(export);
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    assert services.equals(D_NONE) || services.equals(D_EXPORT) || services.equals(D_IMPORT);
                    if (services.equals(D_EXPORT)) {
                        // If services are to be re-exported, add META-INF/services -> true near the end of the list
                        exportBuilder.addFilter(PathFilters.getMetaInfServicesFilter(), true);
                    }
                    if (export) {
                        // If re-exported, add META-INF/** -> false at the end of the list (require explicit override)
                        exportBuilder.addFilter(PathFilters.getMetaInfSubdirectoriesFilter(), false);
                        exportBuilder.addFilter(PathFilters.getMetaInfFilter(), false);
                    }
                    final PathFilter exportFilter = exportBuilder.create();
                    final PathFilter importFilter;
                    if (importBuilder.isEmpty()) {
                        importFilter = services.equals(D_NONE) ? PathFilters.getDefaultImportFilter() : PathFilters.getDefaultImportFilterWithServices();
                    } else {
                        if (!services.equals(D_NONE)) {
                            importBuilder.addFilter(PathFilters.getMetaInfServicesFilter(), true);
                        }
                        importBuilder.addFilter(PathFilters.getMetaInfSubdirectoriesFilter(), false);
                        importBuilder.addFilter(PathFilters.getMetaInfFilter(), false);
                        importFilter = importBuilder.create();
                    }
                    specBuilder.addDependency(DependencySpec.createModuleDependencySpec(importFilter, exportFilter, null, ModuleIdentifier.create(name, slot), optional));
                    return;
                }
                case START_TAG: {
                    validateNamespace(reader);
                    switch (reader.getName()) {
                        case E_EXPORTS:
                            parseFilterList(reader, exportBuilder);
                            break;
                        case E_IMPORTS:
                            parseFilterList(reader, importBuilder);
                            break;
                        default:
                            throw unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
    }

    private static void parseSystemDependency(final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        boolean export = false;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            switch (attribute) {
                case A_EXPORT:
                    export = Boolean.parseBoolean(reader.getAttributeValue(i));
                    break;
                default:
                    throw unexpectedContent(reader);
            }
        }
        Set<String> paths = Collections.emptySet();
        final MultiplePathFilterBuilder exportBuilder = PathFilters.multiplePathFilterBuilder(export);
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    final PathFilter exportFilter = exportBuilder.create();
                    specBuilder.addDependency(DependencySpec.createSystemDependencySpec(PathFilters.acceptAll(), exportFilter, paths));
                    return;
                }
                case START_TAG: {
                    validateNamespace(reader);
                    switch (reader.getName()) {
                        case E_PATHS: {
                            paths = parseSet(reader);
                            break;
                        }
                        case E_EXPORTS: {
                            parseFilterList(reader, exportBuilder);
                            break;
                        }
                        default: {
                            throw unexpectedContent(reader);
                        }
                    }
                }
            }
        }
    }

    private static void parseMainClass(final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        String name = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_NAME));
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                default:
                    throw unexpectedContent(reader);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }
        specBuilder.setMainClass(name);
        // consume remainder of element
        parseNoContent(reader);
    }

    private static void parseResources(final ResourceRootFactory factory, final String rootPath, final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        assertNoAttributes(reader);
        // xsd:choice
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    specBuilder.addResourceRoot(new ResourceLoaderSpec(new NativeLibraryResourceLoader(new File(rootPath, "lib")), PathFilters.rejectAll()));
                    return;
                }
                case START_TAG: {
                    validateNamespace(reader);
                    switch (reader.getName()) {
                        case E_RESOURCE_ROOT: {
                            parseResourceRoot(factory, rootPath, reader, specBuilder);
                            break;
                        }
                        case E_ARTIFACT: {
                            parseArtifact(reader, specBuilder);
                            break;
                        }
                        case E_NATIVE_ARTIFACT: {
                            parseNativeArtifact(reader, specBuilder);
                            break;
                        }
                        default:
                            throw unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    public static ResourceLoader createMavenArtifactLoader(final String name) throws IOException {
// SELF CONTAINED - START
        final String[] parts = name.split(":");

        // Must have at least a groupId, artiactId and version
        if (parts.length < 3) {
            throw new IllegalArgumentException("Maven groupId, artifactId and/or version could not be found in " + name);
        }

        String group = parts[0];
        String artifact = parts[1];
        String version = parts[2];

        String classifier = null;
        // Check for a classifier
        if (parts.length >= 4) {
            classifier = parts[3];
            if (classifier.isEmpty()) {
                classifier = null;
            }
        }

        if (artifact.endsWith("?jandex")) {
            artifact = artifact.substring(0, artifact.length() - 7);
        }

        final String path = group + ":" + artifact + ":" + version + (classifier == null ? "" : ":" + classifier);
        return ArtifactLoaderFactory.INSTANCE.getLoader(path);
// SELF CONTAINED - END
    }

    static void createMavenNativeArtifactLoader(final String name, final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws IOException, XmlPullParserException {
        File fp = MavenArtifactUtil.resolveJarArtifact(name);
        if (fp == null)
            throw new XmlPullParserException(String.format("Failed to resolve native artifact '%s'", name), reader, null);
        File lib = new File(fp.getParentFile(), "lib");
        if (!lib.exists()) {
            if (!fp.getParentFile().canWrite())
                throw new XmlPullParserException(String.format("Native artifact '%s' cannot be unpacked", name), reader, null);
            StreamUtil.unzip(fp, fp.getParentFile());
        }
        specBuilder.addResourceRoot(new ResourceLoaderSpec(new NativeLibraryResourceLoader(lib), PathFilters.rejectAll()));
    }


    private static void parseNativeArtifact(final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        String name = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_NAME));
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }

        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    try {
                        createMavenNativeArtifactLoader(name, reader, specBuilder);
                    } catch (IOException e) {
                        throw new XmlPullParserException(String.format("Failed to add artifact '%s'", name), reader, e);
                    }
                    return;
                }
                case START_TAG: {
                    throw unexpectedContent(reader);
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
    }

    private static void parseArtifact(final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        String name = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_NAME));
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }

        ResourceLoader resourceLoader;
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    try {
                        resourceLoader = createMavenArtifactLoader(name);
                    } catch (IOException e) {
                        throw new XmlPullParserException(String.format("Failed to add artifact '%s'", name), reader, e);
                    }
                    if (resourceLoader == null)
                        throw new XmlPullParserException(String.format("Failed to resolve artifact '%s'", name), reader, null);
                    specBuilder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(resourceLoader));
                    return;
                }
                case START_TAG: {
                    throw unexpectedContent(reader);
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
    }

    private static void parseResourceRoot(final ResourceRootFactory factory, final String rootPath, final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        String name = null;
        String path = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_PATH));
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case A_PATH:
                    path = reader.getAttributeValue(i);
                    break;
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }
        if (name == null) name = path;

        final MultiplePathFilterBuilder filterBuilder = PathFilters.multiplePathFilterBuilder(true);
        final ResourceLoader resourceLoader;

        final Set<String> encountered = new HashSet<>();
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    try {
                        resourceLoader = factory.createResourceLoader(rootPath, path, name);
                    } catch (IOException e) {
                        throw new XmlPullParserException(String.format("Failed to add resource root '%s' at path '%s'", name, path), reader, e);
                    }
                    specBuilder.addResourceRoot(new ResourceLoaderSpec(resourceLoader, filterBuilder.create()));
                    return;
                }
                case START_TAG: {
                    validateNamespace(reader);
                    final String element = reader.getName();
                    if (!encountered.add(element)) throw unexpectedContent(reader);
                    switch (element) {
                        case E_FILTER:
                            parseFilterList(reader, filterBuilder);
                            break;
                        default:
                            throw unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
    }

    private static void parseFilterList(final XmlPullParser reader, final MultiplePathFilterBuilder builder) throws XmlPullParserException, IOException {
        assertNoAttributes(reader);
        // xsd:choice
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    return;
                }
                case START_TAG: {
                    validateNamespace(reader);
                    switch (reader.getName()) {
                        case E_INCLUDE:
                            parsePath(reader, true, builder);
                            break;
                        case E_EXCLUDE:
                            parsePath(reader, false, builder);
                            break;
                        case E_INCLUDE_SET:
                            parseSet(reader, true, builder);
                            break;
                        case E_EXCLUDE_SET:
                            parseSet(reader, false, builder);
                            break;
                        default:
                            throw unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    private static void parsePath(final XmlPullParser reader, final boolean include, final MultiplePathFilterBuilder builder) throws XmlPullParserException, IOException {
        String path = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_PATH));
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_PATH:
                    path = reader.getAttributeValue(i);
                    break;
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }

        final boolean literal = path.indexOf('*') == -1 && path.indexOf('?') == -1;
        if (literal) {
            if (path.charAt(path.length() - 1) == '/') {
                builder.addFilter(PathFilters.isChildOf(path), include);
            } else {
                builder.addFilter(PathFilters.is(path), include);
            }
        } else {
            builder.addFilter(PathFilters.match(path), include);
        }

        // consume remainder of element
        parseNoContent(reader);
    }

    private static Set<String> parseSet(final XmlPullParser reader) throws XmlPullParserException, IOException {
        assertNoAttributes(reader);
        final Set<String> set = new FastCopyHashSet<>();
        // xsd:choice
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    return set;
                }
                case START_TAG: {
                    validateNamespace(reader);
                    switch (reader.getName()) {
                        case E_PATH:
                            parsePathName(reader, set);
                            break;
                        default:
                            throw unexpectedContent(reader);
                    }
                }
            }
        }
        return set;
    }

    private static void parseSet(final XmlPullParser reader, final boolean include, final MultiplePathFilterBuilder builder) throws XmlPullParserException, IOException {
        builder.addFilter(PathFilters.in(parseSet(reader)), include);
    }

    private static void parsePathName(final XmlPullParser reader, final Set<String> set) throws XmlPullParserException, IOException {
        String name = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_NAME));
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }
        set.add(name);

        // consume remainder of element
        parseNoContent(reader);
    }

    private static void parseProperties(final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        assertNoAttributes(reader);
        // xsd:choice
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    return;
                }
                case START_TAG: {
                    validateNamespace(reader);
                    switch (reader.getName()) {
                        case E_PROPERTY: {
                            parseProperty(reader, specBuilder);
                            break;
                        }
                        default:
                            throw unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    private static void parseProperty(final XmlPullParser reader, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        String name = null;
        String value = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_NAME));
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case A_VALUE:
                    value = reader.getAttributeValue(i);
                    break;
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }
        specBuilder.addProperty(name, value == null ? "true" : value);
        if ("jboss.assertions".equals(name)) try {
            specBuilder.setAssertionSetting(AssertionSetting.valueOf(value.toUpperCase(Locale.US)));
        } catch (IllegalArgumentException ignored) {
        }

        // consume remainder of element
        parseNoContent(reader);
    }

    private static void parsePermissions(final XmlPullParser reader, final ModuleLoader moduleLoader, final ModuleIdentifier moduleIdentifier, final ModuleSpec.Builder specBuilder) throws XmlPullParserException, IOException {
        assertNoAttributes(reader);
        // xsd:choice
        ArrayList<PermissionFactory> list = new ArrayList<>();
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    specBuilder.setPermissionCollection(new FactoryPermissionCollection(list.toArray(new PermissionFactory[list.size()])));
                    return;
                }
                case START_TAG: {
                    validateNamespace(reader);
                    switch (reader.getName()) {
                        case E_GRANT: {
                            parseGrant(reader, moduleLoader, moduleIdentifier, list);
                            break;
                        }
                        default:
                            throw unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    private static void parseGrant(final XmlPullParser reader, final ModuleLoader moduleLoader, final ModuleIdentifier moduleIdentifier, final ArrayList<PermissionFactory> list) throws XmlPullParserException, IOException {
        String permission = null;
        String name = null;
        String actions = null;
        final Set<String> required = new HashSet<>(Arrays.asList(A_PERMISSION, A_NAME));
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            validateAttributeNamespace(reader, i);
            final String attribute = reader.getAttributeName(i);
            required.remove(attribute);
            switch (attribute) {
                case A_PERMISSION:
                    permission = reader.getAttributeValue(i);
                    break;
                case A_NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case A_ACTIONS:
                    actions = reader.getAttributeValue(i);
                    break;
                default:
                    throw unknownAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingAttributes(reader, required);
        }
        list.add(new ModularPermissionFactory(moduleLoader, moduleIdentifier, permission, name, actions));

        // consume remainder of element
        parseNoContent(reader);
    }

    private static void parseNoContent(final XmlPullParser reader) throws XmlPullParserException, IOException {
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    return;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    static void parseEndDocument(final XmlPullParser reader) throws XmlPullParserException, IOException {
        int eventType;
        while ((eventType = reader.nextToken()) != END_DOCUMENT) {
            switch (eventType) {
                case END_DOCUMENT: {
                    return;
                }
                case TEXT:
                case CDSECT: {
                    if (!reader.isWhitespace()) {
                        throw unexpectedContent(reader);
                    }
                    // ignore
                    break;
                }
                case IGNORABLE_WHITESPACE:
                case COMMENT: {
                    // ignore
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        return;
    }
}

