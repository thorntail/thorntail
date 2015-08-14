package org.wildfly.swarm.undertow;

import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class UndertowHandlersAsset implements Asset {

    private List<String[]> staticContent = new ArrayList<>();

    public void staticContent(String context, String base) {
        staticContent.add(new String[]{context, base});
    }

    @Override
    public InputStream openStream() {

        StringBuilder conf = new StringBuilder();
        for (String[] each : this.staticContent) {
            conf.append("path-prefix('" + each[0] + "') -> static-content(base='" + each[1] + "', prefix='" + each[0] + "')\n");
        }

        return new ByteArrayInputStream(conf.toString().getBytes());
    }
}
