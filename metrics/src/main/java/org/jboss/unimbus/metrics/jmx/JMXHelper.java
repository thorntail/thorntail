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
package org.jboss.unimbus.metrics.jmx;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.unimbus.metrics.MetricRegistryProducer;

/**
 * @author hrupp
 */
@ApplicationScoped
public class JMXHelper {

    private final String PLACEHOLDER = "%s";

    private MBeanServer mbs;

    private JMXHelper worker;

    private JMXHelper() { /* singleton */ }

    @PostConstruct
    void init() {
        this.mbs = ManagementFactory.getPlatformMBeanServer();
    }


    public Map<String, Double> getMetrics(MetricRegistry.Type scope) {

        Map<String, Metadata> metadataMap = MetricRegistryProducer.get(scope).getMetadata();
        Map<String, Double> outcome = new HashMap<>();

        for (Metadata m : metadataMap.values()) {
            if (!(m instanceof ExtendedMetadata)) {
                throw new IllegalStateException("Not extended Metadata " + m);
            }
            ExtendedMetadata em = (ExtendedMetadata) m;
            Double val = getValue(em.getMbean()).doubleValue();
            outcome.put(em.getName(), val);
        }
        return outcome;
    }


    /**
     * Read a value from the MBeanServer
     *
     * @param mbeanExpression The expression to look for
     * @return The value of the Mbean attribute
     */
    public Number getValue(String mbeanExpression) {

        if (mbeanExpression == null) {
            throw new IllegalArgumentException("MBean Expression is null");
        }
        if (!mbeanExpression.contains("/")) {
            throw new IllegalArgumentException(mbeanExpression);
        }

        int slashIndex = mbeanExpression.indexOf('/');
        String mbean = mbeanExpression.substring(0, slashIndex);
        String attName = mbeanExpression.substring(slashIndex + 1);
        String subItem = null;
        if (attName.contains("#")) {
            int hashIndex = attName.indexOf('#');
            subItem = attName.substring(hashIndex + 1);
            attName = attName.substring(0, hashIndex);
        }

        try {
            ObjectName objectName = new ObjectName(mbean);
            Object attribute = mbs.getAttribute(objectName, attName);
            if (attribute instanceof Number) {
                return (Number) attribute;
            } else if (attribute instanceof CompositeData) {
                CompositeData compositeData = (CompositeData) attribute;
                return (Number) compositeData.get(subItem);
            } else {
                throw new IllegalArgumentException(mbeanExpression);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * We need to expand entries that are marked with the <b>multi</b> flag
     * into the actual MBeans. This is done by replacing a placeholder of <b>%s</b>
     * in the name and MBean name with the real Mbean key-value.
     *
     * @param entries List of entries
     */
    void expandMultiValueEntries(List<ExtendedMetadata> entries) {
        List<ExtendedMetadata> result = new ArrayList<>();
        List<Metadata> toBeRemoved = new ArrayList<>(entries.size());
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        for (ExtendedMetadata entry : entries) {
            if (entry.isMulti()) {
                String name = entry.getMbean().replace(PLACEHOLDER, "*");
                String attName;
                String queryableName;
                int slashIndex = name.indexOf('/');

                // MBeanName is invalid, lets skip this altogether
                if (slashIndex < 0) {
                    toBeRemoved.add(entry);
                    continue;
                }

                queryableName = name.substring(0, slashIndex);
                attName = name.substring(slashIndex + 1);

                try {
                    ObjectName objectName = new ObjectName(queryableName);

                    String keyHolder = findKeyForValueToBeReplaced(objectName);

                    Set<ObjectName> objNames = mbs.queryNames(objectName, null);
                    for (ObjectName oName : objNames) {
                        String keyValue = oName.getKeyPropertyList().get(keyHolder);
                        String newName = entry.getName();
                        if (!newName.contains(PLACEHOLDER)) {
                            System.err.println("Name [" + newName + "] did not contain a %s, no replacement will be done, check" +
                                                       " the configuration");
                        }
                        newName = newName.replace(PLACEHOLDER, keyValue);
                        String newDisplayName = entry.getDisplayName().replace(PLACEHOLDER, keyValue);
                        String newDescription = entry.getDescription().replace(PLACEHOLDER, keyValue);
                        ExtendedMetadata newEntry = new ExtendedMetadata(newName, newDisplayName, newDescription,
                                                                         entry.getTypeRaw(), entry.getUnit());
                        String newObjectName = oName.getCanonicalName() + "/" + attName;
                        newEntry.setMbean(newObjectName);
                        result.add(newEntry);
                    }
                    toBeRemoved.add(entry);
                } catch (MalformedObjectNameException e) {
                    e.printStackTrace();  // TODO: Customise this generated block
                }
            }
        }
        entries.removeAll(toBeRemoved);
        entries.addAll(result);
    }

    private String findKeyForValueToBeReplaced(ObjectName objectName) {
        String keyHolder = null;
        Hashtable<String, String> keyPropList = objectName.getKeyPropertyList();
        for (String key : keyPropList.keySet()) {
            if (keyPropList.get(key).equals("*")) {
                keyHolder = key;
            }
        }
        return keyHolder;
    }
}
