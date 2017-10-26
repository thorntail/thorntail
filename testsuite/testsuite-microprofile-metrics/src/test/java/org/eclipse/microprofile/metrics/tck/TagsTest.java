/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.eclipse.microprofile.metrics.tck;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TagsTest {

    private Metadata metadata = new Metadata("count", "countMe", "countMe tags test", MetricType.COUNTER,
            MetricUnits.PERCENT, "colour=blue");

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void tagsTest() {
        Assert.assertNotNull(metadata);
        Assert.assertTrue(metadata.getTags().containsValue("blue"));
    }

    @Test
    public void addTagsTest() {
        metadata.addTags("colour=green,size=medium");
        metadata.addTag("number=5");

        Assert.assertNotNull(metadata);
        Assert.assertTrue(metadata.getTags().containsKey("size"));
        Assert.assertTrue(metadata.getTags().containsValue("green"));
        Assert.assertFalse(metadata.getTags().containsValue("blue"));
        Assert.assertTrue(metadata.getTags().containsKey("number"));
    }

}
