/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.undertow.staticcontent;

public interface StaticContentCommonTests {

    default void assertBasicStaticContentWorks(String context) throws Exception {
        if (context.length() > 0 && !context.endsWith("/")) {
            context = context + "/";
        }
        assertContains(context + "static-content.txt", "This is static.");
        assertContains(context + "index.html", "This is index.html.");
        assertContains(context + "foo/index.html", "This is foo/index.html.");
        // Ensure index files are used
        assertContains(context, "This is index.html.");
        assertContains(context + "foo", "This is foo/index.html.");
        // Ensure we don't serve up Java class files
        assertNotFound(context + "java/lang/Object.class");
        // And doubly ensure we don't serve up application class files
        assertNotFound(context + this.getClass().getName().replace(".", "/") + ".class");
    }

    void assertContains(String path, String content) throws Exception;

    void assertNotFound(String path) throws Exception;
}
