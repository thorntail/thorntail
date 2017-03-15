package org.wildfly.swarm.jolokia.access;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;

/**
 * @author Bob McWhirter
 */
public class ConfigurationValueAccessPreparer extends AbstractJolokiaAccessPreparer {


    public ConfigurationValueAccessPreparer(String jolokiaAccessXml) {
        this.jolokiaAccessXml = jolokiaAccessXml;
    }

    @Override
    protected Asset getJolokiaAccessXmlAsset() {
        if (this.jolokiaAccessXml == null) {
            return null;
        }

        try {
            URL url = null;
            try {
                url = new URL(this.jolokiaAccessXml);
            } catch (MalformedURLException e) {
                File file = new File(this.jolokiaAccessXml);
                if (file.exists()) {
                    url = file.toURI().toURL();
                }
            }

            if (url != null) {
                return new UrlAsset(url);
            }
        } catch (MalformedURLException e) {
            // ignore;
        }

        return null;
    }

    private final String jolokiaAccessXml;

}
