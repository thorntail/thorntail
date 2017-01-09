package org.wildfly.swarm.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Ken Finnigan
 */
@WebService
@SOAPBinding
public interface EchoService {
    String NAMESPACE = "webservices.swarm.wildfly.org";
    String SERVICE_NAME = "echoService";

    @WebMethod
    String echo(@WebParam(name = "input") String input);
}
