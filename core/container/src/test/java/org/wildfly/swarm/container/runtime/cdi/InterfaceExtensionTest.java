package org.wildfly.swarm.container.runtime.cdi;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.spi.api.cdi.CommonBean;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.api.config.SimpleKey;

public class InterfaceExtensionTest {
    
    @InjectMocks
    private InterfaceExtension ext;
    
    @Test
    public void testAfterBeanDiscovery() throws Exception {
        ConfigView configView = mock(ConfigView.class);
        List<SimpleKey> interfaces = Arrays.asList(new SimpleKey("test"));
        when(configView.simpleSubkeys(any(ConfigKey.class))).thenReturn(interfaces);
        when(configView.valueOf(any(ConfigKey.class))).thenReturn("192.168.1.1");
        ext = new InterfaceExtension(configView);
        
        BeanManager beanManager = mock(BeanManager.class);
        AfterBeanDiscovery abd = mock(AfterBeanDiscovery.class);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<CommonBean<Interface>> captor = ArgumentCaptor.forClass(CommonBean.class);
        
        ext.afterBeanDiscovery(abd, beanManager);
        
        verify(abd,times(1)).addBean(captor.capture());
        assertThat(captor.getValue().create(null).getName()).isEqualTo("test");
        assertThat(captor.getValue().create(null).getExpression()).isEqualTo("192.168.1.1");
    }

}
