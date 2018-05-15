package io.thorntail.testsuite.ogm;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.inject.Inject;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Ken Finnigan
 */
@WebServlet(name = "EmployeeServlet", urlPatterns = "/*")
public class EmployeeServlet extends HttpServlet {

	@PersistenceContext
	private EntityManager em;

	@Inject
	DataLoader dl; //initialize test data

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		em.getTransaction().begin();
		List<Employee> employees = em.createNamedQuery("Employee.findAll", Employee.class).getResultList();
		em.getTransaction().commit();
		PrintWriter writer = resp.getWriter();

		writer.write("<html><head></head><body>\n");
		writer.write("Employees:\n\n");
		writer.write("<table><tr><th>Id</th><th>Name</th></tr>\n");

		for (Employee employee : employees) {
			writer.write("<tr><td>" + employee.getId() + "</td><td>" + employee.getName() + "</td></tr>\n");
		}

		writer.write("</body></html>");
	}
}
