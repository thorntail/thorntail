package io.thorntail.condition.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

/**
 * Annotation which may be placed upon a CDI component to conditionally veto it if a class is present.
 *
 * <p>If the class mentioned in the annotation can be found, this component will be fully veto'd by the CDI container.</p>
 *
 * <p>This annotation may be used repeatedly and with {@link RequiredClassPresent} to create complex conditoions</p>
 *
 * <p>All condition annotations will be applied with implicit <b>AND</b> semantics.</p>
 *
 * <pre>
 * &#64;ApplicationScoped
 * &#64;RequiredClassNotPresent("org.something.WhichIsToBeAvoided")
 * public class MyComponent {
 *
 * }
 * </pre>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 *
 * @see RequiredClassPresent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Repeatable(MultipleRequiredClassNotPresent.class)
public @interface RequiredClassNotPresent {
    @Nonbinding
    String value() default "";
}
