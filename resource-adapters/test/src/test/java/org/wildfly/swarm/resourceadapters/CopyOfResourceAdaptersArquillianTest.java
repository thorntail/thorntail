/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.resourceadapters;

import java.io.File;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;
import org.xadisk.connector.outbound.XADiskConnectionFactory;

/**
 * @author Ralf Battenfeld
 */
@RunWith(Arquillian.class)
public class CopyOfResourceAdaptersArquillianTest implements ContainerFactory {

	@Deployment
    public static Archive<?> createDeployment1() {    	
    	final File[] files = Maven.resolver().resolve("net.java.xadisk:xadisk:jar:1.2.2").withoutTransitivity().asFile();
    	final RARArchive deploymentRar = ShrinkWrap.create(RARArchive.class, "xadisk.rar");
    	deploymentRar.addAsLibraries(files[0]);
    	deploymentRar.addAsManifestResource("ironjacamar.xml", "ironjacamar.xml");   
    	deploymentRar.setResourceAdapterXML("ra.xml");
    	
    	final JavaArchive deploymentEjb = ShrinkWrap.create(JavaArchive.class, "testejb.jar")
    			.addPackages(true, "org.xadisk")
    			.addClass(FileIOBean.class);

    	final EnterpriseArchive deploymentEar = ShrinkWrap.create(EnterpriseArchive.class, "testejb.ear")
//    			.addClass(MyServlet.class)
    			.addAsModule(deploymentRar)
    			.addAsModule(deploymentEjb);
    	
    	final WebArchive deploymentWar = ShrinkWrap.create(WebArchive.class, "testejb.war")
    			.addClass(MyServlet.class)
    			.addAsLibrary(deploymentRar)
    			.addAsLibrary(deploymentEjb);
    	
        return deploymentEjb;
    }
    
//    @Deployment(name = "dep2", order = 2)
//    public static Archive<?> createDeployment2() {
//    	final JavaArchive deployment = ShrinkWrap.create(JavaArchive.class, "testejb.jar");
//    	deployment.addClass(FileIOBean.class);
//        return deployment;
//    }
    
    @Override
    public Container newContainer(String... args) throws Exception {
        return new Container()
//            .fraction(UndertowFraction.createDefaultFraction())
            .fraction(new ResourceAdapterFraction());
//            .fraction(EJBFraction.createDefaultFraction());
    }

    @Resource(
        name               = "ra/XADiskConnectionFactory",
        type               = org.xadisk.connector.outbound.XADiskConnectionFactory.class,
        authenticationType = AuthenticationType.CONTAINER,
        mappedName         = "java:global/XADiskCF")
    private XADiskConnectionFactory _xaDiskConnectionFactory;
    
    @Test
    @RunAsClient
    public void testNothing() {
    }
    
//    @Test
//    public void testConnectionFactory() throws Exception {
//    	final Context ctx = new InitialContext();
//    	final FileIOBean fileIOBean = (FileIOBean)ctx.lookup("java:global/XADiskCF");
//    	assertNotNull(fileIOBean.getConnectionFactory());
//    }

}
