/**
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
package org.wildfly.swarm.cdi.beanvalidation.test;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class BooksService {
    private AtomicLong idGenerator = new AtomicLong();

    private List<Book> books = new CopyOnWriteArrayList<>();

    public List<Book> list() {
        return books;
    }

    @Valid
    public Book create(
            @Isbn(message = "ISBN must be set to a valid value")
            String isbn,
            @NotNull(message = "Title must be set")
            String title,
            @NotNull(message = "Author must be set")
            String author
    ) {
        Book book = new Book(idGenerator.incrementAndGet(), isbn, title, author);
        books.add(book);
        return book;
    }
}
