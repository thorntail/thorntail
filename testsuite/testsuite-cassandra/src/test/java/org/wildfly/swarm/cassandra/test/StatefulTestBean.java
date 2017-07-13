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

package org.wildfly.swarm.cassandra.test;

import javax.annotation.Resource;
import javax.ejb.Stateful;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * StatefulTestBean
 *
 * @author Scott Marlow
 */
@Stateful
public class StatefulTestBean {
    @Resource(lookup = "java:jboss/cassandradriver/test")
    private Cluster cluster;
    private Session session;

    public Row query() {
        openConnection();
        try {
            session.execute("CREATE TABLE employee (lastname varchar primary key, firstname varchar, age int, city varchar, email varchar)");
            session.execute("INSERT INTO employee (lastname, firstname, age, city, email) VALUES ('Smith','Leanne', 30, 'Boston', 'lea@yahoo.com')");
            session.execute("update employee set age = 36 where lastname = 'Smith'");
            // Select and show the change
            ResultSet results = session.execute("select * from employee where lastname='Smith'");
            return results.one();
        } finally {
            try {
                session.execute("DROP TABLE employee");
            } catch (Throwable ignore) {
            }

            closeConnection();
        }
    }

    public Row asyncQuery() {
        openConnection();
        try {
            session.execute("CREATE TABLE employee (lastname varchar primary key, firstname varchar, age int, city varchar, email varchar)");
            session.execute("INSERT INTO employee (lastname, firstname, age, city, email) VALUES ('Smith','Leanne', 30, 'Boston', 'lea@yahoo.com')");
            session.execute("update employee set age = 36 where lastname = 'Smith'");
            // Select and show the change
            try {
                ResultSetFuture results = session.executeAsync("select * from employee where lastname='Smith'");
                return results.get().one();
            } catch( Throwable exception) {
                throw new RuntimeException("could not get executeAsync result for some reason", exception);
            }
        } finally {
            try {
                session.execute("DROP TABLE employee");
            } catch (Throwable ignore) {
            }

            closeConnection();
        }
    }

    private void openConnection() {
        session = cluster.connect();
        session.execute("CREATE KEYSPACE IF NOT EXISTS testspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session = cluster.connect("testspace");
    }

    private void closeConnection() {
        session.execute("DROP KEYSPACE testspace");
        session.close();
        session = null;
    }

}
