package org.jboss.unimbus.config.mp.converters.fallback;

public class StaticParseConverter extends SimpleStaticMethodConverter {
    public StaticParseConverter() {
        super("parse", CharSequence.class);
    }
}
