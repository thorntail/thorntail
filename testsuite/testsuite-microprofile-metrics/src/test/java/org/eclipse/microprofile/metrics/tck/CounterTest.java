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

import org.eclipse.microprofile.metrics.Counter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Arquillian.class)
public class CounterTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private Counter count;

    @Test
    @InSequence(1)
    public void getCountTest() {
        Assert.assertEquals(0, count.getCount());
    }

    @Test
    @InSequence(2)
    public void incrementTest() {
        count.inc();
        Assert.assertEquals(1, count.getCount());
    }

    @Test
    @InSequence(3)
    public void incrementLongTest() {
        count.inc(4);
        Assert.assertEquals(5, count.getCount());
    }

    @Test
    @InSequence(4)
    public void decrementTest() {
        count.dec();
        Assert.assertEquals(4, count.getCount());
    }

    @Test
    @InSequence(5)
    public void decrementLongTest() {
        count.dec(4);
        Assert.assertEquals(0, count.getCount());
    }
}
