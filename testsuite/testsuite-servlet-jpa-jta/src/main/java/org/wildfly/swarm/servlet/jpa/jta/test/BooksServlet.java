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
package org.wildfly.swarm.servlet.jpa.jta.test;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

/**
 * It is a bad idea to mix persistence logic into a servlet, but it is a good test.
 */
@WebServlet("/")
public class BooksServlet extends HttpServlet {
    @PersistenceUnit(unitName = "MyPU")
    private EntityManagerFactory emf;

    @Resource
    private UserTransaction tx;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        EntityManager em = emf.createEntityManager();
        try {
            List<Book> books = em.createNamedQuery("Book.getAll", Book.class).getResultList();
            for (Book book : books) {
                resp.getWriter().println(book.getId() + " " + book.getAuthor() + ": " + book.getTitle());
            }
        } finally {
            em.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String title = req.getParameter("title");
        String author = req.getParameter("author");

        EntityManager em = emf.createEntityManager();
        try {
            tx.begin();
            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            em.persist(book);
            tx.commit();

            resp.getWriter().println(book.getId());
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            em.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = Integer.parseInt(req.getParameter("id"));

        EntityManager em = emf.createEntityManager();
        try {
            tx.begin();
            Book book = em.find(Book.class, id);
            em.remove(book);
            tx.commit();

            resp.getWriter().println(id + " deleted");
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            em.close();
        }
    }
}
