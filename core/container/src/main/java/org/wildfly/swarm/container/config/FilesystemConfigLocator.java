package org.wildfly.swarm.container.config;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class FilesystemConfigLocator extends ConfigLocator {

    public FilesystemConfigLocator() {
        this(Paths.get("."));
    }

    public FilesystemConfigLocator(Path root) {
        this.root = root;
    }

    @Override
    public Stream<URL> locate(String profileName) throws IOException {
        List<URL> located = new ArrayList<>();

        Path path = this.root.resolve(PROJECT_PREFIX + profileName + ".yml");

        if (Files.exists(path)) {
            located.add(path.toUri().toURL());
        }

        path = this.root.resolve(PROJECT_PREFIX + profileName + ".properties");
        if (Files.exists(path)) {
            located.add(path.toUri().toURL());
        }

        return located.stream();
    }

    private final Path root;

}
