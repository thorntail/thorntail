/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wildfly.swarm.internal;

import java.nio.file.Path;

/**
 * Dummy implementation of {@link FileSystemLayout} which is to be used only in the appropriate test case.
 */
public class CustomFileSystemLayout extends FileSystemLayout {

    public CustomFileSystemLayout(String path) {
        super(path);
    }

    @Override
    public String determinePackagingType() {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    @Override
    public Path resolveBuildClassesDir() {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    @Override
    public Path resolveBuildResourcesDir() {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    @Override
    public Path resolveSrcWebAppDir() {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    @Override
    public Path getRootPath() {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Simulate instantiation exception.
     */
    public static class CustomInvalidFileSystemLayout extends CustomFileSystemLayout {

        public CustomInvalidFileSystemLayout(String path) {
            super(path);
            throw new IllegalArgumentException("Test case needs an exception to be raised.");
        }
    }
}
