package io.thorntail.hibernate_search.impl;

import io.thorntail.hibernate_search.MissingQueryBuilderContextException;
import io.thorntail.hibernate_search.QueryBuilderContext;
import static io.thorntail.hibernate_search.impl.InjectionPointUtils.getAnnotation;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * @author kg6zvp
 */
public class HibernateSearchExtension implements Extension {
    public void processQueryBuilderInjectionPoint(@Observes ProcessInjectionPoint pip) {
        if (!QueryBuilder.class.equals(pip.getInjectionPoint().getType()))
            return;
        QueryBuilderContext qc = getAnnotation(pip.getInjectionPoint(), QueryBuilderContext.class);
        if (qc == null) {
            pip.addDefinitionError(HibernateSearchMessages.MESSAGES.missingContextAnnotation(pip.getInjectionPoint().getMember()));
        }
    }
}
