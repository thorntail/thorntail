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
package org.wildfly.swarm.management;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Properties;

import org.wildfly.swarm.config.management.security_realm.PlugInAuthentication;

/**
 * @author Bob McWhirter
 */
public class InMemoryAuthentication {

    public InMemoryAuthentication(String realm, PlugInAuthentication plugin) {
        this.realm = realm;
        this.plugin = plugin;
    }

    public void add(Properties props) {
        add(props, false);
    }

    public void add(Properties props, boolean plainText) {
        Enumeration<?> userNames = props.propertyNames();

        while (userNames.hasMoreElements()) {
            String userName = (String) userNames.nextElement();
            String value = props.getProperty(userName);

            add(userName, value, plainText);
        }
    }

    public void add(String userName, String password) {
        add(userName, password, false);
    }

    public void add(String userName, String password, boolean plainText) {
        if (plainText) {
            try {
                String str = userName + ":" + this.realm + ":" + password;
                MessageDigest digest = MessageDigest.getInstance("MD5");
                byte[] hash = digest.digest(str.getBytes());
                add(userName, hash);
            } catch (NoSuchAlgorithmException e) {
                ManagementMessages.MESSAGES.unknownAlgorithm("MD5", e);
            }
        } else {
            this.plugin.property(userName + ".hash", (prop) -> {
                prop.value(password);
            });
        }
    }

    public void add(String userName, byte[] hash) {
        this.plugin.property(userName + ".hash", (prop) -> {
            StringBuilder str = new StringBuilder();
            for (byte b : hash) {
                int i = b;
                String part = Integer.toHexString(b);
                if (part.length() > 2) {
                    part = part.substring(part.length() - 2);
                } else if (part.length() < 2) {
                    part = "0" + part;
                }
                str.append(part);
            }

            prop.value(str.toString());
        });
    }

    private final String realm;

    private final PlugInAuthentication plugin;

}
