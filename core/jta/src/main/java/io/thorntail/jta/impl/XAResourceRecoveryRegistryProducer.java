package io.thorntail.jta.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import org.jboss.tm.XAResourceRecoveryRegistry;


/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class XAResourceRecoveryRegistryProducer {

    @Produces
    @ApplicationScoped
    XAResourceRecoveryRegistry xaResourceRecoveryRegistry() {
        return new RecoveryManagerService();

    }
}
