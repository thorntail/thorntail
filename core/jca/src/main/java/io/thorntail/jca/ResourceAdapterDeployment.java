package io.thorntail.jca;

import java.io.File;

import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.spec.Connector;

/**
 * Deployment metadata for JCA resource adapters.
 *
 * <p>This class references IronJacamar-related classes for definition implementation.</p>
 *
 * <p>Instances of this class should be added to the {@link ResourceAdapterDeployments} component.</p>
 *
 * <p>Usually there is no need to directly create instance of this class, though.
 * By using configuration property {@code jca.resource-adapters} set to an array of names,
 * deployments will be automatically created.</p>
 *
 * <p>For instance, the property may be set to a value such as {@code [mymq, myeis]}. This will cause
 * the system to automatically seek out the files {@code META-INF/mymq-ra.xml} and {@code META-INF/myeis-ra.xml}
 * and create resource-adapter deployments from them.</p>
 *
 * <p>Should the above auto-configuration not suffice in locating or customizing a resource adapter,
 * you may wish to use the {@link ResourceAdapterDeploymentFactory} to convert an {@code ra.xml}-format
 * file into a {@code ResourceAdapterDeployment}. You may then perform further modification upon it before
 * adding to the {@code ResourceAdapterDeployments} component you {@code @Inject}.</p>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class ResourceAdapterDeployment {


    /**
     * Construct a new deployment.
     *
     * @param uniqueId  The unique identifier for this resource adapter.
     * @param connector The IronJacamar definition of the connector.
     */
    public ResourceAdapterDeployment(String uniqueId, Connector connector) {
        this(uniqueId, new File(uniqueId + ".rar"), connector, null);
    }

    /**
     * Construct a new deployment.
     *
     * @param uniqueId  The unique identifier for this resource adapter.
     * @param root      The root file of the resource adapter archive.
     * @param connector The IronJacamar definition of the connector.
     */
    public ResourceAdapterDeployment(String uniqueId, File root, Connector connector) {
        this(uniqueId, root, connector, null);
    }

    /**
     * Construct a new deployment.
     *
     * @param uniqueId   The unique identifier for this resource adapter.
     * @param root       The root file of the resource adapter archive.
     * @param connector  The IronJacamar definition of the connector.
     * @param activation the IronJacamar definition of the activation.
     */
    public ResourceAdapterDeployment(String uniqueId, File root, Connector connector, Activation activation) {
        this.uniqueId = uniqueId;
        this.root = root;
        this.connector = connector;
        this.activation = activation;
    }

    /**
     * Retrieve the unique identifier.
     *
     * @return The unique identifier.
     */
    public String getUniqueId() {
        return this.uniqueId;
    }

    /**
     * Retrieve the root of the resource-adapter archive.
     *
     * <p>If not explicitly set through the constructor, a synthetic file
     * with a name constructed from {@link #getUniqueId()} with a ".rar" suffix will be used.</p>
     *
     * @return
     */
    public File getRoot() {
        return this.root;
    }

    /**
     * Retrieve the IronJacamar connector definition.
     *
     * @return The IronJacamar connector definition.
     */
    public Connector getConnector() {
        return this.connector;
    }

    /**
     * Retrieve the IronJacamar activation definition.
     *
     * @return The IronJacamar activation definition.
     */
    public Activation getActivation() {
        return this.activation;
    }

    private final String uniqueId;

    private final File root;

    private final Connector connector;

    private final Activation activation;
}
