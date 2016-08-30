package org.wildfly.swarm.bootstrap.env;

import java.util.Comparator;

/** Comparator to sort Manifests based on dependency tree, complexity, and alphabetically.
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

        if ( right.getDependencies().contains(gav(left) ) ) {
            return -1;
        }

        // simpler sort to the left
        if ( left.getDependencies().size() < right.getDependencies().size() ) {
            return -1;
        }

        if ( right.getDependencies().size() > left.getDependencies().size() ) {
            return 1;
        }

        // alphabetically
        return left.getArtifactId().compareTo( right.getArtifactId() );
    }

    protected String gav(FractionManifest manifest) {
        return manifest.getGroupId() +":" + manifest.getArtifactId() + ":jar:" + manifest.getVersion();
    }
}
