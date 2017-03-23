package org.wildfly.swarm.container.runtime.wildfly;

import org.jboss.as.repository.ContentRepository;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistryException;

/** This is a hack to overcome the fact wildfly-core installs
 *  a badly-written ContentRepository, and we don't want to have
 *  to PR upstream.
 *
 * @author Bob McWhirter
 */
public class ContentRepositoryServiceActivator implements ServiceActivator {

    private final SwarmContentRepository repository;

    public ContentRepositoryServiceActivator(SwarmContentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        ServiceController oldContentRepository = context.getServiceRegistry().getService(ContentRepository.SERVICE_NAME);

        oldContentRepository.addListener(new AbstractServiceListener() {
            @Override
            public void transition(ServiceController controller, ServiceController.Transition transition) {
                super.transition(controller, transition);
                if (transition.getAfter() == ServiceController.Substate.REMOVED) {
                    SwarmContentRepository.addService(context.getServiceTarget(), repository);
                }
            }
        });
        oldContentRepository.setMode(ServiceController.Mode.REMOVE);
    }
}
