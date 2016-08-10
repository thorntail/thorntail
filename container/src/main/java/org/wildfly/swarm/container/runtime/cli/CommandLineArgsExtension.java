package org.wildfly.swarm.container.runtime.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.util.TypeLiteral;

/**
 * @author Bob McWhirter
 */
public class CommandLineArgsExtension implements Extension {

    private final String[] args;
    private final List<String> argsList;

    public CommandLineArgsExtension(String... args) {
        this.args = args;
        this.argsList = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(this.args)));
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        abd.addBean()
                .addType(String[].class)
                .addQualifier(CommandLineArgs.Literal.INSTANCE)
                .producing(this.args);

        abd.addBean()
                .addType(new TypeLiteral<List<String>>() { })
                .producing( this.argsList );
    }
}
