package org.jboss.unimbus.jta.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.DeploymentException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import org.jboss.unimbus.events.LifecycleEvent;


/**
 *
 * This bean register {@link TransactionManager} in JNDI at startup in order to have {@link javax.transaction.Transactional}
 * interceptor working.
 *
 * @author Antoine Sabot-Durand
 *
 */
@ApplicationScoped
public class TransactionManagerInJNDIRegister {

    void register(@Observes LifecycleEvent.BeforeStart event, InitialContext ctx, TransactionManager transactionManager) {
        JtaMessages.MESSAGES.regiterTm();
        try {
            ctx.bind(jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerJNDIContext(), transactionManager);
        } catch (NamingException e) {
            throw new DeploymentException("An error occurred while registering Transaction Manager to JNDI", e);
        }
    }

}
