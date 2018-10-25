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
package org.wildfly.swarm.container.runtime.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.wildfly.swarm.spi.api.cdi.CommonBean;
import org.wildfly.swarm.spi.api.cdi.CommonBeanBuilder;

/**
 * @author Bob McWhirter
 */
public class CommandLineArgsExtension implements Extension {

    private final String[] args;

    private final List<String> argsList;

    public CommandLineArgsExtension(String... args) {
        this.args = args;
        this.argsList = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(this.args)));
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        CommonBean<String[]> stringBean = CommonBeanBuilder.newBuilder(String[].class)
                .beanClass(CommandLineArgsExtension.class)
                .scope(Dependent.class)
                .createSupplier(() -> args)
                .addQualifier(CommandLineArgs.Literal.INSTANCE)
                .addType(String[].class)
                .addType(Object.class).build();
        abd.addBean(stringBean);
        CommonBean<List> listBean = CommonBeanBuilder.newBuilder(List.class)
                .beanClass(CommandLineArgsExtension.class)
                .scope(Dependent.class)
                .createSupplier(() -> argsList)
                .addQualifier(Default.Literal.INSTANCE)
                .addType(List.class)
                .addType(Object.class).build();
        abd.addBean(listBean);
    }
}
