package io.thorntail.vertx;

import io.vertx.core.eventbus.Message;

/**
 * Vertx {@link Message} wrapper. Note that if any observer any reply/fail method, other invocations of reply/fail methods will be ignored.
 *
 * @author Martin Kouba
 * @see VertxConsume
 */
public interface VertxMessage extends Message<Object> {

    /**
     * A failure code that is used if an observer method throws an exception.
     *
     * @see Message#fail(int, String)
     */
    int OBSERVER_FAILURE_CODE = 0x1B00;

    /**
     *
     * @return <code>true</code> if any of the observers replied already, <code>false</code> otherwise
     */
    boolean isReplied();

}
