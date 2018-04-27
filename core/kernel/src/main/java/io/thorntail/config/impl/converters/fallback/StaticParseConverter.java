package io.thorntail.config.impl.converters.fallback;

public class StaticParseConverter extends SimpleStaticMethodConverter {
    public StaticParseConverter() {
        super("parse", CharSequence.class);
    }
}
