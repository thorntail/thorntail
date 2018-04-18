package io.thorntail.jms;

import javax.jms.JMSContext;

/**
 * Created by bob on 2/21/18.
 */
public interface WrappedJMSContext extends JMSContext {
    JMSContext getDelegate();

    default JMSContext getCoreJMSContext() {
        JMSContext cur = getDelegate();
        while (cur instanceof WrappedJMSContext) {
            cur = ((WrappedJMSContext) cur).getDelegate();
        }
        return cur;
    }
}
