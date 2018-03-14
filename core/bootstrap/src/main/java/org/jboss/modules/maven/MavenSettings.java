/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.jboss.modules.maven;

import static org.jboss.modules.maven.MavenArtifactUtil.doIo;
import static org.jboss.modules.xml.ModuleXmlParser.endOfDocument;
import static org.jboss.modules.xml.ModuleXmlParser.unexpectedContent;
import static org.jboss.modules.xml.XmlPullParser.END_DOCUMENT;
import static org.jboss.modules.xml.XmlPullParser.END_TAG;
import static org.jboss.modules.xml.XmlPullParser.FEATURE_PROCESS_NAMESPACES;
import static org.jboss.modules.xml.XmlPullParser.START_TAG;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.modules.xml.MXParser;
import org.jboss.modules.xml.XmlPullParser;
import org.jboss.modules.xml.XmlPullParserException;

/**
 * @author Tomaz Cerar (c) 2014 Red Hat Inc.
 */
final class MavenSettings {
    private static final Object settingLoaderMutex = new Object();

    private static volatile MavenSettings mavenSettings;

    private Path localRepository = null;

    private final List<String> remoteRepositories = new LinkedList<>();

    private final Map<String, Profile> profiles = new HashMap<>();

    private final List<String> activeProfileNames = new LinkedList<>();

    MavenSettings() {
        configureDefaults();
    }

    static MavenSettings getSettings() throws IOException {
        if (mavenSettings != null) {
            return mavenSettings;
        }
        synchronized (settingLoaderMutex) {
            if (mavenSettings != null) {
                return mavenSettings;
            }
            return mavenSettings = doIo(() -> {
                MavenSettings settings = new MavenSettings();

                Path m2 = Paths.get(System.getProperty("user.home"), ".m2");
                Path settingsPath = m2.resolve("settings.xml");

                if (Files.notExists(settingsPath)) {
                    String mavenHome = System.getenv("M2_HOME");
                    if (mavenHome != null) {
                        settingsPath = Paths.get(mavenHome, "conf", "settings.xml");
                    }
                }
                if (Files.exists(settingsPath)) {
                    parseSettingsXml(settingsPath, settings);
                }
                if (settings.getLocalRepository() == null) {
                    Path repository = m2.resolve("repository");
                    settings.setLocalRepository(repository);
                }
                settings.resolveActiveSettings();
                return settings;
            });
        }
    }

    static MavenSettings parseSettingsXml(Path settings, MavenSettings mavenSettings) throws IOException {
        final MXParser reader = new MXParser();

        try (InputStream source = Files.newInputStream(settings, StandardOpenOption.READ)){
            reader.setFeature(FEATURE_PROCESS_NAMESPACES, false);
            reader.setInput(source, null);
            int eventType;
            while ((eventType = reader.next()) != END_DOCUMENT) {
                switch (eventType) {
                    case START_TAG: {
                        switch (reader.getName()) {
                            case "settings": {
                                parseSettings(reader, mavenSettings);
                                break;
                            }
                        }
                    }
                    default: {
                        break;
                    }
                }
            }
            return mavenSettings;
        } catch (XmlPullParserException e) {
            throw new IOException("Could not parse maven settings.xml");
        }

    }

