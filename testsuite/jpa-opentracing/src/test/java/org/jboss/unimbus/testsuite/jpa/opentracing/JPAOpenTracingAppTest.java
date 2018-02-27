package org.jboss.unimbus.testsuite.jpa.opentracing;

import javax.inject.Inject;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.jboss.unimbus.testutils.opentracing.SpanNode;
import org.jboss.unimbus.testutils.opentracing.SpanTree;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.fest.assertions.Assertions.assertThat;
import static org.jboss.unimbus.testutils.opentracing.SpanTree.assertThat;
import static org.jboss.unimbus.testutils.opentracing.SpanNode.assertThat;

@RunWith(UNimbusTestRunner.class)
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

        assertThat(tree).hasRootSpans(2);

        SpanNode getNode = tree.getRootNodes().get(1);
        assertThat(getNode).hasChildSpans(1);

        SpanNode jpaNode = getNode.getChildren().get(0);

        assertThat(jpaNode).hasChildSpans(1);
        assertThat(jpaNode).hasOperationName("Employee.findAll/getResultList");
        assertThat(jpaNode).hasTag("class", Employee.class.getName());

        SpanNode sqlNode = jpaNode.getChildren().get(0);
        assertThat(sqlNode).hasNoChildSpans();
        assertThat(sqlNode).hasOperationName("executeQuery");
        assertThat(sqlNode).hasTag("db.instance", "mem:");
        assertThat(sqlNode).hasTag("db.type", "sql");
        assertThat(sqlNode).hasTag("db.user", "sa");
        assertThat(sqlNode).hasTag("db.statement");
    }

    @Inject
    Tracer tracer;
}
