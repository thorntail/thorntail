package org.wildfly.swarm.logstash.runtime;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.wildfly.swarm.config.logging.CustomFormatter;
import org.wildfly.swarm.config.logging.CustomHandler;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.logstash.LogstashFraction;

import static org.fest.assertions.Assertions.assertThat;
import static org.wildfly.swarm.logstash.LogstashProperties.DEFAULT_HOSTNAME;
import static org.wildfly.swarm.logstash.LogstashProperties.DEFAULT_PORT;

/**
 * @author Bob McWhirter
 */
public class LogstashCustomizerTest {

    private LogstashCustomizer customizer;

    @Before
    public void setUp() {
        this.customizer = new LogstashCustomizer();
        this.customizer.logging = new LoggingFraction().rootLogger(Level.INFO, "HANDLER");
        this.customizer.logstash = new LogstashFraction();
    }

    @Test
    public void testNotEnabled() {
        this.customizer.customize();

        assertThat( this.customizer.logging.subresources().customHandler("logstash-handler" ) ).isNull();
        assertThat( this.customizer.logging.subresources().customFormatter( "logstash" )).isNull();
    }

    @Test
    public void testExplicitlyEnabledWithDefaults() {
        this.customizer.logstash.enabled(true);
        this.customizer.customize();

        CustomHandler handler = this.customizer.logging.subresources().customHandler("logstash-handler");
        assertThat( handler ).isNotNull();

        Map props = handler.properties();

        assertThat( props.get("hostname") ).isEqualTo( DEFAULT_HOSTNAME );
        assertThat( props.get("port") ).isEqualTo( "" + DEFAULT_PORT );

        CustomFormatter formatter = this.customizer.logging.subresources().customFormatter("logstash");
        assertThat( formatter ).isNotNull();
    }

    @Test
    public void testExplicitlyEnabledWithLevelConfigValue() {
        this.customizer.logstash.level(Level.DEBUG);
        this.customizer.logstash.enabled(true);
        this.customizer.customize();

        assertThat(customizer.logging.subresources().customHandler("logstash-handler").level())
                .isEqualTo(Level.DEBUG);
    }

    @Test
    public void testImplicitlyEnabledWithHostnameConfigValue() {
        this.customizer.logstash.hostname( "logstash.mycorp.com");
        assertThat( this.customizer.logstash.enabled() ).isTrue();

        this.customizer.customize();

        CustomHandler handler = this.customizer.logging.subresources().customHandler("logstash-handler");
        assertThat( handler ).isNotNull();

        Map props = handler.properties();


        assertThat( props.get("hostname") ).isEqualTo( "logstash.mycorp.com" );
        assertThat( props.get("port") ).isEqualTo( "" + DEFAULT_PORT );

        CustomFormatter formatter = this.customizer.logging.subresources().customFormatter("logstash");
        assertThat( formatter ).isNotNull();
    }

    @Test
    public void testImplicitlyEnabledWithPortConfigValue() {
        this.customizer.logstash.port( 8675 );
        assertThat( this.customizer.logstash.enabled() ).isTrue();

        this.customizer.customize();

        CustomHandler handler = this.customizer.logging.subresources().customHandler("logstash-handler");
        assertThat( handler ).isNotNull();

        Map props = handler.properties();


        assertThat( props.get("hostname") ).isEqualTo( DEFAULT_HOSTNAME );
        assertThat( props.get("port") ).isEqualTo( "8675" );

        CustomFormatter formatter = this.customizer.logging.subresources().customFormatter("logstash");
        assertThat( formatter ).isNotNull();
    }

    @Test
    public void testImplicitlyEnabledWithHostnameFractionSetting() {
        this.customizer.logstash.hostname( "logstash.mycorp.com");

        this.customizer.customize();
        assertThat( this.customizer.logstash.enabled() ).isTrue();

        CustomHandler handler = this.customizer.logging.subresources().customHandler("logstash-handler");
        assertThat( handler ).isNotNull();

        Map props = handler.properties();


        assertThat( props.get("hostname") ).isEqualTo( "logstash.mycorp.com" );
        assertThat( props.get("port") ).isEqualTo( "" + DEFAULT_PORT );

        CustomFormatter formatter = this.customizer.logging.subresources().customFormatter("logstash");
        assertThat( formatter ).isNotNull();
    }

    @Test
    public void testAddingLogstashHandlerToExistingRootHandlers() {
        this.customizer.logging.rootLogger(Level.INFO, "HANDLER1", "HANDLER2");

        this.customizer.logstash.enabled(true);
        this.customizer.customize();

        assertThat(customizer.logging.subresources().rootLogger().handlers())
                .contains("HANDLER1", "HANDLER2", "logstash-handler");
    }

    @Test
    public void testHonoringExistingRootLoggerLevel() {
        this.customizer.logging.rootLogger(Level.WARN, "HANDLER");

        this.customizer.logstash.enabled(true);
        this.customizer.customize();

        assertThat(customizer.logging.subresources().rootLogger().level())
                .isEqualTo(Level.WARN);
    }

}
