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
package org.wildfly.swarm.microprofile_metrics;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;

import com.jayway.restassured.response.Header;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author Heiko W. Rupp
 */
@RunWith(Arquillian.class)
public class MicroprofileMetricsTest {

  private static final String APPLICATION_JSON = "application/json";
 	private static final String TEXT_PLAIN = "text/plain";


  @Deployment(testable = false)
  public static Archive deployment() {
      JARArchive deployment = ShrinkWrap.create(JARArchive.class);
      deployment.add(EmptyAsset.INSTANCE, "nothing");
      return deployment;
  }

  @Test
 	@RunAsClient
 	@InSequence(6)
 	public void testBasePrometheus() {
 	    given()
 	        .header("Accept",TEXT_PLAIN)
 	        .when().get("/metrics/base")
 	        .then()
 	        .statusCode(200)
 	        .and().contentType(TEXT_PLAIN)
 	        .and()
 	        .body(containsString("# TYPE base:thread_max_count"),
 	              containsString("base:thread_max_count"));
 	}

	@Test
  @RunAsClient
  @InSequence(1)
  public void testApplicationJsonResponseContentType() {
      Header acceptHeader = new Header("Accept", APPLICATION_JSON);

      given().header(acceptHeader).when().get("/metrics").then().statusCode(200).and().contentType(APPLICATION_JSON);

  }

  @Test
  @RunAsClient
  @InSequence(2)
  public void testTextPlainResponseContentType() {
      Header acceptHeader = new Header("Accept", TEXT_PLAIN);

      given().header(acceptHeader).when().get("/metrics").then().statusCode(200).and().contentType(TEXT_PLAIN);
  }

  @Test
  @RunAsClient
  @InSequence(3)
  public void testBadSubTreeWillReturn404() {
      when().get("/metrics/bad-tree").then().statusCode(404);
  }
}
