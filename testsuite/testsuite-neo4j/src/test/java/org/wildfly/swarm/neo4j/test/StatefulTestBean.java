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

import static org.junit.Assert.fail;

import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.inject.Named;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

/**
 * StatefulTestBean for the Neo4J database
 *
 * @author Scott Marlow
 */
@Stateful
public class StatefulTestBean {

    @Resource(lookup = "java:jboss/neo4jdriver/test")
    private Driver driver;

    @Inject
    @Named("neo4jtestprofile")
    private Driver injectedDriver;

    @Inject
    NestedBean nestedBean;


    public String addPerson() {
        Session session = driver.session();
        try {
            session.run("CREATE (a:Person {name:'Arthur', title:'King'})");
            StatementResult result = session.run("MATCH (a:Person) WHERE a.name = 'Arthur' RETURN a.name AS name, a.title AS title");


            Record record = result.next();
            return record.toString();
        } finally {
            session.run("MATCH (a:Person) delete a");
            session.close();
        }
    }

    public String addPersonClassInstanceInjection() {
        Session session = injectedDriver.session();
        try {
            session.run("CREATE (a:Person {name:'CDI', title:'King'})");
            StatementResult result = session.run("MATCH (a:Person) WHERE a.name = 'CDI' RETURN a.name AS name, a.title AS title");


            Record record = result.next();
            return record.toString();
        } finally {
            session.run("MATCH (a:Person) delete a");
            session.close();
        }
    }

    public String transactionEnlistmentReadAfterCallingTransactionClose() {
        // JTA transaction is started by CMT, the following obtains a Session that is enlisted into the JTA transaction.
        Session session = injectedDriver.session();
        // The only way to influence success/failure of the Neo4j + JTA transaction, is at the JTA transaction level.
        // If the JTA transaction fails, org.neo4j.driver.v1.Transaction.failure() is called.
        // If the JTA transaction succeeds, org.neo4j.driver.v1.Transaction.success() is called.
        // org.neo4j.driver.v1.Transaction.close() is also called when the JTA transaction ends.

        // Calls to Session.beginTransaction() in a JTA transaction are expected to throw a RuntimeException
        try {
            Transaction transaction = session.beginTransaction();
            fail("Calling Session.beginTransaction in a JTA transaction should throw a RuntimeException.");
        } catch (RuntimeException expectedException) {
            // success
        }

        try {
            session.run("CREATE (a:Person {name:'TRANSACTION', title:'King'})");
            return nestedBean.getPerson("TRANSACTION");
        } finally {
            if ( session.isOpen()) { // this will be true
                session.run("MATCH (a:Person) delete a");
                session.close();             // ignored, session is auto closed when the transaction ends.
            }
        }
    }
}
