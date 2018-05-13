package io.thorntail.testsuite.jpa_contracts;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "EMServlet", urlPatterns = "/*")
public class EMServlet extends HttpServlet {

	@PersistenceUnit
	EntityManagerFactory emf;

	@PersistenceContext
	EntityManager em;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.getWriter().write("Ok\n");
	}
}