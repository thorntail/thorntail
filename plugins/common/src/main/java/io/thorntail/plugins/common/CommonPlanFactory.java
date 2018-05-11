package io.thorntail.plugins.common;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import bin.Run;

import static bin.Run.MAIN_CLASS_ATTRIBUTE_NAME;
import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.jar.Attributes.Name.MAIN_CLASS;
import static java.util.jar.Attributes.Name.MANIFEST_VERSION;

/**
 * Created by bob on 2/13/18.
 */
public class CommonPlanFactory {

    public static Plan dependencies(Stream<File> files) {
        Plan plan = new Plan();

        Path lib = Paths.get("lib");

        files.forEach(file -> {
            plan.add(new FileEntry(lib.resolve(file.getName()), file));
        });

        return plan;
    }

    public static Plan application(Plan parent, File file) {
        Plan plan = new Plan(parent);

        Path app = Paths.get("app");
        plan.add(new FileEntry(app.resolve(file.getName()), file));

        return plan;
    }

    public static Plan confDir(Plan parent) {

        Plan plan = new Plan(parent);

        Path conf = Paths.get( "conf");
        plan.add(new ClasspathEntry(conf.resolve("README.txt"), "conf/README.txt"));
        return plan;
    }

    public static Plan scripts(Plan parent, String mainClass) {
        Plan plan = new Plan(parent);

        plan.add(new ClasspathEntry(Paths.get("README.txt"), "README.txt"));

        Path bin = Paths.get("bin");

        Entry runSh = new ClasspathEntry(bin.resolve("run.sh"), "run.sh");
        if (mainClass != null) {
            runSh = runSh.filter("MAIN_CLASS", mainClass);
        }

        runSh = runSh.withPermissions(OWNER_EXECUTE,
                                      OWNER_READ,
                                      OWNER_WRITE,
                                      GROUP_EXECUTE,
                                      GROUP_READ,
                                      OTHERS_EXECUTE,
                                      OTHERS_READ);

        plan.add(runSh);

        Entry runBat = new ClasspathEntry(bin.resolve("run.bat"), "run.bat");
        if (mainClass != null) {
            runBat = runBat.filter("MAIN_CLASS", mainClass);
        }

        plan.add(runBat);

        return plan;
    }

    public static Plan bootClass(Plan parent) {
        Plan plan = new Plan(parent);
        plan.add(new ClasspathEntry(Paths.get("bin/Run.class"), "bin/Run.class"));
        return plan;
    }

    public static Plan manifest(Plan parent, String mainClass) {
        Plan plan = new Plan(parent);
        Manifest manifest = new Manifest();
        Attributes atts = manifest.getMainAttributes();
        atts.put(MANIFEST_VERSION, "1.0");
        atts.put(MAIN_CLASS, Run.class.getName());
        atts.put(MAIN_CLASS_ATTRIBUTE_NAME, mainClass);
        plan.add(new ManifestEntry(manifest));
        return plan;
    }
}
