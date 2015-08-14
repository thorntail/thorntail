package org.wildfly.swarm.undertow.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.builder.HandlerBuilder;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

/**
 * @author Bob McWhirter
 */
public class StaticHandlerBuilder implements HandlerBuilder {
    @Override
    public String name() {
        return "static-content";
    }

    @Override
    public Map<String, Class<?>> parameters() {
        HashMap<String,Class<?>> params = new HashMap<>();
        params.put( "base", String.class );
        params.put( "prefix", String.class );
        return params;
    }

    @Override
    public Set<String> requiredParameters() {
        return Collections.emptySet();
    }

    @Override
    public String defaultParameter() {
        return "";
    }

    @Override
    public HandlerWrapper build(Map<String, Object> map) {
        final String base = (String) map.get( "base" );
        final String prefix = (String) map.get( "prefix" );


        return new HandlerWrapper() {
            @Override
            public HttpHandler wrap(final HttpHandler next) {

                HttpHandler cur = next;

                if ( base != null ) {
                    cur = new StaticResourceHandler(prefix, new ClassPathResourceManager(ClassLoader.getSystemClassLoader(), base), cur);
                } else {
                    cur = new StaticResourceHandler(prefix, new ClassPathResourceManager(ClassLoader.getSystemClassLoader()), cur);
                }

                try {
                    Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.bootstrap"));
                    if ( base != null ) {
                        cur = new StaticResourceHandler(prefix, new ClassPathResourceManager(module.getClassLoader(), base), cur);
                    } else {
                        cur = new StaticResourceHandler(prefix, new ClassPathResourceManager(module.getClassLoader()), cur);
                    }
                } catch (ModuleLoadException e) {
                }

                Path f = Paths.get(System.getProperty("user.dir"), "target", "classes");
                if ( base != null ) {
                    f = f.resolve(base);
                }
                if (Files.exists(f)) {
                    cur = new StaticResourceHandler(prefix, new FileResourceManager(f.toFile(), 1024), cur);
                }

                f = Paths.get(System.getProperty("user.dir"), "src", "main", "webapp");
                if ( base != null ) {
                    f = f.resolve(base);
                }
                if (Files.exists(f)) {
                    cur = new StaticResourceHandler(prefix, new FileResourceManager(f.toFile(), 1024), cur);
                }

                f = Paths.get(System.getProperty("user.dir"), "src", "main", "resources");
                if ( base != null ) {
                    f = f.resolve(base);
                }
                if (Files.exists(f)) {
                    cur = new StaticResourceHandler(prefix, new FileResourceManager(f.toFile(), 1024), cur);
                }

                return cur;
            }
        };
    }
}
