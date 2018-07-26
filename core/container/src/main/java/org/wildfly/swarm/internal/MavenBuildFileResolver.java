package org.wildfly.swarm.internal;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class MavenBuildFileResolver {

    private static final String POM_XML = "pom.xml";

    private static final String MAVEN_CMD_LINE_ARGS = "MAVEN_CMD_LINE_ARGS";

    private MavenBuildFileResolver() {
    }

    public static Path resolveMavenBuildFileName(String root) {
        String cmdLine = System.getenv(MAVEN_CMD_LINE_ARGS);
        String buildFileName = root + File.separator + POM_XML;

        if (cmdLine != null) {
            MavenArgsParser args = MavenArgsParser.parse(cmdLine);
            Optional<String> f_arg = args.get(MavenArgsParser.ARG.F);
            if (f_arg.isPresent()) {
                buildFileName = f_arg.get();
            }
        }

        if (Paths.get(buildFileName).toFile().isDirectory()) {
            buildFileName += File.separator + POM_XML;
        }

        if (Paths.get(buildFileName).isAbsolute()) {
            return Paths.get(buildFileName).normalize();
        } else {
            return Paths.get(root).resolve(buildFileName).toAbsolutePath().normalize();
        }

    }

}
