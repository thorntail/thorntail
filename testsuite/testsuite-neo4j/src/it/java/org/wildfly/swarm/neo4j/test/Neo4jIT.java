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
package org.wildfly.swarm.neo4j.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeoutException;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.arquillian.container.test.api.Deployment;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.driver.v1.Driver;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.cdi.CDIFraction;
import org.wildfly.swarm.ejb.EJBFraction;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Scott Marlow
 */
@RunWith(Arquillian.class)
public class Neo4jIT {

    @ArquillianResource
    InitialContext context;

    @Deployment
    public static Archive createDeployment() throws Exception {
        WARArchive archive = ShrinkWrap.create(WARArchive.class, "Neo4jIT.war");
        archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        archive.addAsWebResource("project-defaults.yml");
        archive.addPackage("org.wildfly.swarm.neo4j.test");
        archive.addAllDependencies();
        return archive;
    }

    @Test
    public void resourceLookup() throws Exception {
        Object connection = context.lookup("java:jboss/neo4jdriver/test");
        assertNotNull(connection);
    }

    @Inject
    @Named("neo4jtestprofile")
    Driver database;
    // Object database;

    @Test
    public void injectDatabaseConnection() throws Exception {
        assertNotNull(database);
    }

    @EJB(lookup = "java:global/Neo4jIT/StatefulTestBean")
        private StatefulTestBean statefulTestBean;

    @Test
    public void testSimpleCreateAndLoadEntities() throws Exception {
        String result = statefulTestBean.addPerson();
        assertEquals("Record<{name: \"Arthur\", title: \"King\"}>", result);
    }

    @Test
    public void testInjectedClassInstance() throws Exception {
        String result = statefulTestBean.addPersonClassInstanceInjection();
        assertEquals("Record<{name: \"CDI\", title: \"King\"}>", result);
    }

    /**
     * Verify that calling a session bean method that starts a JTA transaction, adds a database value and then calls nested bean method that
     * requires a new transaction, the nested bean method should not be able to read the database value as the controlling JTA transaction did
     * not get committed yet.
     */
    @Test
    public void testTransactionEnlistmentReadAfterTransactionClose() throws Exception {
        String result = statefulTestBean.transactionEnlistmentReadAfterCallingTransactionClose();
        if (result.equals("Record<{name: \"TRANSACTION\", title: \"King\"}>")) {
            fail("Should not be able to read 'TRANSACTION' value from database as the JTA transaction did not end yet.");
        }
        else if (result.equals("TRANSACTION not found")) {
            // success!
            // we expect that the database add of "TRANSACTION" will not occur yet, since the JTA transaction has
            // not ended when we attempt to read the "TRANSACTION" value.  "TRANSACTION not found" is the expected response.
        }
        else {
            fail("unexpected result = " + result);
        }
    }

    @EJB(lookup = "java:global/Neo4jIT/BMTStatefulTestBean")
        private BMTStatefulTestBean bmtStatefulTestBean;

    @Test
    public void testBMT() throws Exception {
        String result = bmtStatefulTestBean.twoTransactions();
        assertEquals(result,"Record<{name: \"BMT\", title: \"King\"}>");
    }

}
