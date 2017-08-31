/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.spi.api;

import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Defaultable<T> implements Supplier<T> {

    private final Class<T> type;

    private final DefaultValue<T> defaultValue;

    private Optional<T> explicit = Optional.empty();

    public static Defaultable<String> string(String defaultValue) {
        return string(() -> defaultValue);
    }

    public static Defaultable<String> string(Supplier<String> defaultValueSupplier) {
        return new Defaultable<>(String.class, defaultValueSupplier);
    }

    public static Defaultable<Integer> integer(int defaultValue) {
        return integer(() -> defaultValue);
    }

    public static Defaultable<Float> floating(float defaultValue) {
        return floating(() -> defaultValue);
    }

    public static Defaultable<Integer> integer(Supplier<Integer> defaultValueSupplier) {
        return new Defaultable<>(Integer.class, defaultValueSupplier);
    }

    public static Defaultable<Float> floating(Supplier<Float> defaultValueSupplier) {
        return new Defaultable<>(Float.class, defaultValueSupplier);
    }

    public static Defaultable<Long> longInteger(long defaultValue) {
        return longInteger(() -> defaultValue);
    }

    public static Defaultable<Long> longInteger(Supplier<Long> defaultValueSupplier) {
        return new Defaultable<>(Long.class, defaultValueSupplier);
    }

    public static Defaultable<Boolean> bool(boolean defaultValue) {
        return bool(() -> defaultValue);
    }

    public static Defaultable<Boolean> bool(Supplier<Boolean> defaultValueSupplier) {
        return new Defaultable<>(Boolean.class, defaultValueSupplier);
    }

    public static Defaultable<URL> url(URL defaultValue) {
        return url(() -> defaultValue);
    }

    public static Defaultable<URL> url(Supplier<URL> defaultValueSupplier) {
        return new Defaultable<>(URL.class, defaultValueSupplier);
    }

    /**
     * Create a <code>Boolean</code> configuration that has a default value of <code>true</code> if all arguments have been explicitly set to non-default values.
     *
     * @param items The items to test.
     * @return The new item.
     */
    public static Defaultable<Boolean> ifAllExplicitlySet(Defaultable<?>... items) {
        return bool(() -> {
            for (Defaultable<?> item : items) {
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
    public static Defaultable<Boolean> ifAnyExplicitlySet(Defaultable<?>... items) {
        return bool(() -> {
            for (Defaultable<?> item : items) {
                if (item.isExplicit()) {
                    return true;
                }
            }
            return false;
        });
    }

    private Defaultable(Class<T> type, Supplier<T> defaultValueSupplier) {
        this.type = type;
        this.defaultValue = new DefaultValue<T>(defaultValueSupplier);
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

