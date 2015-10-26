/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.mail;

/**
 * @author Ken Finnigan
 */
public class SmtpServer {

    private final String name;

    private String host;

    private String port;

    private String outboundSocketBindingRef = "mail-smtp";

    public SmtpServer() {
        this.name = "Default";
    }

    public SmtpServer(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public SmtpServer host(String host) {
        this.host = host;
        return this;
    }

    public String host() {
        return this.host;
    }

    public SmtpServer port(String port) {
        this.port = port;
        return this;
    }

    public String port() {
        return this.port;
    }

    public SmtpServer outboundSocketBindingRef(String bindingReference) {
        this.outboundSocketBindingRef = bindingReference;
        return this;
    }

    public String outboundSocketBindingRef() {
        return this.outboundSocketBindingRef;
    }

    public String jndiName() {
        return "java:jboss/mail/" + this.name;
    }
}
