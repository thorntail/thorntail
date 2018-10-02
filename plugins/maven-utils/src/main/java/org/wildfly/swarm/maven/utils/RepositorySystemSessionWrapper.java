package org.wildfly.swarm.maven.utils;

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

import java.util.Map;

import org.eclipse.aether.RepositoryCache;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.SessionData;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.collection.VersionFilter;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.AuthenticationSelector;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.MirrorSelector;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.resolution.ArtifactDescriptorPolicy;
import org.eclipse.aether.resolution.ResolutionErrorPolicy;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaScopeDeriver;
import org.eclipse.aether.util.graph.transformer.JavaScopeSelector;
import org.eclipse.aether.util.graph.transformer.NearestVersionSelector;
import org.eclipse.aether.util.graph.transformer.SimpleOptionalitySelector;
import org.wildfly.swarm.fractions.FractionDescriptor;

/**
 * @author Ken Finnigan
 */
public final class RepositorySystemSessionWrapper implements RepositorySystemSession {

    public RepositorySystemSessionWrapper(RepositorySystemSession delegate, boolean excludeSwarm) {
        this.delegate = delegate;
        this.transformer = new ConflictResolver(new NearestVersionSelector(),
                new JavaScopeSelector(),
                new SimpleOptionalitySelector(),
                new JavaScopeDeriver()
        );
        this.excludeSwarm = excludeSwarm;
    }

    @Override
    public boolean isOffline() {
        return delegate.isOffline();
    }

    @Override
    public boolean isIgnoreArtifactDescriptorRepositories() {
        return delegate.isIgnoreArtifactDescriptorRepositories();
    }

    @Override
    public ResolutionErrorPolicy getResolutionErrorPolicy() {
        return delegate.getResolutionErrorPolicy();
    }

    @Override
    public ArtifactDescriptorPolicy getArtifactDescriptorPolicy() {
        return delegate.getArtifactDescriptorPolicy();
    }

    @Override
    public String getChecksumPolicy() {
        return delegate.getChecksumPolicy();
    }

    @Override
    public String getUpdatePolicy() {
        return delegate.getUpdatePolicy();
    }

    @Override
    public LocalRepository getLocalRepository() {
        return delegate.getLocalRepository();
    }

    @Override
    public LocalRepositoryManager getLocalRepositoryManager() {
        return delegate.getLocalRepositoryManager();
    }

    @Override
    public WorkspaceReader getWorkspaceReader() {
        return delegate.getWorkspaceReader();
    }

    @Override
    public RepositoryListener getRepositoryListener() {
        return delegate.getRepositoryListener();
    }

    @Override
    public TransferListener getTransferListener() {
        return delegate.getTransferListener();
    }

    @Override
    public Map<String, String> getSystemProperties() {
        return delegate.getSystemProperties();
    }

    @Override
    public Map<String, String> getUserProperties() {
        return delegate.getUserProperties();
    }

    @Override
    public Map<String, Object> getConfigProperties() {
        return delegate.getConfigProperties();
    }

    @Override
    public MirrorSelector getMirrorSelector() {
        return delegate.getMirrorSelector();
    }

    @Override
    public ProxySelector getProxySelector() {
        return delegate.getProxySelector();
    }

    @Override
    public AuthenticationSelector getAuthenticationSelector() {
        return delegate.getAuthenticationSelector();
    }

    @Override
    public ArtifactTypeRegistry getArtifactTypeRegistry() {
        return delegate.getArtifactTypeRegistry();
    }

    @Override
    public DependencyTraverser getDependencyTraverser() {
        return excludeSwarm ? new SwarmExcludedTraverser() : delegate.getDependencyTraverser();
    }

    @Override
    public DependencyManager getDependencyManager() {
        return delegate.getDependencyManager();
    }

    @Override
    public DependencySelector getDependencySelector() {
        /*if(defaultExcludes) {
            Set<Exclusion> exclusions = Collections.singleton(new Exclusion("org.wildfly.swarm", "*", "*", "*"));
            return new ExclusionDependencySelector(exclusions);
        } else {
            return delegate.getDependencySelector();
        }*/

        return delegate.getDependencySelector();

    }

    @Override
    public VersionFilter getVersionFilter() {
        return delegate.getVersionFilter();
    }

    @Override
    public DependencyGraphTransformer getDependencyGraphTransformer() {
        return transformer;
    }

    @Override
    public SessionData getData() {
        return delegate.getData();
    }

    @Override
    public RepositoryCache getCache() {
        return delegate.getCache();
    }

    private RepositorySystemSession delegate;

    private DependencyGraphTransformer transformer;

    private boolean excludeSwarm;

    public class SwarmExcludedTraverser implements DependencyTraverser {

        @Override
        public boolean traverseDependency(Dependency dependency) {
            return !FractionDescriptor.THORNTAIL_GROUP_ID.equals(dependency.getArtifact().getGroupId());
        }

        @Override
        public DependencyTraverser deriveChildTraverser(DependencyCollectionContext context) {
            return this;
        }
    }

}
