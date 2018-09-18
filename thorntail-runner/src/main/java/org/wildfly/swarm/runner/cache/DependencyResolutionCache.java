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

import org.eclipse.aether.util.ChecksumUtils;
import org.wildfly.swarm.tools.ArtifactSpec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.wildfly.swarm.runner.cache.RunnerCacheConstants.CACHE_STORAGE_DIR;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 9/7/18
 */
public class DependencyResolutionCache {

    public static final DependencyResolutionCache INSTANCE = new DependencyResolutionCache();


    private DependencyResolutionCache() {
    }


    public List<ArtifactSpec> getCachedDependencies(Collection<ArtifactSpec> specs, boolean defaultExcludes) {
        Path cacheFile = getCacheFile(specs, defaultExcludes);

        return readDependencies(cacheFile);
    }

    public void storeCachedDependencies(Collection<ArtifactSpec> specs, List<ArtifactSpec> dependencySpecs, boolean defaultExcludes) {
        Path cachePath = getCacheFile(specs, defaultExcludes);

        if (cachePath == null) {
            return;
        }

        List<String> linesToStore = dependencySpecs.stream().map(ArtifactSpec::mscGav).collect(Collectors.toList());

        try {
            storeToFile(cachePath, linesToStore);
        } catch (IOException e) {
            System.err.println("Failed to store cached dependencies to " + cachePath.toAbsolutePath().toString());
            e.printStackTrace();
            return;
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

    private Path getCacheFile(Collection<ArtifactSpec> specs, boolean defaultExcludes) {
        String key = null;
        try {
            key = getCacheKey(specs);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No SHA-1 digest algorithm found, caching is disabled");
            return null;
        }

        return Paths.get(CACHE_STORAGE_DIR, key + (defaultExcludes ? "-with-excludes" : ""));
    }


    private List<ArtifactSpec> readDependencies(Path cacheFile) {
        if (cacheFile == null) {
            return null;
        }
        try {
            return Files.lines(cacheFile)
                    .map(ArtifactSpec::fromMscGav)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }

    private String getCacheKey(Collection<ArtifactSpec> specs) throws NoSuchAlgorithmException {
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
        specs.stream()
                .map(ArtifactSpec::jarName)
                .sorted()
                .forEach(
                        jarName -> sha1Digest.update(jarName.getBytes())
                );

        byte[] resultBytes = sha1Digest.digest();

        return ChecksumUtils.toHexString(resultBytes);
    }
}
