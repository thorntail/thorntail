package io.thorntail.config.impl.converters.fallback;

public class StaticValueOfConverter extends SimpleStaticMethodConverter {
    public StaticValueOfConverter() {
        super("valueOf", String.class);
    }
}
