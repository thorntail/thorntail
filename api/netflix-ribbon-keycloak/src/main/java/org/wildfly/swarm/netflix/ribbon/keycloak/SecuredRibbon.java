package org.wildfly.swarm.netflix.ribbon.keycloak;

import com.netflix.client.config.ClientConfigFactory;
import com.netflix.ribbon.RibbonResourceFactory;
import com.netflix.ribbon.RibbonTransportFactory;
import com.netflix.ribbon.proxy.processor.AnnotationProcessorsProvider;

/**
 * @author Bob McWhirter
 */
public class SecuredRibbon extends RibbonResourceFactory {

    public static SecuredRibbon INSTANCE = new SecuredRibbon(ClientConfigFactory.DEFAULT, new SecuredTransportFactory(), AnnotationProcessorsProvider.DEFAULT);

    public SecuredRibbon(ClientConfigFactory configFactory, RibbonTransportFactory transportFactory, AnnotationProcessorsProvider processors) {
        super(configFactory, transportFactory, processors);
    }

}
