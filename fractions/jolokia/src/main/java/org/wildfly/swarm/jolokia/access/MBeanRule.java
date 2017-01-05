package org.wildfly.swarm.jolokia.access;

import java.util.ArrayList;
import java.util.List;

/**
 * An MBean rule.
 *
 * @author Bob McWhirter
 */
public class MBeanRule {

    MBeanRule(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    /**
     * An operation name or pattern.
     *
     * @param operation The operation.
     * @return This rule.
     */
    public MBeanRule operation(String operation) {
        this.operations.add(operation);
        return this;
    }

    public List<String> operations() {
        return this.operations;
    }

    /**
     * An attribute name or pattern.
     *
     * @param attribute The attribute.
     * @return This rule.
     */
    public MBeanRule attribute(String attribute) {
        this.attributes.add(attribute);
        return this;
    }

    public List<String> attributes() {
        return this.attributes;
    }

    private String name;

    private List<String> operations = new ArrayList<>();

    private List<String> attributes = new ArrayList<>();

    public interface Consumer extends java.util.function.Consumer<MBeanRule> {
    }
}