    static void parseSettings(final XmlPullParser reader, MavenSettings mavenSettings) throws XmlPullParserException, IOException {
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            switch (eventType) {
                case END_TAG: {
                    return;
                }
                case START_TAG: {

                    switch (reader.getName()) {
                        case "localRepository": {
                            String localRepository = reader.nextText();
                            if (localRepository != null && !localRepository.trim().isEmpty()) {
                                mavenSettings.setLocalRepository(Paths.get(interpolateVariables(localRepository)));
                            }
                            break;
                        }
                        case "profiles": {
                            while ((eventType = reader.nextTag()) != END_DOCUMENT) {
                                if (eventType == START_TAG) {
                                    switch (reader.getName()) {
                                        case "profile": {
                                            parseProfile(reader, mavenSettings);
                                            break;
                                        }
                                    }
                                } else {
                                    break;
                                }
                            }
                            break;
                        }
                        case "activeProfiles": {
                            while ((eventType = reader.nextTag()) != END_DOCUMENT) {
                                if (eventType == START_TAG) {
                                    switch (reader.getName()) {
                                        case "activeProfile": {
                                            mavenSettings.addActiveProfile(reader.nextText());
                                            break;
                                        }
                                    }
                                } else {
                                    break;
                                }

                            }
                            break;
                        }
                        default: {
                            skip(reader);

                        }
                    }
                    break;
                }
                default: {
                    throw unexpectedContent(reader);
                }
            }
        }
        throw endOfDocument(reader);
    }

    static void parseProfile(final XmlPullParser reader, MavenSettings mavenSettings) throws XmlPullParserException, IOException {
        int eventType;
        Profile profile = new Profile();
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            if (eventType == START_TAG) {
                switch (reader.getName()) {
                    case "id": {
                        profile.setId(reader.nextText());
                        break;
                    }
                    case "repositories": {
                        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
                            if (eventType == START_TAG) {
                                switch (reader.getName()) {
                                    case "repository": {
                                        parseRepository(reader, profile);
                                        break;
                                    }
                                }
                            } else {
                                break;
                            }

                        }
                        break;
                    }
                    default: {
                        skip(reader);
                    }
                }
            } else {
                break;
            }
        }
        mavenSettings.addProfile(profile);
    }

    static void parseRepository(final XmlPullParser reader, Profile profile) throws XmlPullParserException, IOException {
        int eventType;
        while ((eventType = reader.nextTag()) != END_DOCUMENT) {
            if (eventType == START_TAG) {
                switch (reader.getName()) {
                    case "url": {
                        profile.addRepository(reader.nextText());
                        break;
                    }
                    default: {
                        skip(reader);
                    }
                }
            } else {
                break;
            }

        }
    }

    static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    void configureDefaults() {
        //always add maven central
        remoteRepositories.add("https://repo1.maven.org/maven2/");
        String localRepositoryPath = System.getProperty("local.maven.repo.path");
        if (localRepositoryPath != null && !localRepositoryPath.trim().isEmpty()) {
            System.out.println("Please use 'maven.repo.local' instead of 'local.maven.repo.path'");
            localRepository = java.nio.file.Paths.get(localRepositoryPath.split(File.pathSeparator)[0]);
        }

        localRepositoryPath = System.getProperty("maven.repo.local");
        if (localRepositoryPath != null && !localRepositoryPath.trim().isEmpty()) {
            localRepository = java.nio.file.Paths.get(localRepositoryPath);
        }
        String remoteRepository = System.getProperty("remote.maven.repo");
        if (remoteRepository != null) {
            for (String repo : remoteRepository.split(",")) {
                if (!repo.endsWith("/")) {
                    repo += "/";
                }
                remoteRepositories.add(repo);
            }
        }
    }

    public void setLocalRepository(Path localRepository) {
        this.localRepository = localRepository;
    }

    public Path getLocalRepository() {
        return localRepository;
    }

    public List<String> getRemoteRepositories() {
        return remoteRepositories;
    }

    public void addProfile(Profile profile) {
        this.profiles.put(profile.getId(), profile);
    }

    public void addActiveProfile(String profileName) {
        activeProfileNames.add(profileName);
    }

    void resolveActiveSettings() {
        for (String name : activeProfileNames) {
            Profile p = profiles.get(name);
            if (p != null) {
                remoteRepositories.addAll(p.getRepositories());
            }
        }
    }

    static String interpolateVariables(String in) {
        StringBuilder out = new StringBuilder();

        int cur = 0;
        int startLoc = -1;

        while ((startLoc = in.indexOf("${", cur)) >= 0) {
            out.append(in.substring(cur, startLoc));
            int endLoc = in.indexOf("}", startLoc);
            if (endLoc > 0) {
                String name = in.substring(startLoc + 2, endLoc);
                String value = null;
                if (name.startsWith("env.")) {
                    value = System.getenv(name.substring(4));
                } else {
                    value = System.getProperty(name);
                }
                if (value == null) {
                    value = "";
                }
                out.append(value);
            } else {
                out.append(in.substring(startLoc));
                cur = in.length();
                break;
            }
            cur = endLoc + 1;
        }

        out.append(in.substring(cur));

        return out.toString();
    }


    static final class Profile {
        private String id;

        final List<String> repositories = new LinkedList<>();

        Profile() {

        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void addRepository(String url) {
            if (!url.endsWith("/")) {
                url += "/";
            }
            repositories.add(url);
        }

        public List<String> getRepositories() {
            return repositories;
        }
    }
}

