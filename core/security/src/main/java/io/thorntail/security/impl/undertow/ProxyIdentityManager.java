package io.thorntail.security.impl.undertow;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;

/**
 * Created by bob on 1/18/18.
 */
public class ProxyIdentityManager implements IdentityManager {

    public ProxyIdentityManager(BeanManager beanManager) {
        this.beanManager = beanManager;

    }
    private IdentityManager delegate() {
        Set<Bean<?>> beans = this.beanManager.getBeans(BasicIdentityManager.class);
        Bean<BasicIdentityManager> bean = (Bean<BasicIdentityManager>) this.beanManager.resolve(beans);
        CreationalContext<BasicIdentityManager> context = this.beanManager.createCreationalContext(bean);
        BasicIdentityManager delegate = bean.create(context);
        return delegate;
    }

    @Override
    public Account verify(Account account) {
        return delegate().verify(account);
    }

    @Override
    public Account verify(String id, Credential credential) {
        return delegate().verify(id, credential);
    }

    @Override
    public Account verify(Credential credential) {
        return delegate().verify(credential);
    }

    private final BeanManager beanManager;
}
