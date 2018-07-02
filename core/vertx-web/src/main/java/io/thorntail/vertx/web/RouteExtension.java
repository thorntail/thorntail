package io.thorntail.vertx.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import io.thorntail.vertx.web.WebRoute.WebRoutes;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Collects and registers {@link Route} handlers and observers discovered during container initialization.
 *
 * @author Martin Kouba
 * @see WebRoute
 */
public class RouteExtension implements Extension {

    private final List<AnnotatedType<? extends Handler<RoutingContext>>> handlerTypes = new LinkedList<>();

    private final List<HandlerInstance<?>> handlerInstances = new LinkedList<>();

    private final List<RouteObserver> routeObservers = new LinkedList<>();

    private BeanManager beanManager;

    // Implementation note - ProcessAnnotatedType<? extends Handler<RoutingContext>> is more correct but prevents Weld from using
    // FastProcessAnnotatedTypeResolver
    @SuppressWarnings({ "unchecked" })
    void processHandlerAnnotatedType(@Observes @WithAnnotations({ WebRoute.class, WebRoutes.class }) ProcessAnnotatedType<?> event, BeanManager beanManager) {
        AnnotatedType<?> annotatedType = event.getAnnotatedType();
        if (isWebRoute(annotatedType) && isRouteHandler(annotatedType)) {
            VertxWebLogger.LOG.routeHandlerFound(annotatedType);
            // At this point it is safe to cast the annotated type
            handlerTypes.add((AnnotatedType<? extends Handler<RoutingContext>>) annotatedType);
        } else {
            // Collect route observer methods
            Map<String, RouteObserverId> routes = new HashMap<>();
            for (AnnotatedMethod<?> method : annotatedType.getMethods()) {
                WebRoute[] webRoutes = getWebRoutes(method);
                if (webRoutes.length > 0) {
                    if (!hasEventParameter(method)) {
                        VertxWebLogger.LOG.ignoringNonObserverMethod(method.getJavaMember().toGenericString());
                        continue;
                    }
                    VertxWebLogger.LOG.routeObserverFound(method.getJavaMember().toGenericString());
                    RouteObserverId id = RouteObserverId.Literal.of(UUID.randomUUID().toString());
                    routeObservers.add(new RouteObserver(id, webRoutes, method));
                    routes.put(method.getJavaMember().toGenericString(), id);
                }
            }
            if (!routes.isEmpty()) {
                // We need to add id qualifier to the event param
                event.configureAnnotatedType().methods().forEach(m -> {
                    RouteObserverId id = routes.get(m.getAnnotated().getJavaMember().toGenericString());
                    if (id != null) {
                        m.filterParams(p -> p.isAnnotationPresent(Observes.class)).findFirst().ifPresent(param -> {
                            param.add(id);
                            VertxWebLogger.LOG.addIdQualifier(id, param.getAnnotated());
                        });
                    }
                });
            }
        }
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    void beforeShutdown(@Observes BeforeShutdown event) {
        for (HandlerInstance<?> handler : handlerInstances) {
            handler.dispose();
        }
        handlerInstances.clear();
        handlerTypes.clear();
        routeObservers.clear();
    }

    public void registerRoutes(Router router) {
        for (AnnotatedType<? extends Handler<RoutingContext>> annotatedType : handlerTypes) {
            processHandlerType(annotatedType, router);
        }
        for (RouteObserver routeObserver : routeObservers) {
            routeObserver.process(router);
        }
    }

    public boolean isHandlerType(Class<?> clazz) {
        for (AnnotatedType<? extends Handler<RoutingContext>> handlerType : handlerTypes) {
            if (handlerType.getJavaClass().equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRouteObserver(Method method) {
        for (RouteObserver observer : routeObservers) {
            if (observer.matches(method)) {
                return true;
            }
        }
        return false;
    }

    private void processHandlerType(AnnotatedType<? extends Handler<RoutingContext>> annotatedType, Router router) {
        WebRoute[] webRoutes = getWebRoutes(annotatedType);
        if (webRoutes.length == 0) {
            return;
        }
        HandlerInstance<?> handlerInstance = new HandlerInstance<>(annotatedType, beanManager);
        handlerInstances.add(handlerInstance);
        Handler<RoutingContext> handler = handlerInstance.instance;
        for (WebRoute webRoute : webRoutes) {
            addRoute(router, handler, webRoute);
        }
    }

    private static void addRoute(Router router, Handler<RoutingContext> handler, WebRoute webRoute) {
        Route route;
        if (!webRoute.regex().isEmpty()) {
            route = router.routeWithRegex(webRoute.regex());
        } else if (!webRoute.path().isEmpty()) {
            route = router.route(webRoute.path());
        } else {
            route = router.route();
        }
        if (webRoute.methods().length > 0) {
            for (HttpMethod method : webRoute.methods()) {
                route.method(method);
            }
        }
        if (webRoute.order() != Integer.MIN_VALUE) {
            route.order(webRoute.order());
        }
        if (webRoute.produces().length > 0) {
            for (String produces : webRoute.produces()) {
                route.produces(produces);
            }
        }
        if (webRoute.consumes().length > 0) {
            for (String consumes : webRoute.consumes()) {
                route.consumes(consumes);
            }
        }
        switch (webRoute.type()) {
            case NORMAL:
                route.handler(handler);
                break;
            case BLOCKING:
                // We don't mind if blocking handlers are executed in parallel
                route.blockingHandler(handler, false);
                break;
            case FAILURE:
                route.failureHandler(handler);
                break;
            default:
                throw new IllegalStateException("Unsupported handler type: " + webRoute.type());
        }
        VertxWebLogger.LOG.routeRegistered(webRoute);
    }

    private WebRoute[] getWebRoutes(Annotated annotated) {
        WebRoute webRoute = annotated.getAnnotation(WebRoute.class);
        if (webRoute != null) {
            return new WebRoute[] { webRoute };
        }
        Annotation container = annotated.getAnnotation(WebRoutes.class);
        if (container != null) {
            WebRoutes webRoutes = (WebRoutes) container;
            return webRoutes.value();
        }
        return new WebRoute[] {};
    }

    private boolean isWebRoute(Annotated annotated) {
        return annotated.isAnnotationPresent(WebRoute.class) || annotated.isAnnotationPresent(WebRoutes.class);
    }

    private boolean isRouteHandler(AnnotatedType<?> annotatedType) {
        if (!isTopLevelOrStaticNestedClass(annotatedType.getJavaClass())) {
            VertxWebLogger.LOG.classNotTopLevelOrStaticNested(annotatedType.getJavaClass());
            return false;
        }
        for (Type type : annotatedType.getTypeClosure()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getRawType().equals(Handler.class)) {
                    Type[] arguments = parameterizedType.getActualTypeArguments();
                    if (arguments.length == 1 && arguments[0].equals(RoutingContext.class)) {
                        return true;
                    }
                }
            }
        }
        VertxWebLogger.LOG.classNotHandler(annotatedType.getJavaClass());
        return false;
    }

    private static boolean isStaticNestedClass(Class<?> javaClass) {
        if (javaClass.getEnclosingConstructor() != null || javaClass.getEnclosingMethod() != null) {
            // Local or anonymous class
            return false;
        }
        if (javaClass.getEnclosingClass() != null) {
            // Extra check for anonymous class - http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8034044
            if (javaClass.isAnonymousClass()) {
                return false;
            }
            return Modifier.isStatic(javaClass.getModifiers());
        }
        return false;
    }

    private static boolean isTopLevelOrStaticNestedClass(Class<?> javaClass) {
        return javaClass.getEnclosingClass() == null || isStaticNestedClass(javaClass);
    }

    private static class HandlerInstance<T extends Handler<RoutingContext>> {

        private final AnnotatedType<T> annotatedType;

        private final CreationalContext<T> creationalContext;

        private final InjectionTarget<T> injectionTarget;

        private final T instance;

        HandlerInstance(AnnotatedType<T> annotatedType, BeanManager beanManager) {
            this.annotatedType = annotatedType;
            this.injectionTarget = beanManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);
            this.creationalContext = beanManager.createCreationalContext(null);
            this.instance = injectionTarget.produce(creationalContext);
            injectionTarget.inject(instance, creationalContext);
            injectionTarget.postConstruct(instance);
        }

        private void dispose() {
            try {
                injectionTarget.preDestroy(instance);
                injectionTarget.dispose(instance);
                creationalContext.release();
            } catch (Exception e) {
                VertxWebLogger.LOG.errorDisposingHandler(annotatedType, e);
            }
        }

    }

    private class RouteObserver {

        private final RouteObserverId id;

        private final WebRoute[] webRoutes;

        private final AnnotatedMethod<?> annotatedMethod;

        RouteObserver(RouteObserverId id, WebRoute[] webRoutes, AnnotatedMethod<?> annotatedMethod) {
            this.id = id;
            this.webRoutes = webRoutes;
            this.annotatedMethod = annotatedMethod;
        }

        void process(Router router) {
            Handler<RoutingContext> handler = new ObserverHandlerInstance(beanManager.getEvent().select(RoutingContext.class, id));
            for (WebRoute webRoute : webRoutes) {
                addRoute(router, handler, webRoute);
            }
        }

        boolean matches(Method method) {
            return annotatedMethod.getJavaMember().equals(method);
        }

    }

    private static class ObserverHandlerInstance implements Handler<RoutingContext> {

        private final Event<RoutingContext> event;

        public ObserverHandlerInstance(Event<RoutingContext> event) {
            this.event = event;
        }

        @Override
        public void handle(RoutingContext ctx) {
            event.fire(ctx);
        }

    }

    private boolean hasEventParameter(AnnotatedMethod<?> annotatedMethod) {
        for (AnnotatedParameter<?> param : annotatedMethod.getParameters()) {
            if (param.isAnnotationPresent(Observes.class)) {
                return true;
            }
        }
        return false;
    }

}
