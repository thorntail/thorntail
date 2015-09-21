package org.wildfly.swarm.undertow;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.jbossweb60.JbossWebDescriptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.wildfly.swarm.container.util.ClassLoading.withTCCL;

/**
 * @author Bob McWhirter
 */
public class JBossWebAsset implements Asset{


    public JBossWebAsset() {
        this.descriptor =
                withTCCL(Descriptors.class.getClassLoader(),
                         () -> Descriptors.create(JbossWebDescriptor.class));
    }

    public JBossWebAsset(InputStream fromStream) {
        this.descriptor =
                withTCCL(Descriptors.class.getClassLoader(),
                         () -> Descriptors.importAs(JbossWebDescriptor.class)
                                                 .fromStream(fromStream));
    }

    public void setContextRoot(String contextRoot) {
        this.descriptor.contextRoot(contextRoot);
        rootSet = true;
    }

    public boolean isRootSet() {
        return rootSet;
    }

    @Override
    public InputStream openStream() {
        return new ByteArrayInputStream(this.descriptor.exportAsString().getBytes());
    }

    private final JbossWebDescriptor descriptor;
    private boolean rootSet = false;

}
