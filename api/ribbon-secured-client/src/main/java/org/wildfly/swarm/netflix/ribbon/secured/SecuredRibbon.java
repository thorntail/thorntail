package org.wildfly.swarm.netflix.ribbon.secured;

import com.netflix.ribbon.proxy.RibbonDynamicProxy;

/**
 * @author Bob McWhirter
 */
public class SecuredRibbon {
    public static <T> T from(Class<T> classType) {
        return SecuredRibbonResourceFactory.INSTANCE.from( classType );
    }
}
