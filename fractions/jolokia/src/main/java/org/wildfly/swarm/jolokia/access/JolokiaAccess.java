package org.wildfly.swarm.jolokia.access;

import java.util.ArrayList;
import java.util.List;

/**
 * API for jolokia-access.xml configuration.
 *
 * @author Bob McWhirter
 */
public class JolokiaAccess {

    public interface Supplier extends java.util.function.Supplier<JolokiaAccess> {

    }

    public interface Consumer extends java.util.function.Consumer<JolokiaAccess> {

    }

    public JolokiaAccess() {

    }

    public JolokiaAccess host(String host) {
        this.hosts.add(host);
        return this;
    }

    public JolokiaAccess command(String command) {
        this.commands.add(command);
        return this;
    }

    public JolokiaAccess httpMethod(String method) {
        this.methods.add(method);
        return this;
    }

    public JolokiaAccess allowOrigin(String origin) {
        this.origins.add(origin);
        return this;
    }

    public JolokiaAccess strictChecking() {
        this.strictChecking = true;
        return this;
    }

    public JolokiaAccess allow(Section.Consumer config) {
        Section rule = Section.allow();
        config.accept(rule);
        this.sections.add(rule);
        return this;
    }

    public JolokiaAccess deny(Section.Consumer config) {
        Section rule = Section.deny();
        config.accept(rule);
        this.sections.add(rule);
        return this;
    }

    public String toXML() {
        StringBuilder builder = new StringBuilder();

        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        builder.append("<restrict>\n");

        if (!this.hosts.isEmpty()) {
            builder.append("  <remote>\n");
            this.hosts.forEach(e -> {
                builder.append("    <host>").append(e).append("</host>\n");
            });
            builder.append("  </remote>\n");
        }

        if (!this.methods.isEmpty()) {
            builder.append("  <http>\n");
            this.methods.forEach(e -> {
                builder.append("    <method>").append(e).append("</method>\n");
            });
            builder.append("  </http>\n");
        }

        if (!this.origins.isEmpty()) {
            builder.append("  <cors>\n");
            this.origins.forEach(e -> {
                builder.append("    <allow-origin>").append(e).append("</allow-origin>\n");
            });
            if ( this.strictChecking ) {
                builder.append("    <strict-checking/>\n" );
            }
            builder.append("  </cors>\n");
        }

        if (!this.commands.isEmpty()) {
            builder.append("  <commands>\n");
            this.commands.forEach(e -> {
                builder.append("    <command>").append(e).append("</command>\n");
            });
            builder.append("  </commands>\n");
        }

        if (!this.sections.isEmpty()) {
            this.sections.forEach(e -> {
                builder.append("  <" + e.type() + ">\n");
                e.mbeans().forEach(mbean -> {
                    builder.append("    <mbean>\n");
                    builder.append("      <name>").append(mbean.name()).append("</name>\n");
                    mbean.attributes().forEach(attr -> {
                        builder.append("      <attribute>").append(mbean.name()).append("</attribute>\n");
                    });
                    mbean.operations().forEach(attr -> {
                        builder.append("      <operation>").append(mbean.name()).append("</operation>\n");
                    });
                    builder.append("    </mbean>\n");
                });
                builder.append("  </" + e.type() + ">\n");
            });
        }

        builder.append("</restrict>\n");

        return builder.toString();
    }

    private List<String> hosts = new ArrayList<>();

    private List<String> origins = new ArrayList<>();

    private List<String> methods = new ArrayList<>();

    private List<String> commands = new ArrayList<>();

    private boolean strictChecking = false;

    private List<Section> sections = new ArrayList<>();
}
