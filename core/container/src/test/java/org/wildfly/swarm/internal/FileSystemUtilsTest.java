/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.internal;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by hbraun on 22.08.17.
 */
public class FileSystemUtilsTest {

    @Test
    public void testMavenArgsPresent() {
        String cmd = "clean -f pom-other.xml install";
        MavenArgsParser args = MavenArgsParser.parse(cmd);
        Optional<String> f_arg = args.get(MavenArgsParser.ARG.F);
        Assert.assertTrue(f_arg.isPresent());
        Assert.assertEquals("pom-other.xml", f_arg.get());
    }

    @Test
    public void testMavenArgsMissing() {
        String cmd = "clean install";
        MavenArgsParser args = MavenArgsParser.parse(cmd);
        Optional<String> f_arg = args.get(MavenArgsParser.ARG.F);
        Assert.assertFalse(f_arg.isPresent());
    }

    @Test
    public void testAlternativeFlags() {
        String cmd = "clean --file pom-other.xml install";
        MavenArgsParser args = MavenArgsParser.parse(cmd);
        Optional<String> f_arg = args.get(MavenArgsParser.ARG.F);
        Assert.assertTrue(f_arg.isPresent());
        Assert.assertEquals("pom-other.xml", f_arg.get());
    }

    @Test
    public void testLackOfWhitespaces() {
        String cmd = "clean -fpom-other.xml install";
        MavenArgsParser args = MavenArgsParser.parse(cmd);
        Optional<String> f_arg = args.get(MavenArgsParser.ARG.F);
        Assert.assertTrue(f_arg.isPresent());
        Assert.assertEquals("pom-other.xml", f_arg.get());
    }

    @Test
    public void testMultiArgCommand() {
        String cmd = "clean -fpom-other.xml -PmyProfile install";
        MavenArgsParser args = MavenArgsParser.parse(cmd);

        Optional<String> f_arg = args.get(MavenArgsParser.ARG.F);
        Assert.assertTrue(f_arg.isPresent());
        Assert.assertEquals("pom-other.xml", f_arg.get());

        Optional<String> p_arg = args.get(MavenArgsParser.ARG.P);
        Assert.assertTrue(p_arg.isPresent());
        Assert.assertEquals("myProfile", p_arg.get());
    }

    @Test
    public void faeMavenCommandLine() {
        String cmd = "clean install -fae";
        MavenArgsParser args = MavenArgsParser.parse(cmd);
        Optional<String> f_arg = args.get(MavenArgsParser.ARG.F);
        Assert.assertFalse("Should not have found a value for -f command line argument", f_arg.isPresent());
    }
}
