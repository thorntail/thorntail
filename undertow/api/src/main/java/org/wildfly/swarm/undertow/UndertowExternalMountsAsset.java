package org.wildfly.swarm.undertow;

import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UndertowExternalMountsAsset implements Asset {
    private List<String> externalMounts = new ArrayList<>();

    public void externalMount(String path) {
        externalMounts.add(path);
    }

    @Override
    public InputStream openStream() {
        StringBuilder conf = new StringBuilder();
        for (String each : this.externalMounts) {
            conf.append(each + "\n");
        }
        return new ByteArrayInputStream(conf.toString().getBytes());
    }
}
