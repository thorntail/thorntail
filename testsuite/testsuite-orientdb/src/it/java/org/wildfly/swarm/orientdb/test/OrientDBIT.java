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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalTime;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.cdi.CDIFraction;
import org.wildfly.swarm.ejb.EJBFraction;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Scott Marlow
 */
@RunWith(Arquillian.class)
public class OrientDBIT extends AbstractTestCase {

    @ArquillianResource
    InitialContext context;

    @Deployment
    public static Archive createDeployment() throws Exception {
        WARArchive archive = ShrinkWrap.create(WARArchive.class, "OrientDBIT.war");
        archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        archive.addAsWebResource("project-defaults.yml");
        archive.addPackage("org.wildfly.swarm.orientdb.test");
        archive.addAllDependencies();
        return archive;
    }

    @Test
    public void resourceLookup() throws Exception {
        Object connection = context.lookup("java:jboss/orientdb/test");
        assertNotNull(connection);
    }
    
    @Inject
    @Named("orienttesttprofile")
    // private Object databasePool;
    private OPartitionedDatabasePool databasePool;

    @Test
    public void injectDatabaseConnection() throws Exception {
       assertNotNull(databasePool);
    }

    @EJB(lookup = "java:global/OrientDBIT/StatefulTestBean")
        private StatefulTestBean statefulTestBean;

    @Test
    public void shouldAddAPersonToTheDatabase() {
        String name = "test-name-" + LocalTime.now();
        ODocument person = statefulTestBean.addPerson(name);
        assertEquals(name, person.field("name"));

        List<ODocument> people = statefulTestBean.getPeople();
        assertEquals(1, people.size());
        assertEquals(person, people.get(0));
    }

    @Test
    public void shouldAddAFriendshipToTheGraph() {
        String firstName = "test-name-" + LocalTime.now();
        String secondName = "test-name-" + LocalTime.now();
        OrientEdge edge = statefulTestBean.addFriend(firstName, secondName);
        assertEquals(firstName, edge.getVertex(Direction.OUT).getProperty("name"));
        assertEquals(secondName, edge.getVertex(Direction.IN).getProperty("name"));
        assertEquals("knows", edge.getLabel());

        List<Edge> edges = statefulTestBean.getFriends();
        assertEquals(1, edges.size());
        assertEquals(edge, edges.get(0));
    }

}
