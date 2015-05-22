package org.wildfly.swarm.datasources;

/**
 * @author Bob McWhirter
 */
public class Driver {

    private final String name;
    private String moduleName;
    private String moduleSlot;
    private String datasourceClassName;
    private String xaDatasourceClassName;

    public Driver(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public Driver module(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public Driver module(String moduleName, String moduleSlot) {
        this.moduleName = moduleName;
        this.moduleSlot = moduleSlot;
        return this;
    }

    public String moduleName() {
        return this.moduleName;
    }

    public String moduleSlot() {
        return this.moduleSlot;
    }

    public Driver datasourceClassName(String className) {
        this.datasourceClassName = className;
        return this;
    }

    public String datasourceClassName() {
        return this.datasourceClassName;
    }

    public Driver xaDatasourceClassName(String className) {
        this.xaDatasourceClassName = className;
        return this;
    }

    public String xaDatasourceClassName() {
        return this.xaDatasourceClassName;
    }

}
