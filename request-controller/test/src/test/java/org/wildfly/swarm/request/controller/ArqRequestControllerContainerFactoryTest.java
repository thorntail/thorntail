package org.wildfly.swarm.request.controller;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author alexsoto
 */
@RunWith(Arquillian.class)
public class ArqRequestControllerContainerFactoryTest {

    @Deployment(testable = false)
    public static Archive createDeployment() {
        JARArchive deployment = ShrinkWrap.create(JARArchive.class);
        deployment.add(EmptyAsset.INSTANCE, "nothing");
        return deployment;
    }

    @org.wildfly.swarm.arquillian.adapter.ContainerFactory
    public static Class<? extends ContainerFactory> newContainerFactory() throws Exception {
        return RequestControllerContainerFactory.class;
    }

    @Test
    @RunAsClient
    public void testNothing() {

    }

}
