package org.wildfly.swarm.topology.runtime;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.ArchiveMetadataProcessor;
import org.wildfly.swarm.topology.Advertise;
import org.wildfly.swarm.topology.Advertises;
import org.wildfly.swarm.topology.TopologyArchive;

/**
 * @author Bob McWhirter
 */
public class AdvertisingMetadataProcessor implements ArchiveMetadataProcessor {

    @Override
    public void processArchive(Archive<?> archive, Index index) {
        List<AnnotationInstance> annos = index.getAnnotations(DotName.createSimple(Advertise.class.getName()));
        List<AnnotationInstance> repeatingAnnos = index.getAnnotations(DotName.createSimple(Advertises.class.getName()));

        List<String> names = Stream.concat(annos.stream(),
                                           repeatingAnnos
                                                   .stream()
                                                   .flatMap(anno -> Stream.of(anno.value().asNestedArray())))
                .map(anno -> anno.value().asString())
                .collect(Collectors.toList());

        if (!names.isEmpty()) {
            archive.as(TopologyArchive.class)
                    .advertise(names);
        }

    }

}
