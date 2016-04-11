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
