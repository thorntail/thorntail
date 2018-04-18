package io.thorntail.testsuite.jpa.opentracing;

import javax.inject.Inject;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.thorntail.test.ThorntailTestRunner;
import io.thorntail.testutils.opentracing.SpanNode;
import io.thorntail.testutils.opentracing.SpanTree;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(ThorntailTestRunner.class)
public class JPAOpenTracingAppTest {

    @Test
    public void test() {
        String responseBody =
                when()
                        .get("/")
                        .then()
                        .statusCode(200)
                        .extract().response().body().print();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isNotEmpty();
        assertThat(responseBody).contains("<tr><td>1</td><td>Penny</td></tr>");
        assertThat(responseBody).contains("<tr><td>2</td><td>Sheldon</td></tr>");
        assertThat(responseBody).contains("<tr><td>3</td><td>Amy</td></tr>");
        assertThat(responseBody).contains("<tr><td>4</td><td>Leonard</td></tr>");
        assertThat(responseBody).contains("<tr><td>7</td><td>Howard</td></tr>");

        SpanTree tree = new SpanTree((MockTracer) this.tracer);

        System.err.println( "-- tree --" );
        System.err.println( tree );
        System.err.println( "-- tree --" );

        SpanTree.assertThat(tree).hasRootSpans(2);

        SpanNode getNode = tree.getRootNodes().get(1);
        SpanNode.assertThat(getNode).hasChildSpans(1);

        SpanNode jpaNode = getNode.getChildren().get(0);

        SpanNode.assertThat(jpaNode).hasChildSpans(1);
        SpanNode.assertThat(jpaNode).hasOperationName("Employee.findAll/getResultList");
        SpanNode.assertThat(jpaNode).hasTag("class", Employee.class.getName());

        SpanNode sqlNode = jpaNode.getChildren().get(0);
        SpanNode.assertThat(sqlNode).hasNoChildSpans();
        SpanNode.assertThat(sqlNode).hasOperationName("executeQuery");
        SpanNode.assertThat(sqlNode).hasTag("db.instance", "mem:");
        SpanNode.assertThat(sqlNode).hasTag("db.type", "sql");
        SpanNode.assertThat(sqlNode).hasTag("db.user", "sa");
        SpanNode.assertThat(sqlNode).hasTag("db.statement");
    }

    @Inject
    Tracer tracer;
}
