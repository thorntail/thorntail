/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.undertow;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.undertow.descriptors.Servlet;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class WebXmlContainerTest {

    @Test
    public void testContextParamOnClassloaderWebXML() throws Exception {
        WARArchive archive = ShrinkWrap.create(WARArchive.class);
        archive.setWebXML("web.xml");

        archive.addContextParam("myParam", "myValue");
    }

    @Test
    public void testContextParamOnFileWebXML() throws Exception {
        WARArchive archive = ShrinkWrap.create(WARArchive.class);
        archive.setWebXML(new File("src/test/resources/web.xml"));

        archive.addContextParam("myParam", "myValue");
    }

    @Test
    public void testReadingExistingWebXML() throws Exception {
        WARArchive archive = ShrinkWrap.create(WARArchive.class);
        archive.setWebXML(new File("src/test/resources/web.xml"));

        String paramValue = archive.getContextParamValue("paramName");
        assertThat(paramValue).isNotNull();
        assertThat(paramValue).isEqualTo("paramValue");

        Servlet servlet = archive.servlet("com.app.MyServletClass");
        assertThat(servlet).isNotNull();
        assertThat(servlet.servletName()).isEqualTo("MyServlet");
        assertThat(servlet.urlPatterns()).isNotNull();
        assertThat(servlet.urlPatterns().size()).isEqualTo(1);
        assertThat(servlet.urlPatterns().get(0)).isEqualTo("/me");
    }
}
