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

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
@Transactional
public class BookService {
    @PersistenceContext(unitName = "MyPU")
    private EntityManager em;

    public List<Book> getAll() {
        return em.createNamedQuery("Book.getAll", Book.class).getResultList();
    }

    public Book create(String title, String author) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        em.persist(book);
        return book;
    }

    public void delete(int id) {
        Book book = em.find(Book.class, id);
        em.remove(book);
    }
}
