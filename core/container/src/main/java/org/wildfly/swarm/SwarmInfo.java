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
package org.wildfly.swarm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by bob on 5/17/17.
 */
public class SwarmInfo {

    private static final String SWARM_INFO_PROPERTIES = "swarm-info.properties";

    static {
        String version = "unknown";
        String groupId = "io.thorntail";

        try {
            InputStream in = SwarmInfo.class.getClassLoader().getResourceAsStream(SWARM_INFO_PROPERTIES);
            Properties props = new Properties();
            props.load(in);

            version = props.getProperty("version");
            groupId = props.getProperty("groupId");
        } catch (IOException e) {
            e.printStackTrace();
        }
        VERSION = version;
        GROUP_ID = groupId;
    }

    public static final String VERSION;

    public static final String GROUP_ID;

    public static boolean isProduct() {
        return VERSION.contains("redhat-");
    }

    private SwarmInfo() {
    }
}
