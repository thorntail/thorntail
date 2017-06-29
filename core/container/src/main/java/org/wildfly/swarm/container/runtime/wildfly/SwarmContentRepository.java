package org.wildfly.swarm.container.runtime.wildfly;

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import org.jboss.as.repository.ContentReference;
import org.jboss.as.repository.ContentRepository;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

/**
 * A content-repository capable of providing a static bit of content.
 *
 * @author Bob McWhirter
 * @see org.jboss.as.selfcontained.ContentProvider
 */
@ApplicationScoped
public class SwarmContentRepository implements ContentRepository, Service<ContentRepository> {

    private Map<String, Path> index = new HashMap<>();

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
            Path tmp = File.createTempFile("content", ".tmp").toPath();
            try (OutputStream fos = Files.newOutputStream(tmp)) {
                messageDigest.reset();
                DigestOutputStream dos = new DigestOutputStream(fos, messageDigest);
                BufferedInputStream bis = new BufferedInputStream(stream);
                byte[] bytes = new byte[8192];
                int read;
                while ((read = bis.read(bytes)) > -1) {
                    dos.write(bytes, 0, read);
                }
                fos.flush();
                sha1Bytes = messageDigest.digest();
            }
            String key = toKey(sha1Bytes);
            this.index.put(key, tmp);
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
        VirtualFile result = VFS.getChild(this.index.get(key).toUri());
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
        IOException exception = null;
        for (Path path: this.index.values()) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                exception = e;
            }
        }

        if (exception != null) {
            throw exception;
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
    }

    @Override
    public void stop(StopContext stopContext) {
    }

    @Override
    public ContentRepository getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

}
