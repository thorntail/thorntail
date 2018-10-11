/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.wildfly.swarm.spi.api.config.ConfigKey;

/**
 * @author Bob McWhirter
 */
public class YamlDumper {

    public static void dump(PrintStream out, Properties props) {
        new YamlDumper(out, props).dump();
    }

    private final Node root = new Node("thorntail");

    private final PrintStream out;

    private YamlDumper(PrintStream out, Properties props) {
        this.out = out;

        props.stringPropertyNames().stream()
                .sorted()
                .forEach(key -> {
                    integrate(key, props.getProperty(key));
                });

    }

    private void dump() {
        dump(this.root);
    }

    private void integrate(String keyStr, String docs) {
        ConfigKey key = ConfigKey.parse(keyStr);
        this.root.integrate(key.subkey(1), key, docs);
    }

    private void dump(Node node) {
        dump("", node);
    }

    private void dump(String indent, Node node) {
        this.out.print(indent);
        this.out.print(node.name());
        this.out.print(":");
        if (node.children().isEmpty()) {
            this.out.print(" <value>");
            this.out.println();
        } else {
            this.out.println();
            for (Node child : node.children) {
                dump(indent + "  ", child);
            }
        }
    }

    private static class Node {

        private final String name;

        private final List<Node> children = new ArrayList<>();

        private ConfigKey key;

        private String docs;

        Node(String name) {
            this.name = name;
        }

        String name() {
            return this.name;
        }

        void integrate(ConfigKey remainingKey, ConfigKey actualKey, String docs) {
            Node next = null;
            for (Node child : this.children) {
                if (child.name().equals(remainingKey.head().name())) {
                    next = child;
                    break;
                }
            }
            if (next == null) {
                next = new Node(remainingKey.head().name());
                this.children.add(next);
            }

            if (remainingKey.subkey(1) == ConfigKey.EMPTY) {
                next.define(actualKey, docs);
            } else {
                next.integrate(remainingKey.subkey(1), actualKey, docs);
            }
        }

        void define(ConfigKey key, String docs) {
            this.key = key;
            this.docs = docs;
        }

        ConfigKey key() {
            return this.key;
        }

        String docs() {
            return this.docs;
        }

        void addChild(Node node) {
            this.children.add(node);
        }

        List<Node> children() {
            return this.children;
        }
    }
}
