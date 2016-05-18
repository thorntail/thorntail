package org.wildfly.swarm.cli;

import org.junit.Test;


import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Bob McWhirter
 */
public class CommandLineTest {

    @Test
    public void testEmpty() {
        CommandLine cmd = CommandLine.parse();

        assertThat(cmd.get(CommandLine.HELP)).isFalse();
        assertThat(cmd.get(CommandLine.VERSION)).isFalse();
        assertThat(cmd.get(CommandLine.PROPERTIES)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testHelpLong() {
        CommandLine cmd = CommandLine.parse("--help");

        assertThat(cmd.get(CommandLine.HELP)).isTrue();
        assertThat(cmd.get(CommandLine.VERSION)).isFalse();
        assertThat(cmd.get(CommandLine.PROPERTIES)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testHelpShort() {
        CommandLine cmd = CommandLine.parse("-h");

        assertThat(cmd.get(CommandLine.HELP)).isTrue();
        assertThat(cmd.get(CommandLine.VERSION)).isFalse();
        assertThat(cmd.get(CommandLine.PROPERTIES)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testVersionLong() {
        CommandLine cmd = CommandLine.parse("--version");

        assertThat(cmd.get(CommandLine.HELP)).isFalse();
        assertThat(cmd.get(CommandLine.VERSION)).isTrue();
        assertThat(cmd.get(CommandLine.PROPERTIES)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testVersionShort() {
        CommandLine cmd = CommandLine.parse("-v");

        assertThat(cmd.get(CommandLine.HELP)).isFalse();
        assertThat(cmd.get(CommandLine.VERSION)).isTrue();
        assertThat(cmd.get(CommandLine.PROPERTIES)).isEmpty();
        assertThat(cmd.extraArguments()).isEmpty();
    }

    @Test
    public void testProperties() {
        CommandLine cmd = CommandLine.parse("-Dfoo", "-Dbar=cheese");

        assertThat(cmd.get(CommandLine.HELP)).isFalse();
        assertThat(cmd.get(CommandLine.VERSION)).isFalse();
        assertThat(cmd.extraArguments()).isEmpty();

        assertThat(cmd.get(CommandLine.PROPERTIES)).hasSize(2);
        assertThat(cmd.get(CommandLine.PROPERTIES).get("foo")).isEqualTo("true");
        assertThat(cmd.get(CommandLine.PROPERTIES).get("bar")).isEqualTo("cheese");
    }

    @Test
    public void testComplex() {
        CommandLine cmd = CommandLine.parse(
                "--help",
                "--version",
                "-Dfoo",
                "ken",
                "-Dbar=cheese",
                "heiko",
                "bob");

        assertThat(cmd.get(CommandLine.HELP)).isTrue();
        assertThat(cmd.get(CommandLine.VERSION)).isTrue();
        assertThat(cmd.extraArguments()).hasSize(3);
        assertThat(cmd.extraArguments().get(0)).isEqualTo("ken");
        assertThat(cmd.extraArguments().get(1)).isEqualTo("heiko");
        assertThat(cmd.extraArguments().get(2)).isEqualTo("bob");

        assertThat(cmd.get(CommandLine.PROPERTIES)).hasSize(2);
        assertThat(cmd.get(CommandLine.PROPERTIES).get("foo")).isEqualTo("true");
        assertThat(cmd.get(CommandLine.PROPERTIES).get("bar")).isEqualTo("cheese");


    }

}
