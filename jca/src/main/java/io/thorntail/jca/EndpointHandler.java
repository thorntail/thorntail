package io.thorntail.jca;

import java.lang.reflect.Method;

import javax.resource.ResourceException;

/**
 * Created by bob on 2/21/18.
 */
public interface EndpointHandler<T> {
    void beforeDelivery(T listener, Method method) throws ResourceException;
    void afterDelivery(T listener) throws ResourceException;
}
