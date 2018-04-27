package io.thorntail.test.arquillian.impl;

import io.thorntail.test.arquillian.impl.maps.CDISessionMap;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import io.thorntail.test.arquillian.impl.maps.CDIRequestMap;
import org.jboss.weld.context.bound.BoundRequest;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.manager.api.WeldManager;

/**
 * @author Ken Finnigan
 */
public class ContextLifecycleHandler {

    @Inject
    @ClassScoped
    private InstanceProducer<CDISessionMap> sessionMap;

    @Inject
    @ClassScoped
    private InstanceProducer<CDIRequestMap> requestMap;

    @Inject
    @ClassScoped
    private InstanceProducer<BoundRequest> boundRequest;

    public void createSession(@Observes AfterDeploy event, WeldManager manager) {
        BoundSessionContext sessionContext = manager.instance().select(BoundSessionContext.class).get();

        if (null != sessionContext) {
            CDISessionMap map = new CDISessionMap();
            sessionContext.associate(map);
            sessionContext.activate();
            sessionMap.set(map);
        }
    }

    public void destroySession(@Observes BeforeUnDeploy event, WeldManager manager) {
        BoundSessionContext sessionContext = manager.instance().select(BoundSessionContext.class).get();
        CDISessionMap map = sessionMap.get();

        if (null != map && null != sessionContext) {
            try {
                sessionContext.invalidate();
                sessionContext.deactivate();
            } finally {
                sessionContext.dissociate(map);
            }
        }
    }

    public void createRequest(@Observes Before event, WeldManager manager) {
        BoundRequestContext requestContext = manager.instance().select(BoundRequestContext.class).get();

        if (null != requestContext) {
            CDIRequestMap map = new CDIRequestMap();
            requestContext.associate(map);
            requestContext.activate();
            requestMap.set(map);
        }
    }

    public void destroyRequest(@Observes After event, WeldManager manager) {
        BoundRequestContext requestContext = manager.instance().select(BoundRequestContext.class).get();
        CDIRequestMap map = requestMap.get();

        if (null != map && null != requestContext) {
            try {
                requestContext.invalidate();
                requestContext.deactivate();
            } finally {
                requestContext.dissociate(map);
                map.clear();
            }
        }
    }
}
