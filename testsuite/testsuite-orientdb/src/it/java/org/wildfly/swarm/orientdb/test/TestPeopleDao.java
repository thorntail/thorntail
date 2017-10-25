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
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestPeopleDao {

    private final OPartitionedDatabasePool databasePool;

    public TestPeopleDao(OPartitionedDatabasePool databasePool) {
        this.databasePool = databasePool;
    }

    public ODocument addPerson(String name) {
        try (ODatabaseDocumentTx database = databasePool.acquire()) {
            ODocument document = new ODocument("Person");
            document.field("name", name);
            database.commit();
            return document.save();
        }
    }

    public List<ODocument> getPeople() {
        try (ODatabaseDocumentTx database = databasePool.acquire()) {
            OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("SELECT * FROM Person");
            return database.command(query).execute();
        }
    }

    public OrientEdge addFriend(String outName, String inName) {
        OrientGraph database = new OrientGraph(databasePool);
        try {
            Vertex outVertex = database.addVertex(null);
            Vertex inVertex = database.addVertex(null);
            outVertex.setProperty("name", outName);
            inVertex.setProperty("name", inName);
            OrientEdge edge = database.addEdge(null, outVertex, inVertex, "knows");
            database.commit();
            return edge;
        } catch (Exception e) {
            database.rollback();
        } finally {
            database.shutdown();
        }

        return null;
    }

    public List<Edge> getFriends() {
        List<Edge> edges = new LinkedList<>();
        OrientGraph database = new OrientGraph(databasePool);
        try {
            database.getEdges().forEach(edges::add);
        } finally {
            database.shutdown();
        }

        return edges;
    }

}
