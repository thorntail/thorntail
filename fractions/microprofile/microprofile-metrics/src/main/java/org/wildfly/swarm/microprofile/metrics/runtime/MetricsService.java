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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.metrics.Metric;
import org.jboss.as.controller.ModelController;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.microprofile.metrics.runtime.mbean.MGaugeImpl;
import org.wildfly.swarm.microprofile.metrics.runtime.mbean.MCounterImpl;

/**
 * @author Heiko W. Rupp
 */
public class MetricsService implements Service<MetricsService> {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm", "mp-metrics");

    private final InjectedValue<ServerEnvironment> serverEnvironmentValue = new InjectedValue<ServerEnvironment>();
    private final InjectedValue<ModelController> modelControllerValue = new InjectedValue<ModelController>();


    @Override
    public void start(StartContext context) throws StartException {
        initBaseAndVendorConfiguration();

        LOG.info("MicroProfile-Metrics started");
    }

    /**
     * Read a list of mappings that contains the base and vendor metrics
     * along with their metadata.
     */
    private void initBaseAndVendorConfiguration() {
        InputStream is = getClass().getResourceAsStream("mapping.yml");

        if (is != null) {

            Config config = ConfigProvider.getConfig();

            Optional<String> globalTagsFromConfig = config.getOptionalValue("mp.metrics.tags", String.class);
            if (!globalTagsFromConfig.isPresent()) {
                globalTagsFromConfig = config.getOptionalValue("MP_METRICS_TAGS", String.class);
            }
            List<Tag> globalTags = convertToTags(globalTagsFromConfig);

            // Read base + vendor metrics from mapping.yml
            ConfigReader cr = new ConfigReader();
            MetadataList ml = cr.readConfig(is, globalTags);


            // Turn the multi-entry query expressions into concrete entries.
            JmxWorker.instance().expandMultiValueEntries(ml.getBase());
            JmxWorker.instance().expandMultiValueEntries(ml.getVendor());

            for (ExtendedMetadata em : ml.getBase()) {
                Metric type = getType(em);
                MetricRegistries.getBaseRegistry().register(em, type);
            }
            for (ExtendedMetadata em : ml.getVendor()) {
                Metric type = getType(em);
                MetricRegistries.getVendorRegistry().register(em, type);
            }
        } else {
            throw new IllegalStateException("Was not able to find the mapping file 'mapping.yml'");
        }
    }

    private Metric getType(ExtendedMetadata em) {
        Metric out;
        switch (em.getTypeRaw()) {
            case GAUGE:
                out = new MGaugeImpl(em.getMbean());
                break;
            case COUNTER:
                out = new MCounterImpl(em.getMbean());
                break;
            default:
                throw new IllegalStateException("Not yet supported: " + em);
        }
        return out;
    }

    private List<Tag> convertToTags(Optional<String> globalTags) {
        List<Tag> tags = new ArrayList<>();

        if (!globalTags.isPresent()) {
            return tags;
        }
        String globalTagsString = globalTags.get();
        if (!globalTagsString.equals("")) {
            String[] singleTags = globalTagsString.split(",");
            for (String singleTag : singleTags) {
                tags.add(new Tag(singleTag.trim()));
            }
        }
        return tags;
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public MetricsService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    /**
     * Register the metrics of the base scope with the system.
     */


    public Injector<ServerEnvironment> getServerEnvironmentInjector() {
        return this.serverEnvironmentValue;
    }

    public Injector<ModelController> getModelControllerInjector() {
        return this.modelControllerValue;
    }

}
