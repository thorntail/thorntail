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
