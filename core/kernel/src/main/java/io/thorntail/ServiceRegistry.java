package io.thorntail;

/**
 * Registry for runtime registration of {@code META-INF/services/} entries.
 *
 * <p>May be {@code @Inject}ed into other components during early lifecycle events.</p>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public interface ServiceRegistry {
    /** Register an implementation.
     *
     * @param serviceInterface The service interface.
     * @param implementationClass The implementation class.
     */
    <T> void register(Class<T> serviceInterface, Class<? extends T> implementationClass);
}
