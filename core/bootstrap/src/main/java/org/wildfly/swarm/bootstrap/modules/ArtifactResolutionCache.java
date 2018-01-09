package org.wildfly.swarm.bootstrap.modules;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ArtifactResolutionCache {

    private ArtifactResolutionCache() {

    }

    public static Map<String,File> CACHED_FILES = new HashMap<>();
}
