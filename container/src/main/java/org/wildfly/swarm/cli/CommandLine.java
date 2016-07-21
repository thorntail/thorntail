/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.inject.Vetoed;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.spi.api.ClassLoading;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;

/** A parsed command-line.
 *
 * @author Bob McWhirter
 */
@Vetoed
public class CommandLine {

    /** Default option for parsing -h and --help */
    public static final Option<Boolean> HELP = new Option<Boolean>()
            .withLong("help")
            .withShort('h')
            .withDescription("Display this help")
            .withDefault(()->false)
            .then((cmd, opt, value) -> cmd.put(opt, true));

    /** Default option for parsing -v and --version */
    public static final Option<Boolean> VERSION = new Option<Boolean>()
            .withLong("version")
            .withShort('v')
            .withDescription("Display the version of WildFly Swarm")
            .withDefault(()->false)
            .then((cmd, opt, value) -> cmd.put(opt, true));

    /** Default option for parsing -Dname and -Dname=value */
    public static final Option<Properties> PROPERTY = new Option<Properties>()
            .withShort('D')
            .hasValue("<name>[=<value>]")
            .valueMayBeSeparate(false)
            .withDescription("Set a system property")
            .withDefault(Properties::new)
            .then((cmd, opt, value) -> {
                String[] nameValue = value.split("=");
                Properties props = cmd.get(opt);
                String propName = nameValue[0];
                String propValue = "true";
                if (nameValue.length > 1) {
                    propValue = nameValue[1];
                }
                props.setProperty(propName, propValue);
            });

    /** Default option for parsing -P */
    public static final Option<URL> PROPERTIES_URL = new Option<URL>()
            .withShort('P')
            .withLong("properties")
            .hasValue("<url>")
            .withDescription("Load system properties from the given URL")
            .then( (cmd, opt, value)-> cmd.put( opt, Option.toURL( value ) ));

    /** Default option for parsing -c and --server-config */
    public static final Option<URL> SERVER_CONFIG = new Option<URL>()
            .withShort('c')
            .withLong("server-config")
            .hasValue("<config>")
            .valueMayBeSeparate(true)
            .withDescription("URL of the server configuration (e.g. standalone.xml)")
            .withDefault( ()->{
                return resolveResource("standalone.xml");
            })
            .then( (cmd, opt, value)-> cmd.put( opt, Option.toURL(value)));


    /** Default option for parsing -s and --stage-config */
    public static final Option<URL> STAGE_CONFIG = new Option<URL>()
            .withShort('s')
            .withLong("stage-config")
            .hasValue("<config>")
            .valueMayBeSeparate(true)
            .withDescription("URL to the stage configuration (e.g. config.yaml")
            .withDefault( ()->{
                return resolveResource("project-stages.yml");
            })
            .then( (cmd, opt, value)-> cmd.put( opt, Option.toURL( value ) ));



    public static final Option<String> ACTIVE_STAGE = new Option<String>()
            .withShort('S')
            .withLong("stage")
            .hasValue("<active-stage>")
            .valueMayBeSeparate(true)
            .withDescription( "When using a stage-config, set the active stage" )
            .then(CommandLine::put);

    /** Default option for parsing -b */
    public static final Option<String> BIND = new Option<String>()
            .withShort('b')
            .hasValue("<value>")
            .valueMayBeSeparate(true)
            .withDescription("Set the property " + SwarmProperties.BIND_ADDRESS + " to <value>")
            .then(CommandLine::put);

    /** Default set of options */
    public static Options defaultOptions() {
        return new Options(
                HELP,
                VERSION,
                PROPERTY,
                PROPERTIES_URL,
                SERVER_CONFIG,
                STAGE_CONFIG,
                ACTIVE_STAGE,
                BIND
        );
    }

    CommandLine(Options options) {
        this.options = options;
    }

    /** Put a value under a given key.
     *
     * @param key The key.
     * @param value The value.
     * @param <T> The type of the value.
     */
    public <T> void put(Option<T> key, T value) {
        this.values.put(key, value);
    }

    /** Retrieve a value under a given key.
     *
     * @param key The key.
     * @param <T> The type of the value.
     * @return The previously stored value, or the default provided by key if none has been previously stored.  The default will then be stored.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Option<T> key) {
        T v = (T) this.values.get(key);
        if (v == null) {
            v = key.defaultValue();
            this.values.put(key, v);
        }
        return v;
    }

    /** Display help for the options associated with the creation of this command-line.
     *
     * @param out The output stream to display help upon.
     */
    public void displayHelp(PrintStream out) {
        this.options.displayHelp(out);
    }

    public void displayHelp(PrintStream out, StageConfig stageConfig) {
        displayHelp( out );
        if ( stageConfig != null ) {
            out.println();
            out.println("Stage configuration options:" );
            out.println();
            for (String key : stageConfig.keys()) {
                out.println( "  " + key );
            }
            out.println();
        }
    }

