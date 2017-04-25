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
package org.wildfly.swarm.logging.test;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
@DefaultDeployment(main = MainWithProperties.class)
public class ArqLoggingLevelsTest {

    @Test
    public void testCustomCategory() {
        Logger logger = Logger.getLogger("custom.category");

        logger.info("gouda info");
        logger.debug("gouda debug");
        logger.trace("gouda trace");

        assertFalse(logger.isTraceEnabled());
        assertTrue(logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
    }

    @Test
    public void testCustomCategoryChildren() {
        Logger logger = Logger.getLogger("custom.category.children.Something");

        logger.info("gouda info");
        logger.debug("gouda debug");
        logger.trace("gouda trace");

        assertFalse(logger.isTraceEnabled());
        assertTrue(logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
    }

    @Test
    public void testRoot() {
        Logger logger = Logger.getLogger("");

        logger.info("gouda info");
        logger.debug("gouda debug");
        logger.trace("gouda trace");

        assertTrue(logger.isTraceEnabled());
        assertTrue(logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
    }

}
