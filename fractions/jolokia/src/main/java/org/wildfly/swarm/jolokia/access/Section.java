package org.wildfly.swarm.jolokia.access;

import java.util.ArrayList;
import java.util.List;

/**
 * An allow or deny section.
 *
 * @author Bob McWhirter
 */
public class Section {

    public static Section allow() {
        return new Section(Type.allow);
    }

    public static Section deny() {
        return new Section(Type.deny);
    }

    private enum Type {
        allow,
        deny
    }

    private Section(Type type) {
        this.type = type;
    }

    public String type() {
        return this.type.toString();
    }

    /**
     * Define a rule for a given MBean.
     *
     * @param name   The mbean name or pattern.
     * @param config Configuration.
     * @return This section.
     */
    public Section mbean(String name, MBeanRule.Consumer config) {
        MBeanRule rule = new MBeanRule(name);
        config.accept(rule);
        this.rules.add(rule);
        return this;
    }

    public List<MBeanRule> mbeans() {
        return this.rules;
    }

    private Type type;

    private List<MBeanRule> rules = new ArrayList<>();

    public interface Consumer extends java.util.function.Consumer<Section> {
    }
}
