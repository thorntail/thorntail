/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.jboss.modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.jboss.modules.xml.MXParser;
import org.jboss.modules.xml.XmlPullParser;
import org.jboss.modules.xml.XmlPullParserException;
import org.wildfly.swarm.bootstrap.util.Layout;

import static org.jboss.modules.ModuleXmlParser.endOfDocument;
import static org.jboss.modules.ModuleXmlParser.unexpectedContent;
import static org.jboss.modules.xml.XmlPullParser.END_DOCUMENT;
import static org.jboss.modules.xml.XmlPullParser.END_TAG;
import static org.jboss.modules.xml.XmlPullParser.FEATURE_PROCESS_NAMESPACES;
import static org.jboss.modules.xml.XmlPullParser.START_TAG;

/**
 * Helper class to resolve a maven artifact
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:tcerar@redhat.com">Tomaz Cerar</a>
 * @version $Revision: 2 $
 */
public class MavenArtifactUtil {

    private static final Object settingLoaderMutex = new Object();

    private static final Object artifactLock = new Object();

    private static MavenSettings mavenSettings;

    public static MavenSettings getSettings() throws IOException {
        if (mavenSettings != null) {
            return mavenSettings;
        }
        synchronized (settingLoaderMutex) {
            MavenSettings settings = new MavenSettings();

            Path m2 = java.nio.file.Paths.get(System.getProperty("user.home"), ".m2");
            Path settingsPath = m2.resolve("settings.xml");

            if (Files.notExists(settingsPath)) {
                String mavenHome = System.getenv("M2_HOME");
                if (mavenHome != null) {
                    settingsPath = java.nio.file.Paths.get(mavenHome, "conf", "settings.xml");
                }
            }
            if (Files.exists(settingsPath)) {
                parseSettingsXml(settingsPath, settings);
            }
            Path localRepo = settings.getLocalRepository();
            if (localRepo == null || localRepo.toString().trim().equals("")) {
                Path repository = m2.resolve("repository");
                settings.setLocalRepository(repository);
            }

            String localRepositoryPath = System.getProperty("maven.repo.local");
            if (localRepositoryPath != null && ! localRepositoryPath.trim().equals( "" ) ) {
                settings.setLocalRepository(java.nio.file.Paths.get(localRepositoryPath));
            }
            settings.resolveActiveSettings();

            if ( ! settings.getRemoteRepositories().contains( "http://repository.jboss.org/nexus/content/groups/public/" ) ) {
                settings.getRemoteRepositories().add( "http://repository.jboss.org/nexus/content/groups/public/" );
            }

            mavenSettings = settings;
            return mavenSettings;
        }
    }

