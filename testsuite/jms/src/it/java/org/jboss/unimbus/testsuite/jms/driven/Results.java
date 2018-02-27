package org.jboss.unimbus.testsuite.jms.driven;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.testutils.async.AbstractAwaitableSet;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class Results extends AbstractAwaitableSet<String> {

}
