package io.thorntail.testsuite.opentracing.jaeger.vertx;

import java.util.List;

import javax.enterprise.event.Observes;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.eclipse.microprofile.opentracing.Traced;

import io.thorntail.vertx.VertxConsume;
import io.thorntail.vertx.VertxMessage;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@Traced
public class EmployeeFetcher {

    @PersistenceContext
    private EntityManager em;

    void fetchEmployees(@Observes @VertxConsume("employees") VertxMessage message) {

        List<Employee> employees = em.createNamedQuery("Employee.findAll", Employee.class).getResultList();

        JsonArray employeesJson = new JsonArray();
        for (Employee employee : employees) {
            employeesJson.add(new JsonObject().put("name", employee.getName()).put("id", employee.getId()));
        }
        message.reply(employeesJson);
    }

}
