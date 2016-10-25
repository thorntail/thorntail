/*
 * #%L
 * Camel JPA :: Tests
 * %%
 * Copyright (C) 2016 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.wildfly.swarm.camel.test.jpa;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jpa.JpaComponent;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extension.camel.CamelContextRegistry;
import org.wildfly.swarm.arquillian.DefaultDeployment;
import org.wildfly.swarm.camel.test.jpa.subA.Account;

@RunWith(Arquillian.class)
@DefaultDeployment
public class JPATransactionManagerIntegrationTest {

    @Test
    public void testJpaTransactionManagerRoute() throws Exception {

        CamelContext camelctx = getContextRegistry().getCamelContext("jpa-context");
        Assert.assertNotNull("Expected jpa-context to not be null", camelctx);

        // Persist a new account entity
        Account account = new Account(1, 500);
        camelctx.createProducerTemplate().sendBody("direct:start", account);

        JpaComponent component = camelctx.getComponent("jpa", JpaComponent.class);
        EntityManagerFactory entityManagerFactory = component.getEntityManagerFactory();

        // Read the saved entity back from the database
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Account result = em.getReference(Account.class, 1);
        em.getTransaction().commit();

        Assert.assertEquals(account, result);
    }

	private CamelContextRegistry getContextRegistry() throws NamingException {
		return (CamelContextRegistry) new InitialContext().lookup("java:jboss/camel/CamelContextRegistry");
	}
}
