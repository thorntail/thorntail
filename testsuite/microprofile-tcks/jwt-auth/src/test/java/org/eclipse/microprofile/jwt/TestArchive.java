/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.jwt;

import java.net.URL;

import org.eclipse.microprofile.jwt.tck.container.jaxrs.TCKApplication;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class TestArchive {

    static final String SUFFIX = ".war";

    public static WebArchive createBase(Class<?> testClass) {
        URL publicKey = TestArchive.class.getResource("/publicKey.pem");
        return ShrinkWrap.create(WebArchive.class, testClass.getSimpleName() + SUFFIX)
                .addAsResource(publicKey, "/publicKey.pem")
                .addClass(TCKApplication.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

}
