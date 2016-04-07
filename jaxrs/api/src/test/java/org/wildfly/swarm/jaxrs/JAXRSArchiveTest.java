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
package org.wildfly.swarm.jaxrs;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class JAXRSArchiveTest {

    public static final String PATH = "WEB-INF/classes/org/wildfly/swarm/generated/WildFlySwarmDefaultJAXRSApplication.class";

    @Test
    public void testApplicationPathAnnotation_None() {
        JAXRSArchive archive = ShrinkWrap.create( JAXRSArchive.class );

        Node generated = archive.get(PATH);
        Asset asset = generated.getAsset();

        assertThat( generated ).isNotNull();
        assertThat( asset ).isNotNull();
    }

    @Test
    public void testApplicationPathAnnotation_DirectlyInArchive() {
        JAXRSArchive archive = ShrinkWrap.create( JAXRSArchive.class );

        archive.addClass( MySampleApplication.class );

        Node generated = archive.get(PATH);
        assertThat( generated ).isNull();
    }

    @Test
    public void testApplicationPathAnnotation_InWebInfLibArchive() {

        JAXRSArchive archive = ShrinkWrap.create( JAXRSArchive.class );
        JavaArchive subArchive = ShrinkWrap.create(JavaArchive.class, "mysubarchive.jar");

        subArchive.addClass( MySampleApplication.class );
        archive.addAsLibrary( subArchive );

        Node generated = archive.get(PATH);
        assertThat( generated ).isNull();
    }

}
