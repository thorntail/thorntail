package org.wildfly.swarm.spi.api;

/** Hook point to allow user-space (deployment) extensions for CDIi runtimes.
 *
 * @apiNote Typically advanced usage by the core.
 *
 * @author Bob McWhirter
 */
public interface UserSpaceExtensionFactory {

    /** Perform whatever is necessary to configure the user-space factory.
     *
     * @throws Exception
     */
    void configure() throws Exception;
}
