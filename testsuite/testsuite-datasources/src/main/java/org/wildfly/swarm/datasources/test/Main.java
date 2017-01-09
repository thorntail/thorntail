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
package org.wildfly.swarm.datasources.test;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.datasources.DatasourcesFraction;

/**
 * @author Heiko Braun
 */
public class Main {

    protected Main() {
    }

    public static void main(String... args) throws Exception {
        Swarm swarm = new Swarm(args);

        swarm.fraction(
                new DatasourcesFraction()
                        .jdbcDriver("h2", (d) -> {
                            d.driverClassName("org.h2.Driver");
                            d.xaDatasourceClass("org.h2.jdbcx.JdbcDataSource");
                            d.driverModuleName("com.h2database.h2");
                        })
                        .dataSource("ExampleDS", (ds) -> {
                            ds.driverName("h2");
                            ds.connectionUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
                            ds.userName("sa");
                            ds.password("sa");
                        })
        );

        swarm.start().deploy();
    }
}

