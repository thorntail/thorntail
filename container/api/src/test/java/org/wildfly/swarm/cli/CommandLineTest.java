package org.wildfly.swarm.cli;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.wildfly.swarm.cli.CommandLine.HELP;
import static org.wildfly.swarm.cli.CommandLine.PROPERTY;
import static org.wildfly.swarm.cli.CommandLine.VERSION;

/**
 * @author Bob McWhirter
 */
public class CommandLineTest {

    @Test
    public void testEmpty() throws Exception {
        CommandLine cmd = CommandLine.parse();

        assertThat(cmd.get(HELP)).isFalse();
        assertThat(cmd.get(VERSION)).isFalse();
        assertThat(cmd.get(PROPERTY)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testHelpLong() throws Exception {
        CommandLine cmd = CommandLine.parse("--help");

        assertThat(cmd.get(HELP)).isTrue();
        assertThat(cmd.get(VERSION)).isFalse();
        assertThat(cmd.get(PROPERTY)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testHelpShort() throws Exception {
        CommandLine cmd = CommandLine.parse("-h");

        assertThat(cmd.get(HELP)).isTrue();
        assertThat(cmd.get(VERSION)).isFalse();
        assertThat(cmd.get(PROPERTY)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testVersionLong() throws Exception {
        CommandLine cmd = CommandLine.parse("--version");

        assertThat(cmd.get(HELP)).isFalse();
        assertThat(cmd.get(VERSION)).isTrue();
        assertThat(cmd.get(PROPERTY)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testVersionShort() throws Exception {
        CommandLine cmd = CommandLine.parse("-v");

        assertThat(cmd.get(HELP)).isFalse();
        assertThat(cmd.get(VERSION)).isTrue();
        assertThat(cmd.get(PROPERTY)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testProperties() throws Exception {
        CommandLine cmd = CommandLine.parse("-Dfoo", "-Dbar=cheese");

        assertThat(cmd.get(HELP)).isFalse();
        assertThat(cmd.get(VERSION)).isFalse();
        assertThat(cmd.extraArguments()).isEmpty();

        assertThat(cmd.get(PROPERTY)).hasSize(2);
        assertThat(cmd.get(PROPERTY).get("foo")).isEqualTo("true");
        assertThat(cmd.get(PROPERTY).get("bar")).isEqualTo("cheese");
    }

    @Test
    public void testComplex() throws Exception {
        CommandLine cmd = CommandLine.parse(
                "--help",
                "--version",
                "-Dfoo",
                "ken",
                "-Dbar=cheese",
                "heiko",
                "bob");

        assertThat(cmd.get(HELP)).isTrue();
        assertThat(cmd.get(VERSION)).isTrue();
        assertThat(cmd.extraArguments()).hasSize(3);
        assertThat(cmd.extraArguments().get(0)).isEqualTo("ken");
        assertThat(cmd.extraArguments().get(1)).isEqualTo("heiko");
        assertThat(cmd.extraArguments().get(2)).isEqualTo("bob");

        assertThat(cmd.get(PROPERTY)).hasSize(2);
        assertThat(cmd.get(PROPERTY).get("foo")).isEqualTo("true");
        assertThat(cmd.get(PROPERTY).get("bar")).isEqualTo("cheese");


    }

}
