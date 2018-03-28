package org.jboss.unimbus.config.ext;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.util.TypeLiteral;
import javax.inject.Provider;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.jboss.unimbus.config.impl.ConfigImpl;
import org.jboss.unimbus.config.impl.sources.MapConfigSource;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by bob on 2/2/18.
 */
public class InjectionCoercerTest {

    private static final Type STRING_LIST = new TypeLiteral<List<String>>() {
    }.getType();

    private static final Type STRING_SET = new TypeLiteral<Set<String>>() {
    }.getType();

    private static final Type OPTIONAL_STRING = new TypeLiteral<Optional<String>>() {
    }.getType();

    private static final Type STRING_PROVIDER = new TypeLiteral<Provider<String>>() {
    }.getType();

    private static final Type INTEGER_LIST = new TypeLiteral<List<Integer>>() {
    }.getType();

    private static final Type INTEGER_SET = new TypeLiteral<Set<Integer>>() {
    }.getType();

    private static final Type OPTIONAL_INTEGER = new TypeLiteral<Optional<Integer>>() {
    }.getType();

    private static final Type INTEGER_PROVIDER = new TypeLiteral<Provider<Integer>>() {
    }.getType();

    private static final Type OPTIONAL_STRING_SET = new TypeLiteral<Optional<Set<String>>>() {

    }.getType();

    @Before
    public void setupConfig() {
        this.source = new MapConfigSource("test");
        this.config = (ConfigImpl) ConfigProviderResolver.instance().getBuilder().withSources(this.source).build();
    }

    @Test
    public void testString() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(String.class);
        assertThat(coercer.getTargetType()).isEqualTo(String.class);
        assertThat(coercer.getRequestType()).isEqualTo(String.class);
        assertThat(coercer.isOptional()).isFalse();

