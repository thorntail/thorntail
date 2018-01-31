package org.jboss.unimbus.config.impl.converters.fallback;

public class StaticValueOfConverter extends SimpleStaticMethodConverter {
    public StaticValueOfConverter() {
        super("valueOf", String.class);
    }
}
