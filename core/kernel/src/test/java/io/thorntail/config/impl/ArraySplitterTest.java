package io.thorntail.config.impl;

import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bob on 2/1/18.
 */
public class ArraySplitterTest {

    @Test
    public void testSingle() {
        List<String> list = ArraySplitter.split("foo");
        assertThat(list).hasSize(1);
        assertThat(list).contains("foo");
    }

    @Test
    public void testNoEscape() {
        List<String> list = ArraySplitter.split("foo,bar");
        assertThat(list).hasSize(2);
        assertThat(list).contains("foo");
        assertThat(list).contains("bar");
    }

    @Test
    public void testWithEscape() {
        List<String> list = ArraySplitter.split("foo\\,bar,bar");
        assertThat(list).hasSize(2);
        assertThat(list).contains("foo,bar");
        assertThat(list).contains("bar");
    }

}
