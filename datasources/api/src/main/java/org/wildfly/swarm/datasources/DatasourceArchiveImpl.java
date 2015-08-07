package org.wildfly.swarm.datasources;

import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;

/**
 * @author Bob McWhirter
 */
public class DatasourceArchiveImpl extends AssignableBase<ArchiveBase<?>> implements DatasourceArchive {
    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public DatasourceArchiveImpl(ArchiveBase<?> archive) {
        super(archive);
    }

    @Override
    public DatasourceArchive datasource(Datasource ds) {
        String name = ds.name() + "-ds.xml";

        getArchive().add( new DSXmlAsset( ds ), "META-INF/" + name );

        return this;
    }
}
