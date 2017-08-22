package org.wildfly.swarm.internal;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

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

}
