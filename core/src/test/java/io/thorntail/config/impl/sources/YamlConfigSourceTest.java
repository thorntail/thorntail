package io.thorntail.config.impl.sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 2/6/18.
 */
public class YamlConfigSourceTest {

    @Test
    public void testKeyOfNoPrefix() {
        String result = YamlConfigSource.keyOf(null, "taco");
        assertThat(result).isEqualTo("taco");
    }

    @Test
    public void testKeyOfWithPrefix() {
        String result = YamlConfigSource.keyOf("cheesy", "taco");
        assertThat(result).isEqualTo("cheesy.taco");
    }

    @Test
    public void testEscapeAndJoinOneItemNoEscape() {
        String result = YamlConfigSource.escapeAndJoin(new ArrayList<String>() {{
            add("one");
        }});

        assertThat(result).isEqualTo("one");
    }

    @Test
    public void testEscapeAndJoinOneItemWithEscape() {
        String result = YamlConfigSource.escapeAndJoin(new ArrayList<String>() {{
            add("one,two");
        }});

        assertThat(result).isEqualTo("one\\,two");
    }

    @Test
    public void testEscapeAndJoinTwoItemsNoEscape() {
        String result = YamlConfigSource.escapeAndJoin(new ArrayList<String>() {{
            add("one");
            add("two");
        }});

        assertThat(result).isEqualTo("one,two");
    }

    @Test
    public void testEscapeAndJoinTwoItemsWithEscape() {
        String result = YamlConfigSource.escapeAndJoin(new ArrayList<String>() {{
            add("one,two");
            add("three");
        }});

        assertThat(result).isEqualTo("one\\,two,three");
    }

    @Test
    public void testFlattenSimple() {
        Map<String, ?> tree = new HashMap<String, Object>() {{
            put("a_string", "taco");
            put("an_integer", 22);
            put("a_boolean", true);
        }};

        Map<String, String> map = YamlConfigSource.flatten(tree);

        assertThat(map).hasSize(3);
        assertThat(map.get("a_string")).isEqualTo("taco");
        assertThat(map.get("an_integer")).isEqualTo("22");
        assertThat(map.get("a_boolean")).isEqualTo("true");
    }

    @Test
    public void testFlattenNested() {
        Map<String, ?> tree = new HashMap<String, Object>() {{
            put("a_string", "taco");
            put("an_integer", 22);
            put("a_boolean", true);
            put( "sub", new HashMap<String,Object>() {{
                put( "a_string", "cheese");
            }});
        }};

        Map<String, String> map = YamlConfigSource.flatten(tree);

        assertThat(map).hasSize(4);
        assertThat(map.get("a_string")).isEqualTo("taco");
        assertThat(map.get("an_integer")).isEqualTo("22");
        assertThat(map.get("a_boolean")).isEqualTo("true");
        assertThat(map.get("sub.a_string")).isEqualTo("cheese");
    }
}
