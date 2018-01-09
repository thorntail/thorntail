package org.wildfly.swarm.bootstrap.modules;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.jboss.modules.maven.MavenResolver;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

public class JBossModulesMavenResolver implements MavenResolver {

    public JBossModulesMavenResolver(ArtifactResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public File resolveArtifact(ArtifactCoordinates coordinates, String packaging) throws IOException {
        File file = this.resolutionCache.get(coordinates);
        if (file != null) {
            return file;
        }

        ArtifactResolution resolution = this.delegate.resolveArtifact(coordinates, packaging);
        if (resolution == null) {
            return null;
        }
        if (resolution.isFile()) {
            return resolution.getFile();
        }


        file = copyTempJar(coordinates, resolution.openStream(), packaging);
        this.resolutionCache.put(coordinates, file);
        return file;
    }

    private Map<ArtifactCoordinates, File> resolutionCache = new ConcurrentHashMap<>();

    public static File copyTempJar(ArtifactCoordinates coords, InputStream in, String packaging) throws IOException {
        //String name = coords.getGroupId().replace('.', '-' ) + "@" + coords.getArtifactId();
        String name = "a";
        File tmp = TempFileManager.INSTANCE.newTempFile(name, "." + packaging);
        Files.copy(in, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tmp;
    }

    private final ArtifactResolver delegate;

}
