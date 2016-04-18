package org.wildfly.swarm.resourceadapters;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.config.resource.adapters.ResourceAdapter.TransactionSupport;
import org.wildfly.swarm.config.resource.adapters.resource_adapter.ConfigProperties;
import org.wildfly.swarm.connector.ConnectorFraction;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

public class TestMain {
	public static void main(String[] args) throws Exception {
        final Container container = new Container()
            .fraction(new ConnectorFraction())
//            .fraction(LoggingFraction.createDefaultLoggingFraction())
            .fraction(new ResourceAdapterFraction().resourceAdapter("xadisk.rar", (ra) -> {
	        	ra.archive("xadisk-1.2.2.rar");
	        	ra.transactionSupport(TransactionSupport.XATRANSACTION);
	        	ra.configProperties(new ConfigProperties("xaDiskHome").value("diskHome"));
	        	ra.configProperties(new ConfigProperties("instanceId").value("instance1"));
	        	ra.connectionDefinitions("XADiskConnectionFactoryPool", (connDef) -> {
	        		connDef.className("org.xadisk.connector.outbound.XADiskManagedConnectionFactory");
	        		connDef.jndiName("java:global/XADiskCF");
	        		connDef.configProperties(new ConfigProperties("instanceId").value("instance1"));
	        		connDef.minPoolSize(1);
	        		connDef.maxPoolSize(5);
	        	}
	        	);
        }));

        container.start();
        container.deploy(Swarm.artifact("net.java.xadisk:xadisk:rar:1.2.2"));
        
        final JAXRSArchive appDeployment = ShrinkWrap.create(JAXRSArchive.class);
        appDeployment.addResource(MyResource.class);
        appDeployment.addClass(FileIOBean.class);
        appDeployment.addAsLibraries(Swarm.artifact("net.java.xadisk:xadisk:jar:1.2.2"));

        container.deploy(appDeployment);
        
        Thread.currentThread();
        Thread.sleep(80000);
        
        
        container.stop();
    }

}
