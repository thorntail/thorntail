package org.wildfly.swarm.undertow;

import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class JBossWebAsset implements Asset{


    private final static String JBOSS_WEB_CONTENTS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<jboss-web>\n" +
                    "    <context-root>${CONTEXT_ROOT}</context-root>\n" +
                    "</jboss-web>";

    private String contextRoot = "/";

    public JBossWebAsset() {

    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    @Override
    public InputStream openStream() {
        String contents = JBOSS_WEB_CONTENTS.replace( "${CONTEXT_ROOT}", contextRoot.trim() );
        return new ByteArrayInputStream( contents.getBytes() );
    }
}
