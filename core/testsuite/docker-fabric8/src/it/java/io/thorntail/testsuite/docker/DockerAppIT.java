package io.thorntail.testsuite.docker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.AssertionFailedError;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 2/14/18.
 */
public class DockerAppIT {

    @Test
    public void testIt() throws Exception {
        assertLogContains("Staring, foo:null");
    }

    protected void assertLogContains(String fragment) {
        List<String> log = getLog();

        Optional<String> found = log.stream()
                .filter(e -> e.contains(fragment))
                .findFirst();

        if (!found.isPresent()) {
            new AssertionFailedError("log does not contain: " + fragment);
        }
    }

    protected List<String> getLog() {
        try (BufferedReader reader = new BufferedReader(new FileReader(Paths.get("target").resolve("container.log").toFile()))) {
            return reader.lines().collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
