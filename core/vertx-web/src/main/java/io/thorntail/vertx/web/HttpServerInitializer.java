package io.thorntail.vertx.web;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;

import org.eclipse.microprofile.config.Config;

import io.thorntail.vertx.VertxProperties;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Initializes an {@link HttpServer} instance and triggers registration of various components.
 *
 * <p>
 * Configuration of the {@link HttpServer} instance is performed using the built-in mechanisms. The list of supported property names is not defined. Instead,
 * properties with {@link #PROPERTY_PREFIX} prefix can be mapped to setter methods of the {@link HttpServerOptions} object. A property name consits of the
 * prefix and dot separated parts derived from the setter name. For example, the {@link HttpServerOptions#setPort(int)} setter method is mapped to the
 * {@code vertx.web.port} property. However, complex data structures are not supported - for example it is not possible to configure
 * {@link HttpServerOptions#setClientAuth(io.vertx.core.http.ClientAuth)}.
 * </p>
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class HttpServerInitializer {

    private static final String PROPERTY_PREFIX = VertxProperties.PROPERTY_PREFIX + ".web";

    private HttpServer httpServer;

    void init(@Observes Vertx vertx, BeanManager beanManager, Config config) throws InstantiationException, IllegalAccessException {

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        beanManager.getExtension(RouteExtension.class).registerRoutes(router);

        // Make it possible to add further route handlers
        beanManager.getEvent().select(Router.class).fire(router);

        HttpServerOptions options = VertxProperties.createOptions(HttpServerOptions.class, config, PROPERTY_PREFIX);
        VertxWebLogger.LOG.usingOptions(options.toJson());

        httpServer = vertx.createHttpServer(options).requestHandler(router::accept).listen();
        VertxWebLogger.LOG.httpServerListening(httpServer.actualPort());
    }

}
