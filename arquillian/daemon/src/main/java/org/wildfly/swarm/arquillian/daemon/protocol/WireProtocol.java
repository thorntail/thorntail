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
package org.wildfly.swarm.arquillian.daemon.protocol;

import java.nio.charset.Charset;

/**
 * Defines the wire protocol for the Arquillian Server Daemon.
 *
 * To stop: <code>CMD stop<<EOF</code>  To execute tests:
 * <code>CMD test ${deploymentName} ${FQN test class} ${methodName}<<EOF</code>
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 * @author Toby Crawley
 */
public interface WireProtocol {

    /**
     * Charset used in codec of String/bytes
     */
    Charset CHARSET = Charset.forName("UTF-8");

    String PREFIX_STRING_COMMAND = "CMD ";

    String COMMAND_CHECK_DEPLOYMENT = PREFIX_STRING_COMMAND + "checkdeployment";

    String COMMAND_STOP = PREFIX_STRING_COMMAND + "stop";

    /**
     * To be prepended to the FQN of the test class, then the method name, then
     * {@link WireProtocol#COMMAND_EOF_DELIMITER}
     */
    String COMMAND_TEST_PREFIX = PREFIX_STRING_COMMAND + "test ";

    /**
     * Marks the end of a command
     */
    char COMMAND_EOF_DELIMITER = '\n';

    String RESPONSE_OK_PREFIX = "OK ";
    String RESPONSE_ERROR_PREFIX = "ERR ";

}
