package org.jboss.unimbus.events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class EventEmitter {
    @Any
    @Inject
    Event<Boolean> event;

    public void firePreInitialize() {
        event.select(new AnnotationLiteral<PreInitialize>() {}).fire(Boolean.TRUE);
    }

    public void fireInitialize() {
        event.select(new AnnotationLiteral<Initialize>() {}).fire(Boolean.TRUE);
    }

    public void firePostInitialize() {
        event.select(new AnnotationLiteral<PostInitialize>() {}).fire(Boolean.TRUE);
    }

    public void fireBeforeStart() {
        event.select(new AnnotationLiteral<BeforeStart>() {}).fire(Boolean.TRUE);
    }

    public void fireStart() {
        event.select(new AnnotationLiteral<Start>() {}).fire(Boolean.TRUE);
    }

    public void fireAfterStart() {
        event.select(new AnnotationLiteral<AfterStart>() {}).fire(Boolean.TRUE);
    }
}
