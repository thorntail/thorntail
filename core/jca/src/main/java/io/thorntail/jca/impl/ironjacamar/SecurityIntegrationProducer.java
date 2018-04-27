package io.thorntail.jca.impl.ironjacamar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.jca.core.spi.security.Callback;
import org.jboss.jca.core.spi.security.SecurityContext;
import org.jboss.jca.core.spi.security.SecurityIntegration;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class SecurityIntegrationProducer {

    @Produces
    @ApplicationScoped
    SecurityIntegration securityIntegration() {
        return new SecurityIntegration() {
            public SecurityContext context;

            @Override
            public SecurityContext createSecurityContext(String sd) throws Exception {
                return new SecurityContext() {
                    @Override
                    public Subject getAuthenticatedSubject() {
                        return null;
                    }

                    @Override
                    public void setAuthenticatedSubject(Subject subject) {

                    }

                    @Override
                    public String[] getRoles() {
                        return new String[0];
                    }
                };
            }

            @Override
            public SecurityContext getSecurityContext() {
                return this.context;
            }

            @Override
            public void setSecurityContext(SecurityContext context) {
                this.context = context;

            }

            @Override
            public CallbackHandler createCallbackHandler() {
                return null;
            }

            @Override
            public CallbackHandler createCallbackHandler(Callback callback) {
                return null;
            }
        };
    }
}
