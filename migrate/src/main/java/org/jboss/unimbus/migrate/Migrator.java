package org.jboss.unimbus.migrate;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.jboss.unimbus.migrate.config.ConfigRule;
import org.jboss.unimbus.migrate.maven.ModelRule;

/**
 * Created by bob on 3/13/18.
 */
public class Migrator {

    public static final String GROUP_ID = "org.jboss.unimbus";

    public Migrator(Path root, Iterable<ModelRule> modelRules, Iterable<ConfigRule> configRules) {
        this.root = root;
        this.modelRules = modelRules;
        this.configRules = configRules;
    }

    void migrate() throws IOException {
        List<FileMigrator> migrators = new ArrayList<>();
        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.equals("pom.xml")) {
                    try {
                        migrators.add(new PomXmlMigrator(file, modelRules));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if ( fileName.startsWith("project-") && ( fileName.endsWith( ".yml" ) || fileName.endsWith(".yaml"))) {
                    migrators.add( new ConfigMigrator(file, configRules));

                }
                return super.visitFile(file, attrs);
            }
        };

        Files.walkFileTree(this.root, visitor);

        for (FileMigrator migrator : migrators) {
            try {
                migrator.migrate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final Path root;

    private final Iterable<ModelRule> modelRules;
    private final Iterable<ConfigRule> configRules;
}
