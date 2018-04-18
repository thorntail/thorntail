package io.thorntail.test.arquillian.impl;

import java.lang.annotation.Annotation;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import static io.thorntail.Info.ROOT_PACKAGE;

/**
 * Created by bob on 2/5/18.
 */
public class ThorntailURLResourceProvider implements ResourceProvider {
    @Override
    public boolean canProvide(Class<?> type) {
        return type.isAssignableFrom(URL.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        BeanManager bm = CDI.current().getBeanManager();
        Set<Bean<?>> beans = bm.getBeans(InetSocketAddress.class, Any.Literal.INSTANCE);
        for (Bean<?> bean : beans) {
            Set<Annotation> annos = bean.getQualifiers();
            for (Annotation anno : annos) {
                if (anno.annotationType().getName().equals(ROOT_PACKAGE + ".servlet.annotation.Primary")) {
                    CreationalContext<InetSocketAddress> context = bm.createCreationalContext((Bean<InetSocketAddress>) bean);
                    InetSocketAddress instance = ((Bean<InetSocketAddress>) bean).create(context);
                    try {
                        return toURL(instance);
                    } catch (URISyntaxException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    URL toURL(InetSocketAddress addr) throws URISyntaxException, MalformedURLException {
        if (addr.getAddress() instanceof Inet6Address) {
            return new URL("http://[" + addr.getHostName() + "]:" + addr.getPort() + "/");
        }
        return new URL("http://" + addr.getHostName() + ":" + addr.getPort() + "/");
    }
}
