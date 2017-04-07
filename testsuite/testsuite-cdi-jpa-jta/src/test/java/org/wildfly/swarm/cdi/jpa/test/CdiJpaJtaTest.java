/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.cdi.jpa.test;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import javax.inject.Inject;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
@DefaultDeployment(type = DefaultDeployment.Type.JAR)
public class CdiJpaJtaTest {
    private static int id = -1;

    @Inject
    private BookService books;

    @Inject
    private UserTransaction tx;

    @Test
    @InSequence(1)
    public void empty() {
        assertTrue(books.getAll().isEmpty());
    }

    @Test
    @InSequence(2)
    public void create() {
        Book book = books.create("Title", "Author");
        assertNotNull(book);
        id = book.getId();
    }

    @Test
    @InSequence(3)
    public void notEmpty() {
        assertEquals(1, books.getAll().size());
    }

    @Test
    @InSequence(4)
    public void delete() {
        books.delete(id);
    }

    @Test
    @InSequence(5)
    public void emptyAgain() {
        assertTrue(books.getAll().isEmpty());
    }

    @Test
    @InSequence(6)
    public void userTransaction() throws SystemException {
        assertNotNull(tx);
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());
    }
}
