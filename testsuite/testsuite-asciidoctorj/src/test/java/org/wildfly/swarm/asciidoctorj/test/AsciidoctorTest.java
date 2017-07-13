/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.asciidoctorj.test;

import java.util.Collections;

import org.asciidoctor.Asciidoctor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class AsciidoctorTest {

    private Asciidoctor asciidoctor;

    @Before
    public void setUp() {
        asciidoctor = Asciidoctor.Factory.create();
    }

    @Test
    public void testCanCreateAsciidoctor() {
        Assert.assertNotNull(asciidoctor);
    }

    @Test
    public void testAsciidoctorRender() {
        String content = asciidoctor.render("Hello World", Collections.<String, Object>emptyMap());
        Assert.assertEquals("<div class=\"paragraph\">\n<p>Hello World</p>\n</div>", content);
    }

    @After
    public void tearDown() {
        asciidoctor.shutdown();
    }
}
