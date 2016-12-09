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
package org.wildfly.swarm.mongodb.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;

import com.mongodb.client.MongoDatabase;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

/**
 * @author Scott Marlow
 */
@RunWith(Arquillian.class)
@DefaultDeployment
public class MongoDBArquillianTest {
    @ArquillianResource
    InitialContext context;

    @Test
    public void resourceLookup() throws Exception {
        Object mongoDB = context.lookup("java:jboss/mongodb/test");
        assertNotNull(mongoDB);
        assertTrue(mongoDB instanceof MongoDatabase);
    }

    @Inject
    @Named("mongodbtestprofile")
    MongoDatabase database;

    @Test
    public void injectDatabaseConnection() throws Exception {
        assertNotNull(database);
    }

    @EJB(lookup = "java:global/MongoDBArquillianTest/StatefulTestBean")
        private StatefulTestBean statefulTestBean;

    @Test
    public void beanTest() throws Exception {
        assertNotNull(statefulTestBean);
        String comment = statefulTestBean.addUserComment();
        assertTrue(comment + " contains \"MongoDB Is Web Scale\"", comment.contains("MongoDB Is Web Scale"));
        String product = statefulTestBean.addProduct();
        assertTrue(product + " contains \"Acme products\"", product.contains("Acme products"));
    }
}
