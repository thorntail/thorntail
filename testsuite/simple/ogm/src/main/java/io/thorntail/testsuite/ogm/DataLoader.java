package io.thorntail.testsuite.ogm;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Initializes the database with test data because the sql-load-script option is not supported by OGM
 * @author smccollum
 */
@Singleton
public class DataLoader {
	@PersistenceContext
	EntityManager em;

	@PostConstruct
	public void initDb(){
		/**
		 * Initialize test data
		 */
		em.getTransaction().begin();
		em.persist(new Employee(1, "Penny"));
		em.persist(new Employee(2, "Sheldon"));
		em.persist(new Employee(3, "Amy"));
		em.persist(new Employee(4, "Leonard"));
		em.persist(new Employee(5, "Bernadette"));
		em.persist(new Employee(6, "Raj"));
		em.persist(new Employee(7, "Howard"));
		em.persist(new Employee(8, "Priya"));
		em.getTransaction().commit();
	}
}
