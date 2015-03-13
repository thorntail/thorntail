package org.wildfly.boot.plugin;

import org.apache.maven.artifact.Artifact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class Modules {

    private static Set<String> excludedModules = new HashSet<>();



    static {
        try {
            loadModuleExclusions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean excludeModule(String name) {
        return excludedModules.contains(name.replaceAll("/", "."));
    }

    private static void loadModuleExclusions() throws IOException {
        List<String> lines = load("exclude-modules.txt");
        excludedModules.addAll(lines);
    }

    private static List<String> load(String name) throws IOException {
        List<String> lines = new ArrayList<>();

        InputStream in = Modules.class.getClassLoader().getResourceAsStream(name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line = null;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }

            if (line.startsWith("//")) {
                continue;
            }

            lines.add(line);
        }


        return lines;
    }

}
