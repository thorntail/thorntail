package org.jboss.unimbus.migrate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import org.jboss.unimbus.migrate.config.ConfigRule;
import org.yaml.snakeyaml.Yaml;

/**
 * Created by bob on 3/13/18.
 */
public class ConfigMigrator extends FileMigrator<Map, Properties> {

    public ConfigMigrator(Path file, Iterable<ConfigRule> rules) {
        super(file, rules);
    }

    @Override
    protected Map createInputContext() throws Exception {
        Yaml yaml = new Yaml();
        return yaml.load(new FileInputStream(this.file.toFile()));
    }

    @Override
    protected Properties createOutputContext() throws Exception {
        return new Properties();
    }

    @Override
    protected void finish(Properties output) throws Exception {
        Path path = outputPath();
        System.err.println("write properties related to " + this.file);
        output.store(new FileOutputStream(path.toFile()), "");
    }

    private Path outputPath() {
        Path dir = this.file.getParent();

        String name = this.file.getFileName().toString();
        String base = name.substring(0, name.lastIndexOf("."));

        if (!base.endsWith("-defaults")) {
            int dashLoc = base.lastIndexOf("-");

            if (dashLoc > 0) {
                return dir.resolve("application-" + base.substring(dashLoc + 1) + ".properties");
            }
        }

        return dir.resolve("application.properties");
    }
}
