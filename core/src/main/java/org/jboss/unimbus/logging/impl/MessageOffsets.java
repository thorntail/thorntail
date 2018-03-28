package org.jboss.unimbus.logging.impl;

/**
 * @author Ken Finnigan
 */
public interface MessageOffsets {
    int CORE_OFFSET = 0;

    int SERVLET_OFFSET = 1000;
    int JAXRS_OFFSET = 2000;
    int DATASOURCES_OFFSET = 3000;
    int HEALTH_OFFSET = 4000;
    int JDBC_OFFSET = 5000;
    int JNDI_OFFSET = 6000;
    int JPA_OFFSET = 7000;
    int JSONP_OFFSET = 8000;
    int JTA_OFFSET = 9000;
    int METRICS_OFFSET = 10000;
    int SECURITY_OFFSET = 11000;
    int KEYCLOAK_OFFSET = 12000;
    int JCA_OFFSET = 13000;
    int OPENAPI_OFFSET = 14000;
    int VERTX_OFFSET = 15000;
}
