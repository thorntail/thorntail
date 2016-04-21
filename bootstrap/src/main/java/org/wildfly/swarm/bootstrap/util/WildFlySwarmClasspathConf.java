/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.bootstrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmClasspathConf {

    public static final String CLASSPATH_LOCATION = "META-INF/wildfly-swarm-classpath.conf";

    private List<Matcher> matchers = new ArrayList<>();

    public WildFlySwarmClasspathConf() {

    }

    public WildFlySwarmClasspathConf(InputStream in) throws IOException {
        read(in);
    }

    public WildFlySwarmClasspathConf(ClassLoader classLoader) throws IOException {
        Enumeration<URL> classpathConfs = classLoader.getResources(WildFlySwarmClasspathConf.CLASSPATH_LOCATION);

        while (classpathConfs.hasMoreElements()) {
            URL each = classpathConfs.nextElement();
            try (InputStream in = each.openStream()) {
                read(in);
            }
        }
    }

    public Set<Action> getActions(JarFile jar) {
        return matchers.stream()
                .filter(e -> e.matches(jar))
                .map(e -> e.getAction())
                .collect(Collectors.toSet());
    }

    public Set<Action> getActions(File file, String groupId, String artifactId) {
        Set<Action> actions = null;

        try (JarFile jar = new JarFile(file)) {
            actions = getActions(jar);
        } catch (IOException e) {
            e.printStackTrace();
        }

        actions.addAll(getActions(groupId, artifactId));

        return actions;
    }

    public Set<Action> getActions(String groupId, String artifactId) {
        return matchers.stream()
                .filter(e -> e.matches(groupId, artifactId))
                .map(Matcher::getAction)
                .collect(Collectors.toSet());
    }

    public List<Matcher> getMatchesForActionType(Class<? extends Action> actionType) {
        return matchers.stream()
                .filter(m -> m.getAction().getClass().isAssignableFrom(actionType))
                .collect(Collectors.toList());
    }

    public void read(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String line = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                if (!line.isEmpty()) {
                    process(line);
                }
            }
        }
    }

    List<Matcher> getMatchers() {
        return this.matchers;
    }

    private static final String MAVEN_MATCHER_PREFIX = "maven(";

    private static final String PACKAGE_MATCHER_PREFIX = "package(";

    void process(String line) {
        int spaceLoc = line.indexOf(' ');
        if (spaceLoc < 0) {
            return;
        }

        String left = line.substring(0, spaceLoc);
        String right = line.substring(spaceLoc + 1);

        Matcher matcher = getMatcher(left);

        if (matcher == null) {
            System.err.println("problem parsing: " + line);
            return;
        }

        Action action = getAction(right);

        if (action == null) {
            System.err.println("problem parsing: " + line);
            return;
        }

        matcher.setAction(action);
        this.matchers.add(matcher);
    }

    Matcher getMatcher(String text) {
        if (text.startsWith(MAVEN_MATCHER_PREFIX)) {
            return getGAVMatcher(text);
        }
        if (text.startsWith(PACKAGE_MATCHER_PREFIX)) {
            return getPackageMatcher(text);
        }
        return null;
    }

    MavenMatcher getGAVMatcher(String text) {
        int rightParenLoc = text.indexOf(')');
        if (rightParenLoc < 0) {
            return null;
        }

        String gav = text.substring(MAVEN_MATCHER_PREFIX.length(), rightParenLoc);

        String[] parts = gav.split(":");
        return new MavenMatcher(parts[0], parts[1]);
    }

    PackageMatcher getPackageMatcher(String text) {
        int rightParenLoc = text.indexOf(')');
        if (rightParenLoc < 0) {
            return null;
        }

        String pkg = text.substring(PACKAGE_MATCHER_PREFIX.length(), rightParenLoc);

        return new PackageMatcher(pkg);
    }

    private static final String REPLACE_ACTION_PREFIX = "replace(";

    Action getAction(String text) {
        text = text.trim();
        if (text.equalsIgnoreCase("remove")) {
            return new RemoveAction();
        }

        if (text.startsWith(REPLACE_ACTION_PREFIX)) {
            int rightParenLoc = text.indexOf(')');
            if (rightParenLoc < 0) {
                return null;
            }

            String moduleSpec = text.substring(REPLACE_ACTION_PREFIX.length(), rightParenLoc);

            String parts[] = moduleSpec.split(":");

            String moduleName = parts[0];
            String moduleSlot = "main";
            if (parts.length > 1) {
                moduleSlot = parts[1];
            }

            return new ReplaceAction(moduleName, moduleSlot);
        }

        return null;

    }

    public static class Matcher {
        private Action action;

        void setAction(Action action) {
            this.action = action;
        }

        Action getAction() {
            return this.action;
        }

        boolean matches(JarFile file) {
            return false;
        }

        boolean matches(String groupId, String artifactId) {
            return false;
        }
    }

    public static class MavenMatcher extends Matcher {
        public final String groupId;

        public final String artifactId;

        public MavenMatcher(String groupId, String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        public boolean matches(String groupId, String artifactId) {
            return groupId.equals(this.groupId) && artifactId.equals(this.artifactId);
        }

        public String toString() {
            return "maven(" + this.groupId + ":" + this.artifactId + ") " + getAction();
        }
    }

    public static class PackageMatcher extends Matcher {
        public final String pkg;

        public PackageMatcher(String pkg) {
            this.pkg = pkg;
        }

        @Override
        boolean matches(JarFile file) {
            String pkgPath = this.pkg.replaceAll("\\.", "/");

            return file.getEntry(pkgPath) != null;
        }

        public String toString() {
            return "package(" + this.pkg + ") " + getAction();
        }
    }

    public interface Action {

    }

    public class RemoveAction implements Action {

    }

    public class ReplaceAction implements Action {

        public final String moduleName;

        public final String moduleSlot;

        public ReplaceAction(String moduleName, String moduleSlot) {
            this.moduleName = moduleName;
            this.moduleSlot = moduleSlot;
        }
    }

}
