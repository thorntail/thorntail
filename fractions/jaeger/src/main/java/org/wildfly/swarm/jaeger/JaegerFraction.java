package org.wildfly.swarm.jaeger;

import org.jboss.logging.Logger;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

import static com.uber.jaeger.Configuration.*;

/**
 * @author Juraci Paixão Kröhling
 */
@DeploymentModule(name = "org.wildfly.swarm.jaeger", slot = "deployment")
@Configurable("swarm.jaeger")
public class JaegerFraction implements Fraction<JaegerFraction> {
    private static final Logger logger = Logger.getLogger(JaegerFraction.class);

    private String serviceName = getDefault(JAEGER_SERVICE_NAME);

    private String samplerType = getDefault(JAEGER_SAMPLER_TYPE);
    private String samplerParameter = getDefault(JAEGER_SAMPLER_PARAM);
    private String samplerManagerHost = getDefault(JAEGER_SAMPLER_MANAGER_HOST_PORT);

    private String reporterLogSpans = getDefault(JAEGER_REPORTER_LOG_SPANS);
    private String agentHost = getDefault(JAEGER_AGENT_HOST);
    private String agentPort = getDefault(JAEGER_AGENT_PORT);
    private String reporterFlushInterval = getDefault(JAEGER_REPORTER_FLUSH_INTERVAL);
    private String reporterMaxQueueSize = getDefault(JAEGER_REPORTER_MAX_QUEUE_SIZE);

    public String getServiceName() {
        return serviceName;
    }

    public String getSamplerType() {
        return samplerType;
    }

    public String getSamplerParameter() {
        return samplerParameter;
    }

    public String getSamplerManagerHost() {
        return samplerManagerHost;
    }

    public String getReporterLogSpans() {
        return reporterLogSpans;
    }

    public String getAgentHost() {
        return agentHost;
    }

    public String getAgentPort() {
        return agentPort;
    }

    public String getReporterFlushInterval() {
        return reporterFlushInterval;
    }

    public String getReporterMaxQueueSize() {
        return reporterMaxQueueSize;
    }

    @Override
    public String toString() {
        return "JaegerFraction{" +
                "serviceName='" + serviceName + '\'' +
                ", samplerType='" + samplerType + '\'' +
                ", samplerParameter='" + samplerParameter + '\'' +
                ", samplerManagerHost='" + samplerManagerHost + '\'' +
                ", reporterLogSpans='" + reporterLogSpans + '\'' +
                ", agentHost='" + agentHost + '\'' +
                ", agentPort='" + agentPort + '\'' +
                ", reporterFlushInterval='" + reporterFlushInterval + '\'' +
                ", reporterMaxQueueSize='" + reporterMaxQueueSize + '\'' +
                '}';
    }

    private static String getDefault(String key) {
        return System.getProperty(key, System.getenv(key));
    }
}
