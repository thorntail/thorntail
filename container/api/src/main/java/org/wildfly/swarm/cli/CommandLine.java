package org.wildfly.swarm.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class CommandLine {


    public static class Key<T> {

        private final Supplier<T> defaultSupplier;

        public Key(Supplier<T> defaultSupplier) {
            this.defaultSupplier = defaultSupplier;
        }

        public T defaultValue() {
            return this.defaultSupplier.get();
        }

    }

    public static final Key<Boolean> HELP = new Key<>(()->false);

    public static final Key<Boolean> VERSION = new Key<>(()->false);

    public static final Key<Properties> PROPERTIES = new Key<>(()->new Properties());

    public static final Key<String> PROPERTIES_URL = new Key<>(()->null);

    public static final Key<String> CONFIG = new Key<>(()->null);

    public static final Key<String> STAGE_CONFIG = new Key<>(()->null);

    public static final Key<String> BIND = new Key<>(()->null);

    private final Options options;

    private Map<Key<?>, Object> values = new HashMap<>();

    private final List<String> extraArguments = new ArrayList<>();

    private final List<String> invalidArguments = new ArrayList<>();

    CommandLine(Options options) {
        this.options = options;
    }

    public <T> void put(Key<T> key, T value) {
        this.values.put(key, value);
    }

    public <T> T get(Key<T> key) {
        T v = (T) this.values.get(key);
        if (v == null) {
            v = key.defaultValue();
            this.values.put(key, v);
        }
        return v;
    }

    public void displayHelp(PrintStream out) {
        this.options.displayHelp(out);
    }

    public void displayVersion(PrintStream out) {
        out.println("WildFly Swarm version UNKNOWN");
    }

    public void applyProperties() throws IOException {
        String propsUrl = get(PROPERTIES_URL);

        if (propsUrl != null) {
            Properties urlProps = new Properties();
            urlProps.load(new URL(propsUrl).openStream());

            for (String name : urlProps.stringPropertyNames()) {
                System.setProperty(name, urlProps.getProperty(name));
            }
        }

        Properties props = get(PROPERTIES);

        for (String name : props.stringPropertyNames()) {
            System.setProperty(name, props.getProperty(name));
        }

        if ( get(BIND) != null ) {
            System.setProperty( "swarm.bind.address", get(BIND) );
        }
    }

    public void applyConfigurations(Container container) throws MalformedURLException {
        if (get(CONFIG) != null) {
            container.withXmlConfig(new URL(get(CONFIG)));
        }
        if ( get(STAGE_CONFIG) != null ) {
            container.withStageConfig(new URL(get(STAGE_CONFIG)));
        }
    }

    public void extraArgument(String arg) {
        this.extraArguments.add(arg);
    }

    public List<String> extraArguments() {
        return this.extraArguments;
    }

    public String[] extraArgumentsArray() {
        return this.extraArguments.toArray( new String[ this.extraArguments.size() ] );
    }

    public void invalidArgument(String arg) {
        this.invalidArguments.add(arg);
    }

    public List<String> invalidArguments() {
        return this.invalidArguments;
    }

    public boolean hasInvalidArguments() {
        return !this.invalidArguments.isEmpty();
    }

    public static CommandLine parse(String... args) {
        return CommandLineParser.parse(args);
    }
}
