/**
 * Support for JAX-RS applications.
 *
 * <p>This package contains no directly usable components.</p>
 *
 * <p>By default, the classpath will be scanned for {@link javax.ws.rs.core.Application} instances and automatically
 * deployed. Each application resource participates within the CDI container and can {@code @Inject} any other
 * relevant component.</p>
 */
package io.thorntail.jaxrs;