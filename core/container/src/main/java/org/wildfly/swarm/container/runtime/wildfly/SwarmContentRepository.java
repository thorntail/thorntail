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
package org.wildfly.swarm.container.runtime.wildfly;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.as.repository.ContentReference;
import org.jboss.as.repository.ContentRepository;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

/**
 * A content-repository capable of providing a static bit of content.
 *
 * @author Bob McWhirter
 */
@ApplicationScoped
public class SwarmContentRepository implements ContentRepository, Service<ContentRepository> {


    /**
     * Install the service.
     */
    public static void addService(ServiceTarget serviceTarget, SwarmContentRepository repository) {
        serviceTarget.addService(SERVICE_NAME, repository)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();
    }

    @Override
    public byte[] addContent(InputStream stream) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] sha1Bytes;
            Path tmp = TempFileManager.INSTANCE.newTempFile("content", ".tmp").toPath();
            try (OutputStream fos = Files.newOutputStream(tmp); BufferedInputStream bis = new BufferedInputStream(stream)) {
                messageDigest.reset();
                try (DigestOutputStream dos = new DigestOutputStream(fos, messageDigest)) {
                    byte[] bytes = new byte[8192];
                    int read;
                    while ((read = bis.read(bytes)) > -1) {
                        dos.write(bytes, 0, read);
                    }
                    fos.flush();
                }
                sha1Bytes = messageDigest.digest();
            }
            String key = toKey(sha1Bytes);
            this.index.put(key, tmp.toUri());
            return sha1Bytes;
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    public byte[] addContent(Archive<?> archive) throws IOException, URISyntaxException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] sha1Bytes;
            messageDigest.reset();
            BufferedInputStream bis = new BufferedInputStream(archive.as(ZipExporter.class).exportAsInputStream());
            byte[] bytes = new byte[8192];
            int read;
            while ((read = bis.read(bytes)) > -1) {
                messageDigest.update(bytes, 0, read);
            }
            sha1Bytes = messageDigest.digest();
            String key = toKey(sha1Bytes);
            this.fs.addArchive(archive.getName(), archive);
            this.index.put(key, this.fsMount.getChild(archive.getName()).toURI());
            return sha1Bytes;
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void addContentReference(ContentReference contentReference) {
    }

    @Override
    public VirtualFile getContent(byte[] sha1Bytes) {
        String key = toKey(sha1Bytes);
        VirtualFile result = VFS.getChild(this.index.get(key));
        return result;
    }

    @Override
    public boolean hasContent(byte[] sha1Bytes) {
        String key = toKey(sha1Bytes);
        boolean result = this.index.containsKey(key);
        return result;
    }

    public static String toKey(byte[] hash) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < hash.length; ++i) {
            int bit = Math.abs(hash[i]);
            str.append(Integer.toHexString(bit));
        }
        return str.toString();
    }

    @Override
    public boolean syncContent(ContentReference contentReference) {
        return true;
    }

    @Override
    public void removeContent(ContentReference contentReference) {

    }

    public void removeAllContent() throws IOException {
        for (URI uri : this.index.values()) {
            VirtualFile file = VFS.getChild(uri);
            file.delete();
        }
    }

    @Override
    public Map<String, Set<String>> cleanObsoleteContent() {
        HashMap<String, Set<String>> result = new HashMap<>();
        result.put(ContentRepository.MARKED_CONTENT, Collections.emptySet());
        result.put(ContentRepository.DELETED_CONTENT, Collections.emptySet());
        return result;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.fsMount = VFS.getChild("wildfly-swarm-deployments");
        try {
            this.fsCloseable = VFS.mount(this.fsMount, this.fs);
        } catch (IOException e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext stopContext) {
        try {
            this.fsCloseable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ContentRepository getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    private Map<String, URI> index = new HashMap<>();

    private VirtualFile fsMount;


    @Inject
    private ShrinkWrapFileSystem fs;

    private Closeable fsCloseable;

}
