package org.wildfly.swarm.logstash;

import java.util.Properties;

import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.logging.LoggingFraction;

/**
 * @author Ken Finnigan
 */
public class LogstashFraction implements Fraction {

    private Properties handlerProperties = new Properties();
    private Properties formatterProperties = new Properties();

    private String level = "INFO";

    public LogstashFraction() {
        this("wildflySwarmNode", "${jboss.node.name}");
        hostname( "${logstash.host}" );
    }

    public LogstashFraction(String nodeKey, String nodeValue) {
        this.formatterProperties.put(nodeKey, nodeValue);
    }

    public LogstashFraction level(String level) {
        this.level = level;
        return this;
    }

    public LogstashFraction hostname(String hostname) {
        this.handlerProperties.put("hostname", "${swarm.logstash.hostname:" + hostname + "}");
        return this;
    }

    public LogstashFraction port(String port) {
        this.handlerProperties.put("port", "${swarm.logstash.port:" + port + "}");
        return this;
    }

    public LogstashFraction port(int port) {
        port("" + port);
        return this;
    }

    public LogstashFraction metadata(String key, String value) {
        this.formatterProperties.put(key, value);
        return this;
    }

    @Override
    public void initialize(Container.InitContext initContext) {
        initContext.fraction(new LoggingFraction()
                .customFormatter("logstash", "org.jboss.logmanager.ext", "org.jboss.logmanager.ext.formatters.LogstashFormatter", this.formatterProperties)
                .customHandler("logstash-handler", "org.jboss.logmanager.ext", "org.jboss.logmanager.ext.handlers.SocketHandler", this.handlerProperties, "logstash")
                .rootLogger(this.level));
    }

    public static Fraction createDefaultLogstashFraction() {
        return createDefaultLogstashFraction(true);
    }

    public static Fraction createDefaultLogstashFraction(boolean loggingFractionIfNoLogstash) {
        String hostname = System.getProperty( "swarm.logstash.hostname" );
        String port = System.getProperty("swarm.logstash.port");

        if ( hostname != null && port != null ) {
            return new LogstashFraction()
                    .hostname(hostname)
                    .port(port);
        }

        if( loggingFractionIfNoLogstash ) {
            return LoggingFraction.createDefaultLoggingFraction();
        }
        return null;
    }

}
