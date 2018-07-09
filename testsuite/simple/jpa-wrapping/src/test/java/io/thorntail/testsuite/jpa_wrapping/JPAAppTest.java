package io.thorntail.testsuite.jpa_wrapping;

import io.thorntail.test.ThorntailTestRunner;
import io.thorntail.testsuite.jpa_wrapping.jpa_wrappers.InsideWrapper;
import io.thorntail.testsuite.jpa_wrapping.jpa_wrappers.OutsideWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ThorntailTestRunner.class)
public class JPAAppTest {
    @PersistenceContext
    EntityManager em;

    @Test
    public void testWrapping() {
        assertThat(em).isInstanceOf(OutsideWrapper.OutsideDelegate.class); // outside wrapper should be on the outside
        assertThat(((OutsideWrapper.OutsideDelegate)em).getEntityManagerDelegate()).isInstanceOf(InsideWrapper.InsideDelegate.class);
    }
}
