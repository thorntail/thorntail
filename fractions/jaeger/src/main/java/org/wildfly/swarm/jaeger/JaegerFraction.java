package org.wildfly.swarm.jaeger;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

import java.util.Optional;

import static io.jaegertracing.Configuration.*;

/**
 * @author Juraci Paixão Kröhling
 */
@DeploymentModule(name = "org.wildfly.swarm.jaeger", slot = "deployment")
@Configurable("swarm.jaeger")
public class JaegerFraction implements Fraction<JaegerFraction> {
    @AttributeDocumentation("The service name. Required (via this parameter, system property or env var). Ex.: `order-manager`")
    private Defaultable<String> serviceName = Defaultable.string(getDefault(JAEGER_SERVICE_NAME));

    @AttributeDocumentation("The sampler type. Ex.: `const`")
    private Defaultable<String> samplerType = Defaultable.string(getDefault(JAEGER_SAMPLER_TYPE));
    @AttributeDocumentation("The sampler parameter (number). Ex.: `1`")
    private Defaultable<String> samplerParameter = Defaultable.string(getDefault(JAEGER_SAMPLER_PARAM));
    @AttributeDocumentation("The host name and port when using the remote controlled sampler")
    private Defaultable<String> samplerManagerHost = Defaultable.string(getDefault(JAEGER_SAMPLER_MANAGER_HOST_PORT));

    @AttributeDocumentation("Whether the reporter should also log the spans")
    private Defaultable<String> reporterLogSpans = Defaultable.string(getDefault(JAEGER_REPORTER_LOG_SPANS));
    @AttributeDocumentation("The hostname for communicating with agent via UDP")
    private Defaultable<String> agentHost = Defaultable.string(getDefault(JAEGER_AGENT_HOST));
    @AttributeDocumentation("The port for communicating with agent via UDP")
    private Defaultable<String> agentPort = Defaultable.string(getDefault(JAEGER_AGENT_PORT));
    @AttributeDocumentation("The reporter's flush interval (ms)")
    private Defaultable<String> reporterFlushInterval = Defaultable.string(getDefault(JAEGER_REPORTER_FLUSH_INTERVAL));
    @AttributeDocumentation("The reporter's maximum queue size")
    private Defaultable<String> reporterMaxQueueSize = Defaultable.string(getDefault(JAEGER_REPORTER_MAX_QUEUE_SIZE));

    @AttributeDocumentation("Whether to enable propagation of B3 headers in the configured Tracer. By default this is false.")
    private Defaultable<Boolean> enableB3HeaderPropagation = Defaultable.bool(false);

    @AttributeDocumentation("Remote Reporter HTTP endpoint for Jaeger collector, such as http://jaeger-collector.istio-system:14268/api/traces")
    private Defaultable<String> remoteReporterHttpEndpoint = Defaultable.string(getDefault(JAEGER_ENDPOINT));

    public String getServiceName() {
        return serviceName.get();
    }

    public String getSamplerType() {
        return samplerType.get();
    }

    public String getSamplerParameter() {
        return samplerParameter.get();
    }

    public String getSamplerManagerHost() {
        return samplerManagerHost.get();
    }

    public String getReporterLogSpans() {
        return reporterLogSpans.get();
    }

    public String getAgentHost() {
        return agentHost.get();
    }

    public String getAgentPort() {
        return agentPort.get();
    }

    public String getReporterFlushInterval() {
        return reporterFlushInterval.get();
    }

    public String getReporterMaxQueueSize() {
        return reporterMaxQueueSize.get();
    }

    public Boolean isB3HeaderPropagationEnabled() {
        return enableB3HeaderPropagation.get();
    }

    public String getRemoteReporterHttpEndpoint() {
        return remoteReporterHttpEndpoint.get();
    }

    @Override
    public String toString() {
        return "JaegerFraction{" +
                "serviceName='" + serviceName.get() + '\'' +
                ", samplerType='" + samplerType.get() + '\'' +
                ", samplerParameter='" + samplerParameter.get() + '\'' +
                ", samplerManagerHost='" + samplerManagerHost.get() + '\'' +
                ", reporterLogSpans='" + reporterLogSpans.get() + '\'' +
                ", agentHost='" + agentHost.get() + '\'' +
                ", agentPort='" + agentPort.get() + '\'' +
                ", reporterFlushInterval='" + reporterFlushInterval.get() + '\'' +
                ", reporterMaxQueueSize='" + reporterMaxQueueSize.get() + '\'' +
                '}';
    }

    private static String getDefault(String key) {
        return Optional.ofNullable(System.getProperty(key, System.getenv(key))).orElse("");
    }
}
