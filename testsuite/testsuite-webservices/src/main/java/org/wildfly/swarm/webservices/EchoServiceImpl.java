package org.wildfly.swarm.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.servlet.annotation.WebServlet;

/**
 * @author Ken Finnigan
 */
@WebServlet("/ws/echo")
@WebService(
        name = "echo",
        serviceName = EchoService.SERVICE_NAME,
        targetNamespace = EchoService.NAMESPACE,
        endpointInterface = "org.wildfly.swarm.webservices.EchoService"
)
public class EchoServiceImpl implements EchoService {

    @Override
    @WebMethod
    public String echo(final String input) {
        return "ECHO:" + input;
    }
}
