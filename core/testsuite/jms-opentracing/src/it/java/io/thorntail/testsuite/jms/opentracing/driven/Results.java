package io.thorntail.testsuite.jms.opentracing.driven;

import javax.enterprise.context.ApplicationScoped;

import io.thorntail.testutils.async.AbstractAwaitableSet;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class Results extends AbstractAwaitableSet<String> {

}
