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
package org.wildfly.swarm.logging;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class LoggingArquillianDefaultTest {

    @Deployment
    public static Archive createDeployment() {
        JARArchive deployment = ShrinkWrap.create(JARArchive.class);
        deployment.add(EmptyAsset.INSTANCE, "nothing");
        return deployment;
    }

    @Test
    public void testNothing() {
        Logger gouda = Logger.getLogger( "cheese.gouda" );

        gouda.info( "gouda info" );
        gouda.debug( "gouda debug");

        assertFalse( gouda.isTraceEnabled() );
        assertFalse( gouda.isDebugEnabled() );
        assertTrue( gouda.isInfoEnabled() );

        Logger cheddar = Logger.getLogger( "cheese.cheddar" );

        cheddar.info( "cheddar info" );
        cheddar.debug( "cheddar debug" );

        assertFalse( cheddar.isTraceEnabled() );
        assertFalse( cheddar.isDebugEnabled() );
        assertTrue( cheddar.isInfoEnabled() );
    }

}
