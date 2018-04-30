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
package io.thorntail.testsuite.openapi;

import io.restassured.RestAssured;
import org.eclipse.microprofile.openapi.tck.utils.YamlToJsonFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 4/20/18
 */
@RunWith(Arquillian.class)
public class PostComplexStructuresTest {

    @BeforeClass
    public static void setUp() {
        RestAssured.filters(new YamlToJsonFilter());
    }

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackages(true, ResourceAcceptingList.class.getPackage());
    }

    @Test
    public void shouldGetListDescription() {
        given()
                .log().all()
        .when()
                .get("/openapi")
        .then()
                .statusCode(200)
                .body("paths.'/list'.post", not(is(nullValue())));
    }

    @Test
    public void shouldGetArrayDescription() throws InterruptedException {
        given()
                .log().all()
        .when()
                .get("/openapi")
        .then()
                .statusCode(200)
                .body("paths.'/list'.post", not(is(nullValue())));
    }

}
