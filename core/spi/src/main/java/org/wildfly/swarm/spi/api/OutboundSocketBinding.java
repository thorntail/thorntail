/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.spi.api;

/** An outbound socket-binding.
 *
 * <p>Define a named host-and-port combination for outbound communications.</p>
 *
 * @see SocketBindingGroup
 *
 * @author Bob McWhirter
 */
public class OutboundSocketBinding {

    private String name;

    private String hostExpr;

    private String portExpr;

    /** Construct a new outbound binding.
     *
     * @param name The name of the binding.
     */
    public OutboundSocketBinding(String name) {
        this.name = name;
    }

    /** Retrieve the name of the binding.
     *
     * @return The name of the binding.
     */
    public String name() {
        return this.name;
    }

    /** Set the remote-host name or expression.
     *
     * @param hostExpr The remote host name or expression.
     * @return this binding.
     */
    public OutboundSocketBinding remoteHost(String hostExpr) {
        this.hostExpr = hostExpr;
        return this;
    }

    /** Retrieve the remote-host name or expression.
     *
     * @return The remote host name or expression.
     */
    public String remoteHostExpression() {
        return this.hostExpr;
    }

    /** Set the remote port.
     *
     * @param port The remote port.
     * @return this binding.
     */
    public OutboundSocketBinding remotePort(int port) {
        this.portExpr = "" + port;
        return this;
    }

    /** Set the remote port expression
     *
     * @param portExpr The remote port expression
     * @return this binding.
     */
    public OutboundSocketBinding remotePort(String portExpr) {
        this.portExpr = portExpr;
        return this;
    }

    /** Retrieve the remote port expression.
     *
     * @return the remote port expression.
     */
    public String remotePortExpression() {
        return this.portExpr;
    }


}
