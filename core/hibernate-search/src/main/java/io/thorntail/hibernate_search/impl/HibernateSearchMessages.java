package io.thorntail.hibernate_search.impl;

import io.thorntail.hibernate_search.MissingQueryBuilderContextException;
import io.thorntail.logging.impl.LoggingUtil;
import static io.thorntail.logging.impl.LoggingUtil.CODE;
import static io.thorntail.logging.impl.MessageOffsets.HIBERNATE_SEARCH_OFFSET;
import java.lang.reflect.Member;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 *
 * @author smccollum
 */
@MessageLogger(projectCode = CODE, length = 6)
public interface HibernateSearchMessages extends BasicLogger {
    HibernateSearchMessages MESSAGES = Logger.getMessageLogger(HibernateSearchMessages.class, LoggingUtil.loggerCategory("hibernate-search"));

    @Message(id = 0 + HIBERNATE_SEARCH_OFFSET, value = "Annotation io.thorntail.hibernate_search.QueryBuilderContext is required to inject org.hibernate.search.query.dsl.QueryBuilder, not found on %s")
    MissingQueryBuilderContextException missingContextAnnotation(Member member);
}
