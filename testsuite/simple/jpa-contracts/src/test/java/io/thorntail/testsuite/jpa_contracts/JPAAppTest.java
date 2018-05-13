package io.thorntail.testsuite.jpa_contracts;

import io.thorntail.test.ThorntailTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import io.thorntail.testsuite.jpa_contracts.jpa_impl.EnforcedEntityManagerFactory;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(ThorntailTestRunner.class)
public class JPAAppTest {

	@Test
	public void testJpa() {
		when().get("/").then().assertThat().statusCode(200);
		
		assertThat(EnforcedEntityManagerFactory.inst_count.get("ContractPersistenceUnit").get()).isEqualTo(1);
		assertThat(EnforcedEntityManagerFactory.inst.get("ContractPersistenceUnit").iterator().next().isOpen()).isTrue();
	}
}
