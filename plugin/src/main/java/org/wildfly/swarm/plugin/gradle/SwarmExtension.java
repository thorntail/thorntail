package org.wildfly.swarm.plugin.gradle;

/**
 * @author Bob McWhirter
 */
public class SwarmExtension {
    private String mainClass;

    public SwarmExtension() {

    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMainClass() {
        return this.mainClass;
    }
}
