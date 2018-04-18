package io.thorntail.servlet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.InetSocketAddress;
import java.net.URL;

import javax.inject.Qualifier;

import io.thorntail.servlet.DeploymentMetaData;

/**
 * Qualifier for management web endpoint.
 *
 * <p>The qualifier may be used when injecting:</p>
 *
 * <ul>
 *     <li>{@link URL}</li>
 *     <li>{@link InetSocketAddress}</li>
 * </ul>
 *
 * <p>Additionally, it may be applied to produced {@link DeploymentMetaData} to
 * denote that the deployment should be bound to the management endpoint.</p>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface Management {
}
