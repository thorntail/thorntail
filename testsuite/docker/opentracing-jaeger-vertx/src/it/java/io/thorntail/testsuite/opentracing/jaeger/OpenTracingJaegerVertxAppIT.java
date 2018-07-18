package io.thorntail.testsuite.opentracing.jaeger;

import static io.restassured.RestAssured.when;
import static io.thorntail.testutils.opentracing.jaeger.SpanNode.assertThat;
import static io.thorntail.testutils.opentracing.jaeger.SpanTree.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.thorntail.test.ThorntailTestRunner;
import io.thorntail.testutils.opentracing.jaeger.SpanNode;
import io.thorntail.testutils.opentracing.jaeger.SpanTree;

@RunWith(ThorntailTestRunner.class)
public class OpenTracingJaegerVertxAppIT {

    @Test
    public void test() throws Exception {

        ExtractableResponse<Response> response = when()
                .get("/employees")
                .then()
                .extract();

        String traceId = response.jsonPath().get("traceId");
        List<Map<String, ?>> data = response.jsonPath().get("data");
        assertThat(data).hasSize(8);
        assertNames(data, "Penny", "Sheldon", "Amy", "Leonard", "Bernadette", "Raj", "Howard", "Priya");

        RestAssured.baseURI = "http://localhost:16686/";
        response = when()
                .get("/api/traces/" + traceId)
                .then()
                .extract();

        data = response.jsonPath().get("data");
        assertThat(data).hasSize(1);
        Map<String, ?> dataMap = data.get(0);

        assertThat(dataMap.get("traceID")).isEqualTo(traceId);

        SpanTree tree = new SpanTree(dataMap);

        System.err.println("---");
        System.err.println(tree);
        System.err.println("---");

        assertThat(tree).hasRootSpans(1);

        SpanNode employees = tree.getRootNodes().get(0);
        assertThat(employees)
                .hasChildSpans(1)
                .hasOperationName("GET:io.thorntail.testsuite.opentracing.jaeger.vertx.Employees.getEmployees")
                .hasTag("http.url")
                .hasTag("http.method")
                .hasTag("span.kind", "server");

        SpanNode send = employees.getChildren().get(0);
        assertThat(send)
                .hasChildSpans(1)
                .hasOperationName("vertx-send")
                .hasTag("message_bus.destination", "employees")
                .hasTag("span.kind", "producer");

        SpanNode observer = send.getChildren().get(0);
        assertThat(observer)
                .hasChildSpans(2)
                .hasOperationName("vertx-observer-notify:io.thorntail.testsuite.opentracing.jaeger.vertx.EmployeeFetcher.fetchEmployees")
                .hasTag("message_bus.destination", "employees")
                .hasTag("span.kind", "consumer");

        SpanNode jpa = observer.getChildren().get(0);
        assertThat(jpa)
                .hasChildSpans(1)
                .hasOperationName("Employee.findAll/getResultList")
                .hasTag("class", "io.thorntail.testsuite.opentracing.jaeger.vertx.Employee");

        SpanNode ds = jpa.getChildren().get(0);
        assertThat(ds)
                .hasChildSpans(0)
                .hasOperationName("executeQuery")
                .hasTag("db.instance", "mem:")
                .hasTag("db.user", "sa")
                .hasTag("db.type", "sql");

    }

    private void assertNames(List<Map<String, ?>> data, String... names) {
        for (int i = 0; i < data.size(); i++) {
            assertEquals(names[i], data.get(i).get("name"));
        }
    }
}