    /** Display the version.
     *
     * @param out The output stream to display help upon.
     */
    public void displayVersion(PrintStream out) {
        out.println("WildFly Swarm version UNKNOWN");
    }

    /** Apply properties to the system properties.
     *
     * <p>Applies values stored through the <code>Key.PROPERTIES</code>,
     * <code>Key.PROPERTIES_URL</code> or <code>Key.BIND</code> options.
     *
     * @throws IOException If a URL is attempted to be read and fails.
     */
    public void applyProperties() throws IOException {
        URL propsUrl = get(PROPERTIES_URL);

        if (propsUrl != null) {
            Properties urlProps = new Properties();
            urlProps.load(propsUrl.openStream());

            for (String name : urlProps.stringPropertyNames()) {
                System.setProperty(name, urlProps.getProperty(name));
            }
        }

        Properties props = get(PROPERTY);

        for (String name : props.stringPropertyNames()) {
            System.setProperty(name, props.getProperty(name));
        }

        if (get(BIND) != null) {
            System.setProperty(SwarmProperties.BIND_ADDRESS, get(BIND));
        }

        if ( get(ACTIVE_STAGE) != null ) {
            System.setProperty(SwarmProperties.PROJECT_STAGE, get(ACTIVE_STAGE));
        }
    }

    /** Apply configuration to the container.
     *
     * <p>Applies configuration from <code>Key.SERVER_CONFIG</code> and <code>Key.STAGE_CONFIG</code>.</p>
     *
     * @param container The container to configure.
     * @throws MalformedURLException If a URL is attempted to be read and fails.
     */
    public void applyConfigurations(Container container) throws MalformedURLException {
        if (get(SERVER_CONFIG) != null) {
            container.withXmlConfig(get(SERVER_CONFIG));
        }
        if (get(STAGE_CONFIG) != null) {
            container.withStageConfig(get(STAGE_CONFIG));
        }
    }

    /** Apply properties and configuration from the parsed commandline to a container.
     *
     * @param container The container to apply configuration to.
     * @throws IOException If an error occurs resolving any URL.
     */
    public void apply(Container container) throws IOException {
        applyProperties();
        applyConfigurations(container);

        if ( get(HELP) ) {
            displayVersion( System.err );
            System.err.println();
            displayHelp( System.err, container.hasStageConfig() ? container.stageConfig() : null  );
            System.exit(0);
        }

        if ( get(VERSION) ) {
            displayVersion( System.err );
        }
    }

    void extraArgument(String arg) {
        this.extraArguments.add(arg);
    }

    /** Any un-parsed non-option arguments.
     *
     * @return The list of unparsed arguments.
     */
    public List<String> extraArguments() {
        return this.extraArguments;
    }

    /** Any un-parsed non-option arguments.
     *
     * @return The array of unparsed arguments.
     */
    public String[] extraArgumentsArray() {
        return this.extraArguments.toArray(new String[this.extraArguments.size()]);
    }

    void invalidArgument(String arg) {
        this.invalidArguments.add(arg);
    }

    /** Any invalid options seen during parsing.
     *
     * @return The list of invalid arguments.
     */
    public List<String> invalidArguments() {
        return this.invalidArguments;
    }

    /** Determine if any invalid arguments were seen during the parse.
     *
     * @return <code>true</code> if {@link #invalidArguments()} is not empty, otherwise <code>false</code>.
     */
    public boolean hasInvalidArguments() {
        return !this.invalidArguments.isEmpty();
    }

    /** Parse an array of arguments using the default options.
     *
     * @param args The args to parse.
     * @return The parsed <code>CommandLine</code>.
     */
    public static CommandLine parse(String... args) throws Exception {
        return CommandLineParser.parse(defaultOptions(), args);
    }

    /** Parse an array of arguments using specific options.
     *
     * @param options The options to use.
     * @param args The args to parse.
     * @return The parsed <code>CommandLine</code>.
     */
    public static CommandLine parse(Options options, String... args) throws Exception {
        return CommandLineParser.parse(options, args);
    }

    private static URL resolveResource(String path) {
        Path candidate = Paths.get(path);
        if (Files.exists(candidate)) {
            try {
                return candidate.toUri().toURL();
            } catch (MalformedURLException e) {
                // ignore
            }
        }

        URL yml = null;
        try {
            Module appModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application" ) );
            yml = appModule.getClassLoader().getResource( path );
            if ( yml != null ) {
                return yml;
            }
        } catch (ModuleLoadException e) {
            // ignore;
        }

        yml = ClassLoader.getSystemClassLoader().getResource( path );
        return yml;
    }

    private final Options options;

    private final Map<Option<?>, Object> values = new HashMap<>();

    private final List<String> extraArguments = new ArrayList<>();

    private final List<String> invalidArguments = new ArrayList<>();
}
