package org.wildfly.swarm;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Bob McWhirter
 */
public class HollowSwarm {

    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            System.err.println("Provide one or more deployments as arguments");
            System.exit(1);
        }

        if (System.getProperty("boot.module.loader") == null) {
            System.setProperty("boot.module.loader", "org.wildfly.swarm.bootstrap.modules.BootModuleLoader");
        }

        Swarm swarm = new Swarm(args);
        swarm.start();
        archives(swarm.getCommandLine().extraArguments()).forEach(archive -> {
                    try {
                        swarm.deploy(archive);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    protected static List<Archive> archives(List<String> paths) {
        return paths.stream()
                .map(e -> Paths.get(e))
                .map(path -> {
                    String simpleName = path.getFileName().toString();
                    Archive archive = ShrinkWrap.create(JavaArchive.class, simpleName);
                    archive.as(ZipImporter.class).importFrom(path.toFile());
                    return archive;
                })
                .collect(Collectors.toList());
    }
}
