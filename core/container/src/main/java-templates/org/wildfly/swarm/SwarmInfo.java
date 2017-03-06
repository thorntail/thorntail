package org.wildfly.swarm;

/**
 * @author Ken Finnigan
 */
public interface SwarmInfo {

    final String VERSION = "${project.version}";

    final String GROUP_ID = "${project.groupId}";

    default boolean isProduct() {
        return VERSION.contains("-redhat-");
    }
}
