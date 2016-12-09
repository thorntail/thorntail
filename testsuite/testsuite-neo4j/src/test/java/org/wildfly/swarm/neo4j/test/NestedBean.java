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

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.inject.Named;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

/**
 * NestedBean
 *
 * @author Scott Marlow
 */
@Remote
@Stateful
public class NestedBean {

    @Inject
    @Named("neo4jtestprofile")
    private Driver injectedDriver;

    @TransactionAttribute(REQUIRES_NEW)
    public String getPerson(String name) {
        Session session = injectedDriver.session();
        try {
            StatementResult result = session.run("MATCH (a:Person) WHERE a.name = '" + name + "' RETURN a.name AS name, a.title AS title");
            if (result.hasNext()) {
                Record record = result.next();
                return record.toString();
            }
            return name + " not found";
        }
        finally {
            session.close();
        }
    }

}
