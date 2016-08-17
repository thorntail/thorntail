package org.wildfly.swarm.jmx;

/** Configuration property keys for the JMX fraction.
 *
 * @author Bob McWhirter
 */
public class JMXProperties {

    /** Key for configuration value to enable and optionally select remote JMX endpoint.
     *
     * <p>Values may include</p>
     *
     * <ul>
     *     <li><code>management</code> to explicitly select the management interface</li>
     *     <li><code>http</code> to select the HTTP interface</li>
     *     <li>any other non-null value to allow for auto-detection.</li>
     * </ul>
     */
    public static final String REMOTE = "swarm.jmx.remote";
}
