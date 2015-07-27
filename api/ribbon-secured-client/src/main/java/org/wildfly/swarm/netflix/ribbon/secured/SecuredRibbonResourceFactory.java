package org.wildfly.swarm.netflix.ribbon.secured;

import com.netflix.client.config.ClientConfigFactory;
import com.netflix.ribbon.RibbonResourceFactory;
import com.netflix.ribbon.RibbonTransportFactory;
import com.netflix.ribbon.proxy.processor.AnnotationProcessorsProvider;

/**
 * @author Bob McWhirter
 */
public class SecuredRibbonResourceFactory extends RibbonResourceFactory {

    public static SecuredRibbonResourceFactory INSTANCE = new SecuredRibbonResourceFactory(ClientConfigFactory.DEFAULT, new SecuredTransportFactory(), AnnotationProcessorsProvider.DEFAULT);

    public SecuredRibbonResourceFactory(ClientConfigFactory configFactory, RibbonTransportFactory transportFactory, AnnotationProcessorsProvider processors) {
        super(configFactory, transportFactory, processors);
    }


}
