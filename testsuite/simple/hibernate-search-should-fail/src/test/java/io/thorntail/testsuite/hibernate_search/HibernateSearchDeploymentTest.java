package io.thorntail.testsuite.hibernate_search;

import io.thorntail.hibernate_search.MissingQueryBuilderContextException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.jboss.weld.exceptions.DefinitionException;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * @author kg6zvp
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class HibernateSearchDeploymentTest {
    @Test
    public void testDeployWithoutContextAnnotationShouldFail() {
        assertThatThrownBy(() -> HibernateSearchApp.main())
            .isInstanceOf(DefinitionException.class)
            .hasMessageContaining("qb");
    }
}
