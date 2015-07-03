package org.wildfly.swarm.plugin.gradle;

/**
 * @author Bob McWhirter
 */
public class SwarmExtension {
    private String mainClass;

    public SwarmExtension() {

    }

    public void setMainClassName(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMainClassName() {
        return this.mainClass;
    }
}
