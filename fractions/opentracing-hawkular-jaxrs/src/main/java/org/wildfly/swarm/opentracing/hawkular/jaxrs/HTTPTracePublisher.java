package org.wildfly.swarm.opentracing.hawkular.jaxrs;

/**
 * Created by bob on 5/23/17.
 */
public class HTTPTracePublisher {

    public HTTPTracePublisher() {

    }

    public HTTPTracePublisher userName(String userName) {
        this.userName = userName;
        return this;
    }

    public String userName() {
        return this.userName;
    }

    public HTTPTracePublisher password(String password) {
        this.password = password;
        return this;
    }

    public String password() {
        return this.password;
    }

    public HTTPTracePublisher url(String url) {
        this.url = url;
        return this;
    }

    public String url() {
        return this.url;
    }

    private String userName;

    private String password;

    private String url;
}
