/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile_metrics.test;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.wildfly.swarm.microprofile_metrics.runtime.Tag;

/**
 * @author hrupp
 */
public class TagsTest {

  @Test
  public void testParseOne() {
    Tag tag = new Tag("a=b");
    assert tag.getKey().equals("a");
    assert tag.getValue().equals("b");
  }

  @Test
  public void testParseHole() {
    Tag tag = new Tag("a = b");
    assert tag.getKey().equals("a");
    assert tag.getValue().equals("b");
  }

  @Test
  public void testParseSeparate() {
    Tag tag = new Tag("a","b");
    assert tag.getKey().equals("a");
    assert tag.getValue().equals("b");
  }

  @Test
  public void testParseSeparateHole() {
    Tag tag = new Tag(" a","b ");
    assert tag.getKey().equals("a");
    assert tag.getValue().equals("b");
  }

  @Test
  public void testParseInvalid() {
    try {
      new Tag("a=");
      assert false;
    }
    catch (IllegalArgumentException iae) {
      assert true;
    }
  }

  @Test
  public void testFromMap() throws Exception {
    Map<String,String> map = new HashMap<>(2);
    map.put("key","myKey");
    map.put("value","myValue");
    Tag t = new Tag(map);

    assert t.getKey().equals("myKey");
    assert t.getValue().equals("myValue");
  }
}
