package org.wildfly.swarm.logstash;

import java.util.Properties;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.config.logging.CustomHandler;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Post;
import org.wildfly.swarm.spi.api.Pre;
import org.wildfly.swarm.spi.api.annotations.ConfigurationValue;

/**
 * @author Bob McWhirter
 */
@Post
@Singleton
public class LogstashCustomizer implements Customizer {

    @Inject
    @Any
    private LogstashFraction logstashFraction;

    @Inject
    @Any
    private LoggingFraction loggingFraction;

    @Inject
    @ConfigurationValue(LogstashProperties.HOSTNAME)
    private String hostname;

    @Inject
    @ConfigurationValue(LogstashProperties.PORT)
    private Integer port;

    @Override
    public void customize() {
        try {
            String hostname = (this.hostname != null ? this.hostname : this.logstashFraction.hostname());
            int port = (this.port != null ? this.port : this.logstashFraction.port());

            if (hostname != null) {
                Properties handlerProps = new Properties();

                handlerProps.put("hostname", hostname);
                handlerProps.put("port", "" + port);

                System.err.println( "using: " + handlerProps );

                final CustomHandler<?> logstashHandler = new CustomHandler<>("logstash-handler")
                        .module("org.jboss.logmanager.ext")
                        .attributeClass("org.jboss.logmanager.ext.handlers.SocketHandler")
                        .namedFormatter("logstash")
                        .properties(handlerProps);

                System.err.println( "console handlers: " + this.loggingFraction.consoleHandlers() );

                this.loggingFraction
                        .customFormatter("logstash", "org.jboss.logmanager.ext", "org.jboss.logmanager.ext.formatters.LogstashFormatter",
                                this.logstashFraction.formatterProperties())
                        .customHandler(logstashHandler)
                        .rootLogger(this.logstashFraction.level(), logstashHandler.getKey());
            } else {
                System.err.println("not enabling logstash, no host set");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
