package org.jboss.unimbus.testsuite.opentracing.jaeger;

import java.util.List;
import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.jboss.unimbus.testutils.opentracing.jaeger.SpanNode;
import org.jboss.unimbus.testutils.opentracing.jaeger.SpanTree;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.fest.assertions.Assertions.assertThat;
import static org.jboss.unimbus.testutils.opentracing.jaeger.SpanNode.assertThat;
import static org.jboss.unimbus.testutils.opentracing.jaeger.SpanTree.assertThat;

@RunWith(UNimbusTestRunner.class)
public class OpenTracingJaegerAppIT {

    @Test
    public void test() throws Exception {
        String traceId =
                when()
                        .get("/")
                        .then()
                        .statusCode(200)
                        .extract().response().body().print();

        Thread.sleep(1000);

        RestAssured.baseURI = "http://localhost:16686/";

        ExtractableResponse<Response> response = when()
                .get("/api/traces/" + traceId)
                .then()
                .extract();

        List<Map<String, ?>> data = response.jsonPath().get("data");
        assertThat(data).hasSize(1);
        Map<String, ?> dataMap = data.get(0);

        assertThat(dataMap.get("traceID")).isEqualTo(traceId);

        SpanTree tree = new SpanTree(dataMap);

        System.err.println("---");
        System.err.println(tree);
        System.err.println("---");

        assertThat(tree).hasRootSpans(1);

        SpanNode servlet = tree.getRootNodes().get(0);

        assertThat(servlet).hasChildSpans(1);

        SpanNode jpa = servlet.getChildren().get(0);

        assertThat(jpa).hasTag("class", Employee.class.getName());
        assertThat(jpa).hasChildSpans(1);

        SpanNode ds = jpa.getChildren().get(0);
        assertThat(ds).hasOperationName("executeQuery");
        assertThat(ds).hasTag("db.instance", "mem:");
        assertThat(ds).hasTag("db.user", "sa");
        assertThat(ds).hasTag("db.type", "sql");
    }
}
