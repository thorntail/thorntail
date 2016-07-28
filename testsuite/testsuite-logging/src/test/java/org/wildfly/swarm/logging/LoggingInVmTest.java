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

import java.util.logging.LogManager;

import org.jboss.logging.Logger;
import org.junit.Test;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.container.Container;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Bob McWhirter
 */
public class LoggingInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction(
                LoggingFraction.createDebugLoggingFraction()
                        .logger("cheese.gouda", l -> {
                            l.level(Level.FINEST);
                        })
                        .logger("cheese.cheddar", l -> {
                            l.level(Level.OFF);
                        })
        );

        System.err.println( "Starting the container" );

        container.start();

        System.err.println( "LogManager is : " + LogManager.getLogManager().getClass() );

        System.err.println( "Executing tests" );

        Logger gouda = Logger.getLogger( "cheese.gouda" );

        gouda.info( "gouda info" );
        gouda.debug( "gouda debug");

        assertTrue( gouda.isTraceEnabled() );
        assertTrue( gouda.isDebugEnabled() );
        assertTrue( gouda.isInfoEnabled() );

        Logger cheddar = Logger.getLogger( "cheese.cheddar" );

        cheddar.info( "cheddar info" );
        cheddar.debug( "cheddar debug" );

        assertFalse( cheddar.isTraceEnabled() );
        assertFalse( cheddar.isDebugEnabled() );
        assertFalse( cheddar.isInfoEnabled() );

        container.stop();
    }
}
