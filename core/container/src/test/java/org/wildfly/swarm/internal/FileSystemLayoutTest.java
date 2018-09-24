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

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

/**
 * Basic tests for the {@link FileSystemLayout} class.
 */
public class FileSystemLayoutTest {

    /**
     * Verify the default behavior of the FileSystemLayout w.r.t current build system.
     */
    @Test
    public void verifyDefaultBehavior() {
        withProperty(null, layout -> {
            // By default, we should be getting the MavenFileSystemLayout
            Assert.assertEquals("Unexpected file system layout. Did the default layout change?",
                                MavenFileSystemLayout.class, layout.getClass());
        });
    }

    /**
     * Verify that we can specify a custom "valid" FileSystemLayout.
     */
    @Test
    public void verifySystemProperty() {
        withProperty("org.wildfly.swarm.internal.GradleFileSystemLayout", layout -> {
            Assert.assertEquals("Unexpected file system layout.", GradleFileSystemLayout.class, layout.getClass());
        });
    }

    /**
     * Verify that we get the default layout when using an invalid system property.
     */
    @Test
    public void verifyInvalidSystemProperty() {
        withProperty("   ", layout -> {
            // By default, we should be getting the MavenFileSystemLayout
            Assert.assertEquals("Unexpected file system layout. Did the default layout change?",
                                MavenFileSystemLayout.class, layout.getClass());
        });
    }

    /**
     * Verify that we can pass in a custom layout via the system properties.
     */
    @Test
    public void verifyCustomLayout() {
        withProperty("org.wildfly.swarm.internal.CustomFileSystemLayout", layout -> {
            Assert.assertEquals("Unexpected file system layout.", CustomFileSystemLayout.class, layout.getClass());
        });
    }

    /**
     * Verify that an instantiation exception in the custom layout is propagated all through.
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyCustomLayoutFailure() {
        withProperty("org.wildfly.swarm.internal.CustomFileSystemLayout$CustomInvalidFileSystemLayout", layout -> {
            Assert.fail("An exception is expected at this point.");
        });
    }

    /**
     * Convenience method that sets & clears out the property for the FileSystemLayout. This method is synchronized so that
     * we don't run in to race conditions if the tests are run in parallel.
     *
     * @param propertyValue the value of the implementation class.
     * @param consumer      a simple function that needs to be invoked on the FileSystemLayout that was generated.
     */
    private synchronized void withProperty(String propertyValue, Consumer<FileSystemLayout> consumer) {
        try {
            if (propertyValue != null) {
                System.setProperty(FileSystemLayout.CUSTOM_LAYOUT_CLASS, propertyValue);
            }
            FileSystemLayout layout = FileSystemLayout.create();
            consumer.accept(layout);
        } finally {
            System.clearProperty(FileSystemLayout.CUSTOM_LAYOUT_CLASS);
        }
    }
}
