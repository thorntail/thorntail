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
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateful;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Stateful
public class StatefulTestBean {

    @Resource(lookup = "java:jboss/orientdb/test")
    private OPartitionedDatabasePool databasePool;

    private TestPeopleDao peopleDao;

    @PostConstruct
    private void init() {
        peopleDao = new TestPeopleDao(databasePool);
    }

    public ODocument addPerson(String name) {
        return peopleDao.addPerson(name);
    }

    public List<ODocument> getPeople() {
        return peopleDao.getPeople();
    }

    public OrientEdge addFriend(String outName, String inName) {
        return peopleDao.addFriend(outName, inName);
    }

    public List<Edge> getFriends() {
        return peopleDao.getFriends();
    }

    public ClassLoader getNoSQLClassLoader() {
        return OPartitionedDatabasePool.class.getClassLoader();
    }

}
