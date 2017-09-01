/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.orientdb.test;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public abstract class AbstractTestCase {

    private static final String DATABASE_JNDI = "java:jboss/orientdb/test";

    @ArquillianResource
    private static InitialContext initialContext;

    @Before
    public void before() throws NamingException {
        try {
            clearDatabase();
        } catch (Throwable t) {
            // Database might not exist at this stage
        }
        initDatabase();
    }

    @After
    public void after() throws NamingException {
        clearDatabase();
    }

    @Inject
        @Named("orienttesttprofile")
        private OPartitionedDatabasePool databasePool;
    private OPartitionedDatabasePool getDatabasePool() throws NamingException {
        return databasePool;
    }

    private void initDatabase() throws NamingException {
        try (ODatabaseDocumentTx database = getDatabasePool().acquire()) {
            database.getMetadata().getSchema().createClass("Person").createProperty("name", OType.STRING);
        }
    }

    private void clearDatabase() throws NamingException {
        try (ODatabaseDocumentTx database = getDatabasePool().acquire()) {
            database.getMetadata().getSchema().dropClass("Person");
        } catch (Throwable ignore) { }

        OrientGraph database = new OrientGraph(getDatabasePool());
        try {
            database.getEdges().forEach(database::removeEdge);
            database.getVertices().forEach(database::removeVertex);
        } finally {
            database.shutdown();
        }
    }

}