        assertThat(coercer.coerce("foo")).isEqualTo("foo");
        assertThat(coercer.coerce("42")).isEqualTo("42");
    }

    @Test
    public void testOptionalString() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(OPTIONAL_STRING);
        assertThat(coercer.getTargetType()).isEqualTo(OPTIONAL_STRING);
        assertThat(coercer.getRequestType()).isEqualTo(String.class);
        assertThat(coercer.isOptional()).isTrue();

        Object result = null;

        result = coercer.coerce("foo");
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Optional.class);
        assertThat(((Optional<String>) result).get()).isEqualTo("foo");

        result = coercer.coerce(null);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Optional.class);
        assertThat(((Optional<String>) result).isPresent()).isFalse();
    }

    @Test
    public void testStringProvider() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(STRING_PROVIDER);
        assertThat(coercer.getTargetType()).isEqualTo(STRING_PROVIDER);
        assertThat(coercer.getRequestType()).isEqualTo(String.class);
        assertThat(coercer.isOptional()).isTrue();
        assertThat(coercer.isDynamic()).isTrue();

        Object result = null;

        result = coercer.coerce(this.config, "dynamic.string", "foo");
        assertThat(result).isInstanceOf(Provider.class);
        assertThat(((Provider<String>) result).get()).isEqualTo("foo");

        this.source.setValue("dynamic.string", "bar");
        assertThat(((Provider<String>) result).get()).isEqualTo("bar");
    }


    @Test
    public void testStringArray() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(String[].class);
        assertThat(coercer.getTargetType()).isEqualTo(String[].class);
        assertThat(coercer.getRequestType()).isEqualTo(String[].class);
        assertThat(coercer.isOptional()).isFalse();

        Object result = null;

        result = coercer.coerce(new String[]{"foo", "bar"});
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String[].class);
        assertThat((String[]) result).hasSize(2);
        assertThat(((String[]) result)[0]).isEqualTo("foo");
        assertThat(((String[]) result)[1]).isEqualTo("bar");
    }

    @Test
    public void testStringList() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(STRING_LIST);
        assertThat(coercer.getTargetType()).isEqualTo(STRING_LIST);
        assertThat(coercer.getRequestType()).isEqualTo(String[].class);
        assertThat(coercer.isOptional()).isFalse();

        Object result = null;

        result = coercer.coerce(new String[]{"foo", "bar"});
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(List.class);
        assertThat((List<String>) result).hasSize(2);
        assertThat(((List<String>) result).get(0)).isEqualTo("foo");
        assertThat(((List<String>) result).get(1)).isEqualTo("bar");
    }

    @Test
    public void testStringSet() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(STRING_SET);
        assertThat(coercer.getTargetType()).isEqualTo(STRING_SET);
        assertThat(coercer.getRequestType()).isEqualTo(String[].class);
        assertThat(coercer.isOptional()).isFalse();

        Object result = null;

        result = coercer.coerce(new String[]{"foo", "bar"});
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Set.class);
        assertThat((Set<String>) result).hasSize(2);
        assertThat(((Set<String>) result)).contains("foo");
        assertThat(((Set<String>) result)).contains("bar");
    }

    @Test
    public void testInteger() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(Integer.class);
        assertThat(coercer.getTargetType()).isEqualTo(Integer.class);
        assertThat(coercer.getRequestType()).isEqualTo(Integer.class);
        assertThat(coercer.isOptional()).isFalse();
    }

    @Test
    public void testIntegerList() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(INTEGER_LIST);
        assertThat(coercer.getTargetType()).isEqualTo(INTEGER_LIST);
        assertThat(coercer.getRequestType()).isEqualTo(Integer[].class);
        assertThat(coercer.isOptional()).isFalse();

        Object result = null;

        result = coercer.coerce(new Integer[]{42, 101});
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(List.class);
        assertThat((List<Integer>) result).hasSize(2);
        assertThat(((List<Integer>) result).get(0)).isEqualTo(42);
        assertThat(((List<Integer>) result).get(1)).isEqualTo(101);
    }

    @Test
    public void testOptionalInteger() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(OPTIONAL_INTEGER);
        assertThat(coercer.getTargetType()).isEqualTo(OPTIONAL_INTEGER);
        assertThat(coercer.getRequestType()).isEqualTo(Integer.class);
        assertThat(coercer.isOptional()).isTrue();

        Object result = null;

        result = coercer.coerce(42);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Optional.class);
        assertThat(((Optional<Integer>) result).get()).isEqualTo(42);

        result = coercer.coerce(null);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Optional.class);
        assertThat(((Optional<Integer>) result).isPresent()).isFalse();
    }

    @Test
    public void testIntegerArray() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(Integer[].class);
        assertThat(coercer.getTargetType()).isEqualTo(Integer[].class);
        assertThat(coercer.getRequestType()).isEqualTo(Integer[].class);
        assertThat(coercer.isOptional()).isFalse();
    }


    @Test
    public void testIntegerSet() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(INTEGER_SET);
        assertThat(coercer.getTargetType()).isEqualTo(INTEGER_SET);
        assertThat(coercer.getRequestType()).isEqualTo(Integer[].class);
        assertThat(coercer.isOptional()).isFalse();

        Object result = null;

        result = coercer.coerce(new Integer[]{42, 101});
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Set.class);
        assertThat((Set<Integer>) result).hasSize(2);
        assertThat(((Set<Integer>) result)).contains(42);
        assertThat(((Set<Integer>) result)).contains(101);
    }

    @Test
    public void testOptionalStringList() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(OPTIONAL_STRING_SET);
        assertThat(coercer.getTargetType()).isEqualTo(OPTIONAL_STRING_SET);
        assertThat(coercer.getRequestType()).isEqualTo(String[].class);
        assertThat(coercer.isOptional()).isTrue();

        Object result = null;

        result = coercer.coerce(new String[]{"foo", "bar"});

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Optional.class);

        Set<String> set = ((Optional<Set<String>>) result).get();

        assertThat(set).isNotNull();
        assertThat(set).contains("foo");
        assertThat(set).contains("bar");
    }

    @Test
    public void testNullOptionalStringList() throws Exception {
        InjectionCoercer coercer = new InjectionCoercer(OPTIONAL_STRING_SET);
        assertThat(coercer.getTargetType()).isEqualTo(OPTIONAL_STRING_SET);
        assertThat(coercer.getRequestType()).isEqualTo(String[].class);
        assertThat(coercer.isOptional()).isTrue();

        Object result = null;

        result = coercer.coerce(null);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Optional.class);

        assertThat( ((Optional<Set<String>>) result).isPresent() ).isFalse();

    }


    private ConfigImpl config;

    private MapConfigSource source;
}
