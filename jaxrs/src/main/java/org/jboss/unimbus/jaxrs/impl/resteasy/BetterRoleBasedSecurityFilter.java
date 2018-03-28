package org.jboss.unimbus.jaxrs.impl.resteasy;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.UnauthorizedException;

/**
 * Created by bob on 3/27/18.
 */
@Priority(Priorities.AUTHORIZATION)
public class BetterRoleBasedSecurityFilter implements ContainerRequestFilter {
    protected String[] rolesAllowed;

    protected boolean denyAll;

    protected boolean permitAll;

    public BetterRoleBasedSecurityFilter(String[] rolesAllowed, boolean denyAll, boolean permitAll) {
        this.rolesAllowed = rolesAllowed;
        this.denyAll = denyAll;
        this.permitAll = permitAll;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        SecurityContext context = ResteasyProviderFactory.getContextData(SecurityContext.class);
        if (denyAll) {
            if ( context == null || context.getUserPrincipal() == null ) {
                throw new NotAuthorizedException(Response.status(401).entity("Not authorized").type("text/html;charset=UTF-8").build());
            }
            throw new ForbiddenException(Response.status(403).entity("Access forbidden: role not allowed").type("text/html;charset=UTF-8").build());
        }
        if (permitAll) {
            return;
        }

        if (rolesAllowed != null) {
            if ( context != null && context.getUserPrincipal() != null ) {
                for (String role : rolesAllowed) {
                    if (context.isUserInRole(role)) return;
                }
                throw new ForbiddenException(Response.status(403).entity("Access forbidden: role not allowed").type("text/html;charset=UTF-8").build());
            }
            throw new NotAuthorizedException(Response.status(401).entity("Not authorized").type("text/html;charset=UTF-8").build());
        }
        return;
    }
}
