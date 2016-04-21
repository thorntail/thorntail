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
package org.wildfly.swarm.bootstrap.util;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmClasspathConfTest {

    @Test
    public void testComment() {
        WildFlySwarmClasspathConf conf = new WildFlySwarmClasspathConf();

        conf.process( "# ignore me" );

        assertThat( conf.getMatchers() ).isEmpty();
    }

    @Test
    public void testPackageMatcher_ReplaceSimpleModule() {
        WildFlySwarmClasspathConf conf = new WildFlySwarmClasspathConf();

        conf.process( "package(javax.servlet) replace(javax.servlet.api)" );

        assertThat( conf.getMatchers() ).hasSize(1);

        WildFlySwarmClasspathConf.Matcher matcher = conf.getMatchers().get(0);

        assertThat( matcher ).isNotNull();
        assertThat( matcher ).isInstanceOf(WildFlySwarmClasspathConf.PackageMatcher.class);

        assertThat( ((WildFlySwarmClasspathConf.PackageMatcher) matcher).pkg).isEqualTo( "javax.servlet" );

        assertThat( matcher.getAction() ).isInstanceOf(WildFlySwarmClasspathConf.ReplaceAction.class);

        WildFlySwarmClasspathConf.ReplaceAction action = (WildFlySwarmClasspathConf.ReplaceAction) matcher.getAction();

        assertThat( action.moduleName ).isEqualTo( "javax.servlet.api" );
        assertThat( action.moduleSlot ).isEqualTo( "main" );
    }

    @Test
    public void testPackageMatcher_ReplaceModuleWithSlot() {
        WildFlySwarmClasspathConf conf = new WildFlySwarmClasspathConf();

        conf.process( "package(javax.servlet) replace(javax.servlet.api:jbossorg)" );

        assertThat( conf.getMatchers() ).hasSize(1);

        WildFlySwarmClasspathConf.Matcher matcher = conf.getMatchers().get(0);

        assertThat( matcher ).isNotNull();
        assertThat( matcher ).isInstanceOf(WildFlySwarmClasspathConf.PackageMatcher.class);

        assertThat( ((WildFlySwarmClasspathConf.PackageMatcher) matcher).pkg).isEqualTo( "javax.servlet" );

        assertThat( matcher.getAction() ).isInstanceOf(WildFlySwarmClasspathConf.ReplaceAction.class);

        WildFlySwarmClasspathConf.ReplaceAction action = (WildFlySwarmClasspathConf.ReplaceAction) matcher.getAction();

        assertThat( action.moduleName ).isEqualTo( "javax.servlet.api" );
        assertThat( action.moduleSlot ).isEqualTo( "jbossorg" );
    }

    @Test
    public void testPackageMatcher_Remove() {
        WildFlySwarmClasspathConf conf = new WildFlySwarmClasspathConf();

        conf.process( "package(javax.servlet) remove");

        assertThat( conf.getMatchers() ).hasSize(1);

        WildFlySwarmClasspathConf.Matcher matcher = conf.getMatchers().get(0);

        assertThat( matcher ).isNotNull();
        assertThat( matcher ).isInstanceOf(WildFlySwarmClasspathConf.PackageMatcher.class);

        assertThat( ((WildFlySwarmClasspathConf.PackageMatcher) matcher).pkg).isEqualTo( "javax.servlet" );

        assertThat( matcher.getAction() ).isInstanceOf(WildFlySwarmClasspathConf.RemoveAction.class);
    }

    @Test
    public void testMavenMatcher_ReplaceSimpleModule() {
        WildFlySwarmClasspathConf conf = new WildFlySwarmClasspathConf();

        conf.process( "maven(org.jboss.spec:javax_servlet_specthingy) replace(javax.servlet.api)" );

        assertThat( conf.getMatchers() ).hasSize(1);

        WildFlySwarmClasspathConf.Matcher matcher = conf.getMatchers().get(0);

        assertThat( matcher ).isNotNull();
        assertThat( matcher ).isInstanceOf(WildFlySwarmClasspathConf.MavenMatcher.class);

        assertThat( ((WildFlySwarmClasspathConf.MavenMatcher) matcher).groupId).isEqualTo( "org.jboss.spec" );
        assertThat( ((WildFlySwarmClasspathConf.MavenMatcher) matcher).artifactId).isEqualTo( "javax_servlet_specthingy" );

        assertThat( matcher.getAction() ).isInstanceOf(WildFlySwarmClasspathConf.ReplaceAction.class);

        WildFlySwarmClasspathConf.ReplaceAction action = (WildFlySwarmClasspathConf.ReplaceAction) matcher.getAction();

        assertThat( action.moduleName ).isEqualTo( "javax.servlet.api" );
        assertThat( action.moduleSlot ).isEqualTo( "main" );
    }

    @Test
    public void testMavenMatcher_ReplaceModuleWithSlot() {
        WildFlySwarmClasspathConf conf = new WildFlySwarmClasspathConf();

        conf.process( "maven(org.jboss.spec:javax_servlet_specthingy) replace(javax.servlet.api:jbossorg)" );

        assertThat( conf.getMatchers() ).hasSize(1);

        WildFlySwarmClasspathConf.Matcher matcher = conf.getMatchers().get(0);

        assertThat( matcher ).isNotNull();
        assertThat( matcher ).isInstanceOf(WildFlySwarmClasspathConf.MavenMatcher.class);

        assertThat( ((WildFlySwarmClasspathConf.MavenMatcher) matcher).groupId).isEqualTo( "org.jboss.spec" );
        assertThat( ((WildFlySwarmClasspathConf.MavenMatcher) matcher).artifactId).isEqualTo( "javax_servlet_specthingy" );

        assertThat( matcher.getAction() ).isInstanceOf(WildFlySwarmClasspathConf.ReplaceAction.class);

        WildFlySwarmClasspathConf.ReplaceAction action = (WildFlySwarmClasspathConf.ReplaceAction) matcher.getAction();

        assertThat( action.moduleName ).isEqualTo( "javax.servlet.api" );
        assertThat( action.moduleSlot ).isEqualTo( "jbossorg" );
    }

    @Test
    public void testMavenMatcher_Remove() {
        WildFlySwarmClasspathConf conf = new WildFlySwarmClasspathConf();

        conf.process( "maven(org.jboss.spec:javax_servlet_specthingy) remove");

        assertThat( conf.getMatchers() ).hasSize(1);

        WildFlySwarmClasspathConf.Matcher matcher = conf.getMatchers().get(0);

        assertThat( matcher ).isNotNull();
        assertThat( matcher ).isInstanceOf(WildFlySwarmClasspathConf.MavenMatcher.class);

        assertThat( ((WildFlySwarmClasspathConf.MavenMatcher) matcher).groupId).isEqualTo( "org.jboss.spec" );
        assertThat( ((WildFlySwarmClasspathConf.MavenMatcher) matcher).artifactId).isEqualTo( "javax_servlet_specthingy" );

        assertThat( matcher.getAction() ).isInstanceOf(WildFlySwarmClasspathConf.RemoveAction.class);
    }
}
