package org.wildfly.swarm.resourceadapters;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.Stateless;

import org.xadisk.connector.outbound.XADiskConnectionFactory;


/**
* This class provides basic file operation functionalities. The class is simple wrapper
* around the XADisk resource adapter. All file related tasks are delegated to an dedicated
* connector instance.
* <p>
* The main purpose of this class is to hide the XADisk details.
*
* @author <a href="mailto:ralf.battenfeld@six-group.com">Ralf Battenfeld</a>
*/
@Stateless
public class FileIOBean {
 
    @Resource(
        name               = "ra/XADiskConnectionFactory",
        type               = org.xadisk.connector.outbound.XADiskConnectionFactory.class,
        authenticationType = AuthenticationType.CONTAINER,
        mappedName         = "java:global/XADiskCF")
    private XADiskConnectionFactory _xaDiskConnectionFactory;

    public XADiskConnectionFactory getConnectionFactory() {
    	return _xaDiskConnectionFactory;
    }

}

