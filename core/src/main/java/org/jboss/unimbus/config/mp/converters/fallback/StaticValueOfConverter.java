package org.jboss.unimbus.config.mp.converters.fallback;

public class StaticValueOfConverter extends SimpleStaticMethodConverter {
    public StaticValueOfConverter() {
        super("valueOf", String.class);
    }
}
