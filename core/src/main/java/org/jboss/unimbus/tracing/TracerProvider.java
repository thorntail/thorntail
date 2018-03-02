package org.jboss.unimbus.tracing;

import javax.annotation.Priority;
import javax.inject.Provider;

import io.opentracing.Tracer;

/**
 * Interface for components which may provide configured {@code Tracer} implementations.
 *
 * <p>These may be annotated with {@link Priority} to indicate relative priority. The
 * priority follows the same rules as {@code TracerResolver}. This class simply aims to
 * allow for avoiding {@code META-INF/services/} and integration with CDI.</p>
 */
public interface TracerProvider extends Provider<Tracer> {
}
