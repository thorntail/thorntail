package org.wildfly.swarm.spi.api;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A configuration value holder.
 *
 * <p>Holds both an optional default value, plus an optional explicitly-set value.
 * Operates not unlike a JDK <code>Optional</code> in terms of semantics, while
 * providing introspection into the source (default or explicit) of whatever value
 * is provided.</p>
 *
 * <p>By calling {@link #explicit()}, you may retrieve a possibly-empty bonafide
 * <code>Optional</code> if there is a desire to ignore any default value.</p>
 *
 * <p>Additionally, {@link #explicitOrElseGet(Supplier)} provides <code>Optional</code>-like</p>
 * shortcut for working with an explicit value, ignoring the default, falling back
 * to the value provided at the call location.
 *
 * <p>Defaults can be specified using either constant values or by providing a
 * <code>Supplier</code> which is invoked at the point of use.  This allows for
 * dynamically constructing default values that are reliant upon other configuration
 * items or arbitrary calculations.</p>
 *
 * <p>This entire class is simply wrappers around the functionality already
 * provided by <code>ConfigurationValue</code>, but helps embody the knowledge
 * of default values, and provides more type-safe ways of working with
 * configuration items from within <b>fraction</b> code.</p>
 *
 * <p><b>This is not considered a user-facing class.</b></p>
 *
 * @author Bob McWhirter
 * @see org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue
 */
public class Configurable<T> implements Supplier<T> {

    private String name;

    private Class<T> type;

    private Optional<T> explicit = Optional.empty();

    private final DefaultValue<T> defaultValue;

    public static Configurable<String> string(String name) {
        return string(name, () -> null);
    }

    public static Configurable<String> string(String name, String defaultValue) {
        return string(name, () -> defaultValue);
    }

    public static Configurable<String> string(String name, Supplier<String> defaultValueSupplier) {
        return new Configurable<>(name, String.class, defaultValueSupplier);
    }

    public static Configurable<Integer> integer(String name) {
        return integer(name);
    }

    public static Configurable<Integer> integer(String name, int defaultValue) {
        return integer(name, () -> defaultValue);
    }

    public static Configurable<Integer> integer(String name, Supplier<Integer> defaultValueSupplier) {
        return new Configurable<>(name, Integer.class, defaultValueSupplier);
    }

    public static Configurable<Boolean> bool(String name) {
        return bool(name, () -> null);
    }

    public static Configurable<Boolean> bool(String name, boolean defaultValue) {
        return bool(name, () -> defaultValue);
    }

    public static Configurable<Boolean> bool(String name, Supplier<Boolean> defaultValueSupplier) {
        return new Configurable<>(name, Boolean.class, defaultValueSupplier);
    }

    /**
     * Create a <code>Boolean</code> configuration that has a default value of <code>true</code> if all arguments have been explicitly set to non-default values.
     *
     * @param items The items to test.
     * @return The new item.
     */
    public static Configurable<Boolean> ifAllExplicitlySet(String name, Configurable<?>... items) {
        return bool(name, () -> {
            for (Configurable<?> item : items) {
                if (!item.isExplicit()) {
                    return false;
                }
            }
            return true;
        });
    }

    /**
     * Create a <code>Boolean</code> configuration that has a default value of <code>true</code> if any arguments have been explicitly set to non-default values.
     *
     * @param items The items to test.
     * @return The new item.
     */
    public static Configurable<Boolean> ifAnyExplicitlySet(String name, Configurable<?>... items) {
        return bool(name, () -> {
            for (Configurable<?> item : items) {
                if (item.isExplicit()) {
                    return true;
                }
            }
            return false;
        });
    }

    private Configurable(String name, Class<T> type, Supplier<T> defaultValueSupplier) {
        this.name = name;
        this.type = type;
        this.defaultValue = new DefaultValue<T>(defaultValueSupplier);
    }

    public String name() {
        return this.name;
    }

    public Class<T> type() {
        return this.type;
    }

    /**
     * Explicitly set a value.
     *
     * @param explicitValue The value to set.
     */
    public synchronized void set(T explicitValue) {
        this.explicit = Optional.ofNullable(explicitValue);
    }

    /**
     * Retrieve the default value, if any.
     *
     * @return The default value.
     * @throws NoSuchElementException if no default value is available.
     */
    public T defaultValue() throws NoSuchElementException {
        return defaultValue(true);
    }

    private synchronized T defaultValue(boolean throwIfNull) {
        return this.defaultValue.get(throwIfNull);
    }

    /**
     * Retrieve the explicitly set value, if any.
     *
     * @return The explicitly set value.
     * @throws NoSuchElementException if no explicit value is available.
     */
    public T explicitValue() throws NoSuchElementException {
        return explicitValue(true);
    }

    private synchronized T explicitValue(boolean throwIfNull) {
        return this.explicit.orElseGet(() -> {
            if (throwIfNull) {
                throw new NoSuchElementException("No explicit value present");
            }
            return null;
        });
    }

    /**
     * Retrieve the value, regardless of it's origin.
     *
     * @return The value, if non-null.
     * @throws NoSuchElementException If no value (default nor explicit) is present.
     */
    public T get() throws NoSuchElementException {
        return get(true);
    }

    private synchronized T get(boolean throwIfNull) {
        return explicitOrElseGet(() -> this.defaultValue.get(throwIfNull));
    }

    /**
     * Retrieve a standard <code>Optional</code> containing the explicit value, if any.
     *
     * <p>If no explicit value is set, an <b>empty</b> <code>Optional</code> will be returned.</p>
     *
     * @return The <code>Optional</code> wrapper around the explicitly set value, if any.
     */
    public Optional<T> explicit() {
        return this.explicit;
    }

    /**
     * If a default or explicit value is present, invoke the supplied consumer with it.
     *
     * @param consumer The consumer.
     */
    public void ifPresent(Consumer<? super T> consumer) {
        T value = get(false);

        if (value != null) {
            consumer.accept(value);
        }
    }

    /**
     * If (and only if) an explicit value is present, invoke the supplied consumer with it
     *
     * @param consumer The consumer.
     */
    public void ifExplicit(Consumer<? super T> consumer) {
        if (isExplicit()) {
            consumer.accept(get());
        }
    }

    /**
     * If (and only if) only a default value is present (without an explicit value), invoke the supplied consumer with it.
     *
     * @param consumer The consumer.
     */
    public void ifDefault(Consumer<? super T> consumer) {
        if (isDefault()) {
            consumer.accept(get());
        }
    }

    /**
     * Retrieve the value (default or explicit) if present, otherwise the provided value parameter.
     *
     * @param defaultValue The value to return only if no value is present.
     * @return Either the value (default or explicit) or the <code>defaultValue</code> parameter.
     */
    public T orElse(T defaultValue) {
        T value = get();

        if (value != null) {
            return value;
        }

        return defaultValue;
    }

    /**
     * Retrieve the value (default or explicit) if present, otherwise return the value supplied by the <code>Supplier</code> parameter.
     *
     * @param other The value supplier to use to supply a value only if no value is present.
     * @return Either the value (default or explicit) or the value supplied by the <code>other</code> <code>Supplier</code> parameter.
     */
    public T orElseGet(Supplier<? extends T> other) {
        T value = get();

        return value != null ? value : other.get();
    }

    /**
     * Retrieve the explicit value if present, otherwise return the value supplied by the <code>Supplier</code> parameter.
     *
     * @param other The value supplier to use to supply a value only if no explicit value is present.
     * @return Either the explicitly set value (if set) or the value supplied by the <code>other</code> <code>Supplier</code> parameter.
     */
    public T explicitOrElseGet(Supplier<? extends T> other) {
        return this.explicit.orElseGet(other);
    }

    /**
     * Retrieve the explicit value if present, followed in precedence by whatever value, if not-null
     * supplied by the <code>preferred</code> parameter, followed by the default if all previous
     * options were null.
     *
     * @param preferred The intermediate value supplier.
     * @return The explicitly set, preferred or default value in that order, or possibly null.
     */
    public T get(Supplier<? extends T> preferred) {
        return explicitOrElseGet(() -> {
            T value = preferred.get();
            if (value != null) {
                return value;
            }
            return defaultValue.get(false);
        });
    }

    /**
     * Determine if any value (default or explicit) is present.
     *
     * @return <code>true</code> is a value is present, otherwise <code>false</code>.
     */
    public boolean isPresent() {
        return get(false) != null;
    }

    /**
     * Determine if either an explicit value is set of the supplied <code>other</code> <code>Supplier</code> parameter returns non-null.
     *
     * @param other The alternative value supplier.
     * @return <code>true</code> if either an explicit value is set or the supplier provides a non-null value.
     */
    public boolean isExplicitPresent(Supplier<T> other) {
        return isExplicit() || other.get() != null;
    }

    /**
     * Determine if the value that would be provided (even if null) is the default value or not.
     *
     * @return <code>true</code> only if no explicit value is set.
     */
    public boolean isDefault() {
        return !this.explicit.isPresent();
    }

    /**
     * Determine if the value that would be provided is explicitly set or not.
     *
     * @return <code>true</code> only if an explicit value is set.
     */
    public boolean isExplicit() {
        return this.explicit.isPresent();
    }

    private static class DefaultValue<T> {

        private final Supplier<T> supplier;

        DefaultValue(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        synchronized T get(boolean throwIfNull) {
            T value = supplier.get();

            if (throwIfNull && value == null) {
                throw new NoSuchElementException("No default value present");
            }

            return value;
        }
    }


}

