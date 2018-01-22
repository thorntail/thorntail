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

package org.wildfly.swarm.microprofile.openapi.runtime;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eric.wittmann@gmail.com
 */
public class OpenApiAnnotationScannerTest {

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.OpenApiAnnotationScanner#makePath(java.lang.String[])}.
     */
    @Test
    public void testMakePath() {
        String path = OpenApiAnnotationScanner.makePath("", "", "");
        Assert.assertEquals("/", path);

        path = OpenApiAnnotationScanner.makePath("/", "/");
        Assert.assertEquals("/", path);

        path = OpenApiAnnotationScanner.makePath("", "/bookings");
        Assert.assertEquals("/bookings", path);

        path = OpenApiAnnotationScanner.makePath("/api", "/bookings");
        Assert.assertEquals("/api/bookings", path);

        path = OpenApiAnnotationScanner.makePath("api", "bookings");
        Assert.assertEquals("/api/bookings", path);

        path = OpenApiAnnotationScanner.makePath("/", "/bookings", "{id}");
        Assert.assertEquals("/bookings/{id}", path);
    }

}
