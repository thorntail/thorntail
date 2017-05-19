package org.wildfly.swarm.topology.runtime;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.topology.Advertise;
import org.wildfly.swarm.topology.Advertises;
import org.wildfly.swarm.topology.TopologyArchive;

/**
 * @author Bob McWhirter
 */
@DeploymentScoped
public class AdvertisingMetadataProcessor implements DeploymentProcessor {

    final Archive archive;
    final IndexView index;

    @Inject
    public AdvertisingMetadataProcessor(Archive archive, IndexView index) {
        this.archive = archive;
        this.index = index;
    }

    @Override
    public void process() {
        Collection<AnnotationInstance> annos = index.getAnnotations(DotName.createSimple(Advertise.class.getName()));
        Collection<AnnotationInstance> repeatingAnnos = index.getAnnotations(DotName.createSimple(Advertises.class.getName()));

        Stream.concat(annos.stream(),
                      repeatingAnnos
                              .stream()
                              .flatMap(anno -> Stream.of(anno.value().asNestedArray())))
                .forEach(anno -> advertise(archive, anno));
    }

    private void advertise(Archive<?> archive, AnnotationInstance anno) {

        String serviceName = anno.value().asString();
        List<String> tags = Optional.ofNullable(anno.value(Advertise.TAGS_ATTRIBUTE_NAME))
                .map(AnnotationValue::asStringArray)
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
        TopologyArchive topologyArchive = archive.as(TopologyArchive.class);
        topologyArchive.advertise(serviceName, tags);
    }

}
