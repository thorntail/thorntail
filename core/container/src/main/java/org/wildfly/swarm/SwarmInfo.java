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
        String groupId = "org.wildfly.swarm";

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
        return VERSION.contains("-redhat-");
    }

    private SwarmInfo() {
    }
}
