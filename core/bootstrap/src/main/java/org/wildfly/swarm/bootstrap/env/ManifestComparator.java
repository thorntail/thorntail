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
package org.wildfly.swarm.bootstrap.env;

import java.util.Comparator;

/**
 * Comparator to sort Manifests based on dependency tree, complexity, and alphabetically.
 *
 * @author Bob McWhirter
 */
public class ManifestComparator implements Comparator<FractionManifest> {

    @Override
    public int compare(FractionManifest left, FractionManifest right) {

        // dependents sort to the right
        if (left.getDependencies().contains(gav(right))) {
            return 1;
        }

        if (right.getDependencies().contains(gav(left))) {
            return -1;
        }

        // simpler sort to the left
        if (left.getDependencies().size() < right.getDependencies().size()) {
            return -1;
        }

        if (right.getDependencies().size() > left.getDependencies().size()) {
            return 1;
        }

        // alphabetically
        return left.getArtifactId().compareTo(right.getArtifactId());
    }

    protected String gav(FractionManifest manifest) {
        return manifest.getGroupId() + ":" + manifest.getArtifactId() + ":jar:" + manifest.getVersion();
    }
}
