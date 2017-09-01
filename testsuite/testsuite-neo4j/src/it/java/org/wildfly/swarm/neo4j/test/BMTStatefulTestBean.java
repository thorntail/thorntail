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
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.ejb.Stateful;
import javax.transaction.UserTransaction;
import javax.transaction.Status;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

/**
 * test with bean managed transactions
 *
 * @author Scott Marlow
 */
@Stateful
@TransactionManagement(TransactionManagementType.BEAN)
public class BMTStatefulTestBean {

    @Resource
    private UserTransaction userTransaction;

    @Resource(lookup = "java:jboss/neo4jdriver/test")
    private Driver driver;

    public String twoTransactions() throws Exception {
        // start the JTA transaction via javax.transaction.UserTransaction
        userTransaction.begin();

        try {
            // obtain session which will be enlisted into the JTA transaction.
            Session session = driver.session();

            if (session != driver.session()) {
                throw new RuntimeException("multiple calls to Driver.session() must return the same session within JTA transaction.");
            }

            // Calls to Session.beginTransaction() in a JTA transaction are expected to throw a RuntimeException
            try {
                Transaction transaction = session.beginTransaction();
                fail("Calling Session.beginTransaction in a JTA transaction should throw a RuntimeException.");
            } catch (RuntimeException expectedException) {
                // success
            }

            session.run("CREATE (a:Person {name:'BMT', title:'King'})");

            // calls to close the session should also be ignored, since the session is also considered to be enlisted into the JTA transaction
            session.close();
            if (session.isOpen() != true) {
                throw new RuntimeException("Session should be open since JTA transaction is still active.");
            }

            // commit the JTA transaction, which also calls org.neo4j.driver.v1.Transaction.success()/close().
            // if the JTA transaction rolls back, org.neo4j.driver.v1.Transaction.failure()/close() would instead be called.
            userTransaction.commit();

            if (session.isOpen() != false) {
                throw new RuntimeException("Session should now be closed since JTA transaction ended.");
            }
            session.close();    // should be ignored
            // Start another JTA transaction, note that the session has to be obtained again
            userTransaction.begin();
            session = driver.session();

            if (session != driver.session()) {
                throw new RuntimeException("multiple calls to Driver.session() must return the same session within JTA transaction.");
            }

            // Calls to Session.beginTransaction() in a JTA transaction are expected to throw a RuntimeException
            try {
                Transaction transaction = session.beginTransaction();
                fail("Calling Session.beginTransaction in a JTA transaction should throw a RuntimeException.");
            } catch (RuntimeException expectedException) {
                // success
            }

            StatementResult result = session.run("MATCH (a:Person) WHERE a.name = 'BMT' RETURN a.name AS name, a.title AS title");
            Record record = result.next();
            return record.toString();
        } finally {
            if(userTransaction.getStatus() == Status.STATUS_ACTIVE) {
                userTransaction.commit();     // second JTA transaction is ended, which also closes the enlisted org.neo4j.driver.v1.Transaction/Session
            }
            Session cleanupSession = driver.session();
            cleanupSession.run("MATCH (a:Person) delete a");
            cleanupSession.close();
        }
    }
}
