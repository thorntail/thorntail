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
package org.wildfly.swarm.container.runtime.wildfly;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.enterprise.inject.Vetoed;

import org.jboss.as.logging.logmanager.ConfigurationPersistence;
import org.jboss.logmanager.Configurator;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.PropertyConfigurator;
import org.jboss.logmanager.config.ErrorManagerConfiguration;
import org.jboss.logmanager.config.FilterConfiguration;
import org.jboss.logmanager.config.FormatterConfiguration;
import org.jboss.logmanager.config.HandlerConfiguration;
import org.jboss.logmanager.config.LogContextConfiguration;
import org.jboss.logmanager.config.LoggerConfiguration;
import org.jboss.logmanager.config.PojoConfiguration;
import org.wildfly.swarm.bootstrap.logging.InitialLoggerManager;
import org.wildfly.swarm.bootstrap.logging.LevelNode;

/**
 * @author Bob McWhirter
 */
@Vetoed
// WF14 WFLYLOG0013: A configurator class, 'class org.wildfly.swarm.container.runtime.wildfly.LoggingConfigurator', is not a known configurator and will be replaced.
public class LoggingConfigurator implements Configurator, LogContextConfiguration {

    private final ConfigurationPersistence configPersistence;
    private final PropertyConfigurator propertyConfigurator;
    /**
     * Construct an instance.
     */
    public LoggingConfigurator() {
        this(LogContext.getSystemLogContext());
    }

    /**
     * Construct a new instance.
     *
     * @param context the log context to be configured
     */
    public LoggingConfigurator(LogContext context) {
        this.configPersistence = ConfigurationPersistence.getOrCreateConfigurationPersistence(context);
        this.propertyConfigurator = new PropertyConfigurator(context);
    }

    @Override
    public void configure(InputStream inputStream) throws IOException {
        this.propertyConfigurator.configure(inputStream);
        LogContextConfiguration config = this.propertyConfigurator.getLogContextConfiguration();
        config.getHandlerConfiguration("CONSOLE").setLevel("ALL");
        LevelNode root = InitialLoggerManager.INSTANCE.getRoot();
        apply(root, config);
        config.commit();
    }

    protected void apply(LevelNode node, LogContextConfiguration config) {
        if (!node.getName().equals("")) {
            config.addLoggerConfiguration(node.getName()).setLevel(node.getLevel().toString());
        }

        for (LevelNode each : node.getChildren()) {
            apply(each, config);
        }
    }

    @Override
    public LogContext getLogContext() {
        return configPersistence.getLogContext();
    }

    @Override
    public LoggerConfiguration addLoggerConfiguration(String loggerName) {
        return configPersistence.addLoggerConfiguration(loggerName);
    }

    @Override
    public boolean removeLoggerConfiguration(String loggerName) {
        return configPersistence.removeLoggerConfiguration(loggerName);
    }

    @Override
    public LoggerConfiguration getLoggerConfiguration(String loggerName) {
        return configPersistence.getLoggerConfiguration(loggerName);
    }

    @Override
    public List<String> getLoggerNames() {
        return configPersistence.getLoggerNames();
    }

    @Override
    public HandlerConfiguration addHandlerConfiguration(String moduleName, String className, String handlerName,
            String... constructorProperties) {
        return configPersistence.addHandlerConfiguration(moduleName, className, handlerName, constructorProperties);
    }

    @Override
    public boolean removeHandlerConfiguration(String handlerName) {
        return configPersistence.removeHandlerConfiguration(handlerName);
    }

    @Override
    public HandlerConfiguration getHandlerConfiguration(String handlerName) {
        return configPersistence.getHandlerConfiguration(handlerName);
    }

    @Override
    public List<String> getHandlerNames() {
        return configPersistence.getHandlerNames();
    }

    @Override
    public FormatterConfiguration addFormatterConfiguration(String moduleName, String className, String formatterName,
            String... constructorProperties) {
        return configPersistence.addFormatterConfiguration(moduleName, className, formatterName, constructorProperties);
    }

    @Override
    public boolean removeFormatterConfiguration(String formatterName) {
        return configPersistence.removeFormatterConfiguration(formatterName);
    }

    @Override
    public FormatterConfiguration getFormatterConfiguration(String formatterName) {
        return configPersistence.getFormatterConfiguration(formatterName);
    }

    @Override
    public List<String> getFormatterNames() {
        return configPersistence.getFormatterNames();
    }

    @Override
    public FilterConfiguration addFilterConfiguration(String moduleName, String className, String filterName,
            String... constructorProperties) {
        return configPersistence.addFilterConfiguration(moduleName, className, filterName, constructorProperties);
    }

    @Override
    public boolean removeFilterConfiguration(String filterName) {
        return configPersistence.removeFilterConfiguration(filterName);
    }

    @Override
    public FilterConfiguration getFilterConfiguration(String filterName) {
        return configPersistence.getFilterConfiguration(filterName);
    }

    @Override
    public List<String> getFilterNames() {
        return configPersistence.getFilterNames();
    }

    @Override
    public ErrorManagerConfiguration addErrorManagerConfiguration(String moduleName, String className,
            String errorManagerName, String... constructorProperties) {
        return configPersistence.addErrorManagerConfiguration(moduleName, className, errorManagerName, constructorProperties);
    }

    @Override
    public boolean removeErrorManagerConfiguration(String errorManagerName) {
        return configPersistence.removeErrorManagerConfiguration(errorManagerName);
    }

    @Override
    public ErrorManagerConfiguration getErrorManagerConfiguration(String errorManagerName) {
        return configPersistence.getErrorManagerConfiguration(errorManagerName);
    }

    @Override
    public List<String> getErrorManagerNames() {
        return configPersistence.getErrorManagerNames();
    }

    @Override
    public void prepare() {
        configPersistence.prepare();
    }

    @Override
    public PojoConfiguration addPojoConfiguration(String moduleName, String className, String pojoName,
            String... constructorProperties) {
        return configPersistence.addPojoConfiguration(moduleName, className, pojoName, constructorProperties);
    }

    @Override
    public boolean removePojoConfiguration(String pojoName) {
        return configPersistence.removePojoConfiguration(pojoName);
    }

    @Override
    public PojoConfiguration getPojoConfiguration(String pojoName) {
        return configPersistence.getPojoConfiguration(pojoName);
    }

    @Override
    public List<String> getPojoNames() {
        return configPersistence.getPojoNames();
    }

    @Override
    public void commit() {
        configPersistence.commit();
    }

    @Override
    public void forget() {
        configPersistence.forget();
    }
}
