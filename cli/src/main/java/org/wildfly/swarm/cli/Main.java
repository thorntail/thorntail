package org.wildfly.swarm.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.jboss.modules.Module;

/**
 * @author Bob McWhirter
 */
public class Main {
    private Main() {
    }

    public static void main(String... args) throws Exception {
        Path tmpFile = null;
        if (System.getProperty("jboss.cli.config") == null) {
            tmpFile = Files.createTempFile("jboss-cli", ".xml");
            Files.copy(Main.class.getResourceAsStream("/jboss-cli.xml"),
                    tmpFile,
                    StandardCopyOption.REPLACE_EXISTING);
            System.setProperty("jboss.cli.config", tmpFile.toAbsolutePath().toString());
        }
        Module cli = Module.getBootModuleLoader().loadModule("org.jboss.as.cli");
        try {
            cli.run(args);
        } finally {
            if (tmpFile != null) {
                Files.delete(tmpFile);
            }
        }
    }
}
