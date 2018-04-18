package io.thorntail.servlet.impl.undertow.config;

import io.undertow.Undertow;

/**
 * Created by bob on 1/17/18.
 */
public interface UndertowConfigurer {
    void configure(Undertow.Builder builder);
}
