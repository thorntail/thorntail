/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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

package io.thorntail.openapi.impl.api.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.Extensible;
import org.eclipse.microprofile.openapi.models.Reference;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import io.thorntail.openapi.impl.api.models.OpenAPIImpl;

/**
 * Used to merge two OAI data models into a single one.  The MP+OAI 1.0 spec
 * requires that any or all of the various mechanisms for producing an OAI document
 * can be used.  When more than one mechanism is used, each mechanism produces an
 * OpenAPI document.  These multiple documents must then be sensibly merged into
 * a final result.
 *
 * @author eric.wittmann@gmail.com
 */
public class MergeUtil {

    private static final Set<String> EXCLUDED_PROPERTIES = new HashSet<>();

    static {
        EXCLUDED_PROPERTIES.add("class");
    }

    /**
     * Constructor.
     */
    private MergeUtil() {
    }

    /**
     * Merges two documents and returns the result.
     *
     * @param document1
     * @param document2
     */
    public static final OpenAPIImpl merge(OpenAPIImpl document1, OpenAPIImpl document2) {
        return mergeObjects(document1, document2);
    }

    /**
     * Generic merge of two objects of the same type.
     *
     * @param object1
     * @param object2
     */
    @SuppressWarnings({"rawtypes"})
    public static <T> T mergeObjects(T object1, T object2) {
        if (object1 == null && object2 != null) {
            return object2;
        }
        if (object1 != null && object2 == null) {
            return object1;
        }
        if (object1 == null && object2 == null) {
            return null;
        }

        // It's uncommon, but in some cases (like Link Parameters or Examples) the values could
        // be different types.  In this case, just take the 2nd one (the override).
        if (!object1.getClass().equals(object2.getClass())) {
            return object2;
        }

        PropertyDescriptor[] descriptors;
        try {
            descriptors = Introspector.getBeanInfo(object1.getClass()).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        for (PropertyDescriptor descriptor : descriptors) {
            if (EXCLUDED_PROPERTIES.contains(descriptor.getName())) {
                continue;
            }
            Class ptype = descriptor.getPropertyType();
            if (Map.class.isAssignableFrom(ptype)) {
                try {
                    Map values1 = (Map) descriptor.getReadMethod().invoke(object1);
                    Map values2 = (Map) descriptor.getReadMethod().invoke(object2);
                    Map newValues = mergeMaps(values1, values2);
                    descriptor.getWriteMethod().invoke(object1, newValues);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else if (List.class.isAssignableFrom(ptype)) {
                try {
                    List values1 = (List) descriptor.getReadMethod().invoke(object1);
                    List values2 = (List) descriptor.getReadMethod().invoke(object2);
                    List newValues = mergeLists(values1, values2);
                    descriptor.getWriteMethod().invoke(object1, newValues);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else if (Constructible.class.isAssignableFrom(ptype)) {
                try {
                    Object val1 = descriptor.getReadMethod().invoke(object1);
                    Object val2 = descriptor.getReadMethod().invoke(object2);
                    Object newValue = mergeObjects(val1, val2);
                    if (newValue != null) {
                        descriptor.getWriteMethod().invoke(object1, newValue);
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    Object newValue = descriptor.getReadMethod().invoke(object2);
                    if (newValue != null) {
                        descriptor.getWriteMethod().invoke(object1, newValue);
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return object1;
    }

    /**
     * Merges two Maps.  Any values missing from Map1 but present in Map2 will be added.  If a value
     * is present in both maps, it will be overridden or merged.
     *
     * @param values1
     * @param values2
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map mergeMaps(Map values1, Map values2) {
        if (values1 == null && values2 == null) {
            return null;
        }
        if (values1 != null && values2 == null) {
            return values1;
        }
        if (values1 == null && values2 != null) {
            return values2;
        }

        for (Object key : values2.keySet()) {
            if (values1.containsKey(key)) {
                Object pval1 = values1.get(key);
                Object pval2 = values2.get(key);
                if (pval1 instanceof Map) {
                    values1.put(key, mergeMaps((Map) pval1, (Map) pval2));
                } else if (pval1 instanceof List) {
                    values1.put(key, mergeLists((List) pval1, (List) pval2));
                } else if (pval1 instanceof Constructible) {
                    values1.put(key, mergeObjects(pval1, pval2));
                } else {
                    values1.put(key, pval2);
                }
            } else {
                Object pval2 = values2.get(key);
                values1.put(key, pval2);
            }
        }

        if (values1 instanceof Constructible) {
            if (values1 instanceof Reference) {
                Reference ref1 = (Reference) values1;
                Reference ref2 = (Reference) values2;
                if (ref2.getRef() != null) {
                    ref1.setRef(ref2.getRef());
                }
            }
            if (values1 instanceof Extensible) {
                Extensible extensible1 = (Extensible) values1;
                Extensible extensible2 = (Extensible) values2;
                extensible1.setExtensions(mergeMaps(extensible1.getExtensions(), extensible2.getExtensions()));
            }
            if (values1 instanceof APIResponses) {
                APIResponses responses1 = (APIResponses) values1;
                APIResponses responses2 = (APIResponses) values2;
                responses1.defaultValue(mergeObjects(responses1.getDefault(), responses2.getDefault()));
            }
        }

        return values1;
    }

    /**
     * Merges two Lists.  Any values missing from List1 but present in List2 will be added.  Depending on
     * the type of list, further processing and de-duping may be required.
     *
     * @param values1
     * @param values2
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List mergeLists(List values1, List values2) {
        if (values1 == null && values2 == null) {
            return null;
        }
        if (values1 != null && values2 == null) {
            return values1;
        }
        if (values1 == null && values2 != null) {
            return values2;
        }

        if (values1.get(0) instanceof String) {
            return mergeStringLists(values1, values2);
        }

        if (values1.get(0) instanceof Tag) {
            return mergeTagLists(values1, values2);
        }

        if (values1.get(0) instanceof Server) {
            return mergeServerLists(values1, values2);
        }

        if (values1.get(0) instanceof SecurityRequirement) {
            return mergeSecurityRequirementLists(values1, values2);
        }

        values1.addAll(values2);
        return values1;
    }

    /**
     * Merge a list of strings.  In all cases, string lists are really sets.  So this is just
     * combining the two lists and then culling duplicates.
     *
     * @param values1
     * @param values2
     */
    private static List<String> mergeStringLists(List<String> values1, List<String> values2) {
        Set<String> set = new LinkedHashSet<String>();
        set.addAll(values1);
        set.addAll(values2);
        return new ArrayList<String>(set);
    }

    /**
     * Merge two lists of Tags.  Tags are a special case because they are named and you cannot
     * have two Tags with the same name.  This will append any tags from values2 that don't
     * exist in values1.  It will *merge* any tags found in values2 that already exist in
     * values1.
     *
     * @param values1
     * @param values2
     */
    private static List<Tag> mergeTagLists(List<Tag> values1, List<Tag> values2) {
        for (Tag value2 : values2) {
            Tag match = null;
            for (Tag value1 : values1) {
                if (value1.getName() != null && value1.getName().equals(value2.getName())) {
                    match = value1;
                    break;
                }
            }
            if (match == null) {
                values1.add(value2);
            } else {
                mergeObjects(match, value2);
            }
        }
        return values1;
    }

    /**
     * Merge two lists of Servers.  Servers are a special case because they must be unique
     * by the 'url' property each must have.
     *
     * @param values1
     * @param values2
     */
    private static List<Server> mergeServerLists(List<Server> values1, List<Server> values2) {
        for (Server value2 : values2) {
            Server match = null;
            for (Server value1 : values1) {
                if (value1.getUrl() != null && value1.getUrl().equals(value2.getUrl())) {
                    match = value1;
                    break;
                }
            }
            if (match == null) {
                values1.add(value2);
            } else {
                mergeObjects(match, value2);
            }
        }
        return values1;
    }

    /**
     * Merge two lists of Security Requirements.  Security Requirement lists are are a
     * special case because
     * values1.
     *
     * @param values1
     * @param values2
     */
    private static List<SecurityRequirement> mergeSecurityRequirementLists(List<SecurityRequirement> values1, List<SecurityRequirement> values2) {
        for (SecurityRequirement value2 : values2) {
            if (values1.contains(value2)) {
                continue;
            }
            values1.add(value2);
        }
        return values1;
    }

}
