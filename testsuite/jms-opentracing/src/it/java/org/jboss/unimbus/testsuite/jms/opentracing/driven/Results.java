package org.jboss.unimbus.testsuite.jms.opentracing.driven;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class Results {

    public void add(String body) {
        synchronized ( this.results ) {
            this.results.add( body );
            this.results.notifyAll();
        }
    }

    public Set<String> get() {
        return this.results;
    }

    public void await(int count, long timeout) throws InterruptedException {
        synchronized ( this.results ) {
            while ( this.results.size() < count ) {
                this.results.wait( timeout );
            }
        }
    }

    public void clear() {
        this.results.clear();
    }

    private Set<String> results = new ConcurrentSkipListSet<>();

}
