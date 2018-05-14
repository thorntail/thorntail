package io.thorntail.testsuite.opentracing.jaeger;

import java.util.List;
import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.thorntail.test.ThorntailTestRunner;
import io.thorntail.testutils.opentracing.jaeger.SpanNode;
import io.thorntail.testutils.opentracing.jaeger.SpanTree;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static io.thorntail.testutils.opentracing.jaeger.SpanNode.assertThat;
import static io.thorntail.testutils.opentracing.jaeger.SpanTree.assertThat;

@RunWith(ThorntailTestRunner.class)
public class OpenTracingJaegerAppIT {

    @Test
    public void test() throws Exception {
        String startResponse =
                when()
                        .get("/start")
                        .then()
                        //.statusCode(200)
                        .extract().response().body().asString();

        Thread.sleep(1000);

        String[] parts = startResponse.split("\\|");
        String traceId = parts[0];
        String employeeList = parts[1];

        assertThat( employeeList )
                .contains("Penny")
                .contains("Sheldon")
                .contains("Amy")
                .contains("Leonard")
                .contains("Bernadette")
                .contains("Raj")
                .contains("Howard")
                .contains("Priya");

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

        assertThat(tree).hasRootSpans(2);

        SpanNode start = tree.getRootNodes().get(0);
        assertThat(start)
                .hasChildSpans(1)
                .hasOperationName("GET:io.thorntail.testsuite.opentracing.jaeger.Employees.start")
                .hasTag("http.status_code", 200)
                .hasTag("span.kind", "server");

        SpanNode client = start.getChildren().get(0);
        assertThat(client).hasChildSpans(1)
                .hasOperationName("GET")
                .hasTag("span.kind", "client")
                .hasTag("http.status_code", 200)
                .hasTag("http.url", "http://localhost:8080/employees");

        SpanNode employees = client.getChildren().get(0);
        assertThat(employees)
                .hasChildSpans(1)
                .hasOperationName("GET:io.thorntail.testsuite.opentracing.jaeger.Employees.getEmployees")
                .hasTag("span.kind", "server")
                .hasTag("http.status_code", 200);

        SpanNode send = employees.getChildren().get(0);
        assertThat(send)
                .hasChildSpans(1)
                .hasOperationName("jms-send")
                .hasTag("message_bus.destination", "employees")
                .hasTag("span.kind", "producer");

        SpanNode receive = send.getChildren().get(0);
        assertThat(receive)
                .hasChildSpans(3)
                .hasOperationName("jms-receive")
                .hasTag("message_bus.destination", "employees")
                .hasTag("span.kind", "consumer");

        SpanNode jpa = receive.getChildren().get(0);
        assertThat(jpa)
                .hasChildSpans(1)
                .hasOperationName("Employee.findAll/getResultList")
                .hasTag("class", "io.thorntail.testsuite.opentracing.jaeger.Employee");

        SpanNode ds = jpa.getChildren().get(0);
        assertThat(ds)
                .hasChildSpans(0)
                .hasOperationName("executeQuery")
                .hasTag("db.instance", "mem:")
                .hasTag("db.user", "sa")
                .hasTag("db.type", "sql");

        SpanNode reply = receive.getChildren().get(1);
        assertThat(reply)
                .hasChildSpans(1)
                .hasOperationName("jms-send")
                .hasTag("span.kind", "producer")
                .hasTag("message_bus.destination");

        SpanNode receiveReply = reply.getChildren().get(0);
        assertThat( receiveReply )
                .hasChildSpans(0)
                .hasOperationName("jms-receive")
                .hasTag("span.kind", "consumer")
                .hasTag("jms.message.id" )
                .hasTag("message_bus.destination", (String) reply.getTags().get("message_bus.destination"));

        SpanNode loggerSend = receive.getChildren().get(2);
        assertThat(loggerSend)
                .hasChildSpans(1)
                .hasOperationName("vertx-send")
                .hasTag("span.kind", "producer")
                .hasTag("message_bus.destination", "fetch-logger");

        SpanNode loggerReceive = loggerSend.getChildren().get(0);
        assertThat(loggerReceive)
                .hasChildSpans(0)
                .hasOperationName("vertx-receive")
                .hasTag("span.kind", "consumer")
                .hasTag("message_bus.destination", "fetch-logger");



        /*
        SpanNode jpa = servlet.getChildren().get(0);

        assertThat(jpa).hasTag("class", Employee.class.getName());
        assertThat(jpa).hasChildSpans(1);

        SpanNode ds = jpa.getChildren().get(0);
        assertThat(ds).hasOperationName("executeQuery");
        assertThat(ds).hasTag("db.instance", "mem:");
        assertThat(ds).hasTag("db.user", "sa");
        assertThat(ds).hasTag("db.type", "sql");
        */
    }
}
