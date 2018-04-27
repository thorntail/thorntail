package io.thorntail.jta.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.arjuna.ats.internal.jbossatx.jta.jca.XATerminator;
import org.jboss.tm.JBossXATerminator;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class JBossXATerminatorProducer {

    @Produces
    @ApplicationScoped
    JBossXATerminator xaTerminator() {
        return new XATerminator();
    }
}
