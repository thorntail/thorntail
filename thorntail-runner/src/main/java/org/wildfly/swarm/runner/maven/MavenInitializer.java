/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.runner.maven;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import java.io.File;
import java.nio.file.Paths;

/**
 *
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 9/11/18
 */
public class MavenInitializer {

    public static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        return locator.getService(RepositorySystem.class);
    }

    public static RepositorySystemSession newSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(localRepoLocation());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        return session;
    }

    public static File localRepoLocation() {
        File result = null;
        String userRepository = System.getProperty("thorntail.runner.local-repository");
        if (userRepository != null) {
            result = new File(userRepository);
            if (!result.isDirectory()) {
                System.err.println("The defined local repository: " + userRepository + " does not exist or is not a directory");
            }
        }
        if (result == null) {
            String userHome = System.getProperty("user.home");
            result = Paths.get(userHome, ".m2", "repository").toFile();

            if (!result.exists()) {
                result.mkdirs();
            }
            // TODO maybe use .thorntail-runner-cache if it does not exist?
            // TODO check if exists, ask user to create or point to a different location?
        }
        return result;
    }

    public static RemoteRepository buildRemoteRepository(final RepositorySystemSession session,
                                                         final String id, final String url, final String username, final String password) {
        RemoteRepository.Builder builder = new RemoteRepository.Builder(id, "default", url);
        if (username != null) {
            builder.setAuthentication(new AuthenticationBuilder()
                    .addUsername(username)
                    .addPassword(password).build());
        }

        RemoteRepository repository = builder.build();

        final RemoteRepository mirror = session.getMirrorSelector().getMirror(repository);

        if (mirror != null) {
            final org.eclipse.aether.repository.Authentication mirrorAuth = session.getAuthenticationSelector()
                    .getAuthentication(mirror);
            RemoteRepository.Builder mirrorBuilder = new RemoteRepository.Builder(mirror)
                    .setId(repository.getId());
            if (mirrorAuth != null) {
                mirrorBuilder.setAuthentication(mirrorAuth);
            }
            repository = mirrorBuilder.build();
        }

        Proxy proxy = session.getProxySelector().getProxy(repository);

        if (proxy != null) {
            repository = new RemoteRepository.Builder(repository).setProxy(proxy).build();
        }

        return repository;
    }

    private MavenInitializer() {
    }
}
