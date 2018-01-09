package org.wildfly.swarm.jsp.test;

/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import category.CommunityOnly;
import category.ProductOnly;
import org.apache.http.client.fluent.Request;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Heiko Braun
 */
public class TransformerIT {

    @Test
    @Category(CommunityOnly.class)
    public void verifyTransformerFactoryName_Community() throws Exception {
        String result = Request.Get("http://localhost:8080/transformer").execute().returnContent().asString();
        Assert.assertTrue("Expected transformer factory class to be org.apache.xalan.xsltc.trax.TransformerFactoryImpl but was " + result,
                          result.startsWith("org.apache.xalan.xsltc.trax.TransformerFactoryImpl"));
    }

    @Test
    @Ignore
    @Category(ProductOnly.class)
    public void verifyTransformerFactoryName_Product() throws Exception {
        String result = Request.Get("http://localhost:8080/transformer").execute().returnContent().asString();
        Assert.assertTrue("Expected transformer factory class to be org.apache.xalan.processor.TransformerFactoryImpl but was " + result,
                          result.startsWith("org.apache.xalan.processor.TransformerFactoryImpl"));
    }
}
