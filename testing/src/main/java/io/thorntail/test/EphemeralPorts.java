package io.thorntail.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to cause ephemeral ports to be used for web endpoints.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EphemeralPorts {

    /**
     * Flag that determines if the primary web endpoint should use an ephemeral port.
     */
    boolean primary() default true;

    /**
     * Flag that determines if the management web endpoint should use an ephemeral port.
     */
    boolean management() default true;
}