    private static MavenSettings parseSettingsXml(Path settings, MavenSettings mavenSettings) throws IOException {
        try {
            final MXParser reader = new MXParser();
            reader.setFeature(FEATURE_PROCESS_NAMESPACES, false);
            InputStream source = Files.newInputStream(settings, StandardOpenOption.READ);
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

    private static void parseSettings(final XmlPullParser reader, MavenSettings mavenSettings) throws XmlPullParserException, IOException {
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
                                mavenSettings.setLocalRepository(java.nio.file.Paths.get(localRepository));
                            }

                            String localRepositoryPath = System.getProperty("local.maven.repo.path");
                            if (localRepositoryPath != null) {
                                mavenSettings.setLocalRepository(java.nio.file.Paths.get(localRepositoryPath.split(File.pathSeparator)[0]));
                            }

                            localRepositoryPath = System.getProperty("maven.repo.local");
                            if (localRepositoryPath != null) {
                                mavenSettings.setLocalRepository(java.nio.file.Paths.get(localRepositoryPath));
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

    private static void parseProfile(final XmlPullParser reader, MavenSettings mavenSettings) throws XmlPullParserException, IOException {
        int eventType;
        MavenSettings.Profile profile = new MavenSettings.Profile();
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

    private static void parseRepository(final XmlPullParser reader, MavenSettings.Profile profile) throws XmlPullParserException, IOException {
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

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
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

    /**
     * First checks this class's ClassLoader for an embedded maven repository under {@code m2repo} and extracts
     * the artifact if found.
     * <p></p>
     * Then tries to find a maven jar artifact from the system property "local.maven.repo.path" This property is a list of
     * platform separated directory names.  If not specified, then it looks in ${user.home}/.m2/repository by default.
     * <p></p>
     * If it can't find it in local paths, then will try to download from a remote repository from the system property
     * "remote.maven.repo".  There is no default remote repository.  It will download both the pom and jar and put it
     * into the first directory listed in "local.maven.repo.path" (or the default dir).  This directory will be created
     * if it doesn't exist.
     * <p></p>
     * Finally, if you do not want a message to console, then set the system property "maven.download.message" to
     * "false"
     *
     * @param qualifier group:artifact:version[:classifier]
     * @return absolute path to artifact, null if none exists
     * @throws IOException Unable to download artifact
     */
    public static File resolveJarArtifact(String qualifier) throws IOException {
        return resolveArtifact(qualifier, "jar");
    }

    public static File resolveArtifact(String qualifier, String packaging) throws IOException {
        if (qualifier.startsWith("${") && qualifier.endsWith("}")) {
            qualifier = qualifier.substring(2, qualifier.length() - 1);
        }
        String[] split = qualifier.split(":");
        if (split.length < 3) {
            throw new IllegalArgumentException("Illegal artifact " + qualifier);
        }
        String groupId = split[0];
        String artifactId = split[1];
        String version = split[2];
        String classifier = "";
        if (split.length >= 4) {
            classifier = "-" + split[3];
        }

        final MavenSettings settings = getSettings();
        final Path localRepository = settings.getLocalRepository();

        // serialize artifact lookup because we want to prevent parallel download
        synchronized (artifactLock) {
            String artifactRelativePath = "m2repo/" + relativeArtifactPath('/', groupId, artifactId, version);
            String jarPath = artifactRelativePath + classifier + "." + packaging;

            InputStream stream = MavenArtifactUtil.class.getClassLoader().getResourceAsStream(jarPath);
            System.err.println( "jar: " + jarPath );
            if (stream != null) {
                return copyTempJar(artifactId + "-" + version, stream, packaging);
            }

            /*
            try {
                if (Layout.getInstance().isUberJar() ) {
                    System.err.println( "UBERJAR, not searching further for : " + qualifier + ":" + packaging );
                    return null;
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            */

            artifactRelativePath = relativeArtifactPath(groupId, artifactId, version);
            jarPath = artifactRelativePath + classifier + "." + packaging;

            Path fp = java.nio.file.Paths.get(localRepository.toString(), jarPath);
            System.err.println( "repo-local:" + fp );
            if (Files.exists(fp)) {
                return fp.toFile();
            }

            List<String> remoteRepos = mavenSettings.getRemoteRepositories();
            if (remoteRepos.isEmpty()) {
                return null;
            }

            final File jarFile = new File(localRepository.toFile(), jarPath);
            final File pomFile = new File(localRepository.toFile(), artifactRelativePath + ".pom");
            for (String remoteRepository : remoteRepos) {
                try {
                    String remotePomPath = remoteRepository + artifactRelativePath + ".pom";
                    String remoteJarPath = remoteRepository + artifactRelativePath + classifier + "." + packaging;
                    downloadFile(qualifier + ":pom", remotePomPath, pomFile);
                    downloadFile(qualifier + ":" + packaging, remoteJarPath, jarFile);
                    if (jarFile.exists()) { //download successful
                        return jarFile;
                    }
                } catch (IOException e) {
                    Module.log.trace(e, "Could not download '%s' from '%s' repository", artifactRelativePath, remoteRepository);
                    //
                }
            }
            //could not find it in remote
            Module.log.trace("Could not find in any remote repository");
            return null;
        }
    }

    public static String relativeArtifactPath(String groupId, String artifactId, String version) {
        return relativeArtifactPath(File.separatorChar, groupId, artifactId, version);
    }

    public static String relativeArtifactHttpPath(String groupId, String artifactId, String version) {
        return relativeArtifactPath('/', groupId, artifactId, version);
    }

    private static String relativeArtifactPath(char separator, String groupId, String artifactId, String version) {
        StringBuilder builder = new StringBuilder(groupId.replace('.', separator));
        builder.append(separator).append(artifactId).append(separator).append(version).append(separator).append(artifactId).append('-').append(version);
        return builder.toString();
    }

    public static File copyTempJar(String artifact, InputStream in, String packaging) throws IOException {
        try {
            File temp = File.createTempFile(artifact, "." + packaging);
            temp.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(temp)) {
                byte[] buf = new byte[1024];
                int len = -1;

                while ((len = in.read(buf)) >= 0) {
                    out.write(buf, 0, len);
                }
            }
            return temp;
        } finally {
            in.close();
        }
    }

    public static void downloadFile(String artifact, String src, File dest) throws IOException {
        final URL url = new URL(src);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        boolean message = Boolean.getBoolean("maven.download.message");

        InputStream bis = connection.getInputStream();
        try {
            dest.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(dest);
            try {
                if (message) {
                    System.out.println("Downloading " + artifact);
                }
                Files.copy(bis, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } finally {
                StreamUtil.safeClose(fos);
            }
        } finally {
            StreamUtil.safeClose(bis);
        }
    }
}

