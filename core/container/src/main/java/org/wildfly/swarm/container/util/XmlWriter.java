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
package org.wildfly.swarm.container.util;

import java.io.IOException;
import java.io.Writer;

import javax.enterprise.inject.Vetoed;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class XmlWriter implements AutoCloseable {

    public XmlWriter(Writer out) {
        this.out = out;
    }

    public Element element(String name) throws IOException {
        return new Element(name);
    }

    public void close() throws IOException {
        this.out.close();
    }

    private final Writer out;

    @Vetoed
    public class Element {
        Element(String name) throws IOException {
            this.name = name;
            out.write("<" + name);
        }

        public Element attr(String name, String value) throws IOException {
            out.write(" " + name + "=\"" + value + "\"");
            return this;
        }

        public Element element(String name) throws IOException {
            if (!this.hasContent) {
                this.hasContent = true;
                out.write(">");
            }
            return new Element(name);
        }

        public Element content(String content) throws IOException {
            if (!this.hasContent) {
                this.hasContent = true;
                out.write(">");
            }

            out.write(content);
            return this;
        }

        public void end() throws IOException {
            if (hasContent) {
                out.write("</" + name + ">");
            } else {
                out.write("/>");
            }
        }

        private String name;

        private boolean hasContent = false;
    }
}
