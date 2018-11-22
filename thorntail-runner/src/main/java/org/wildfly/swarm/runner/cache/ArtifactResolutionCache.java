/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.runner.cache;

import org.wildfly.swarm.tools.ArtifactSpec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wildfly.swarm.runner.cache.RunnerCacheConstants.CACHE_STORAGE_DIR;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 9/7/18
 */
public class ArtifactResolutionCache {
    private static final String RESOLUTION_CACHE_FILE = "resolution-cache";
    public static final Path CACHE_PATH = Paths.get(CACHE_STORAGE_DIR, RESOLUTION_CACHE_FILE);

    public static final ArtifactResolutionCache INSTANCE = new ArtifactResolutionCache();

    private Map<String, File> resolvedCache = new ConcurrentHashMap<>();
    private Set<String> resolutionFailures = Collections.newSetFromMap(new ConcurrentHashMap<>()); // transient

    private ArtifactResolutionCache() {
        long startTime = System.currentTimeMillis();
        System.out.println("Reading and verifying cache");
        Path cache = CACHE_PATH;
        if (!cache.toFile().exists()) {
            System.out.println("No preexisting artifact resolution cache found. The first execution may take some time.");
        } else {
            try (Stream<String> lines = Files.lines(cache)) {
                lines.forEach(this::addToCache);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error reading resolution cache file, caching will be disabled.");
            }
        }
        resolutionFailures.add("org.jboss.narayana.jta:cdi:jar:5.5.30.Final");
        System.out.printf("Cache initialization done in %d ms\n", System.currentTimeMillis() - startTime);
    }

    public void store() {
        System.out.println("Storing cache resolution results...");
        try {
            Files.deleteIfExists(CACHE_PATH);
            List<String> lines = resolvedCache.entrySet()
                    .stream()
                    .map(entry -> String.format("%s#%s", entry.getKey(), entry.getValue().getAbsolutePath()))
                    .collect(Collectors.toList());
            storeToFile(CACHE_PATH, lines);
        } catch (IOException e) {
            System.out.println("Failed to store artifact resolution. Next execution won't be able to use full caching capabilities");
            e.printStackTrace();
        }
    }

    private void addToCache(String cacheFileLine) {
        String[] split = cacheFileLine.split("#");
        try {
            String key = split[0];
            String path = split[1];

            File file = Paths.get(path).toFile();
            if (file.exists()) {
                resolvedCache.put(key, file);
            } else {
                System.out.printf("Omitting %s -> %s mapping from cache resolution. It points to a non-existent file\n", key, path);
            }
        } catch (Exception any) {
            System.out.printf("Omitting invalid cache line %s\n", cacheFileLine);
            any.printStackTrace();
        }
    }

    private void storeToFile(Path path, List<String> linesToStore) throws IOException {
        File cacheFile = path.toFile();

        File parent = cacheFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(cacheFile)) {
            for (String line : linesToStore) {
                writer.append(line).append("\n");
            }
        }
    }

    public File getCachedFile(ArtifactSpec spec) {
        String key = spec.mavenGav();
        return resolvedCache.get(key);
    }

    public void storeArtifactFile(ArtifactSpec spec, File maybeFile) {
        if (maybeFile != null) {
            resolvedCache.put(spec.mavenGav(), maybeFile);
        }
    }

    public void storeResolutionFailure(ArtifactSpec spec) {
        resolutionFailures.add(spec.mavenGav());
    }

    public boolean isKnownFailure(ArtifactSpec spec) {
        return resolutionFailures.contains(spec.mavenGav());
    }

}
