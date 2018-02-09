package org.jboss.unimbus.testsuite.jms.driven;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class Results {

    public void add(String body) {
        this.results.add( body );
    }

    public Set<String> get() {
        return this.results;
    }

    private Set<String> results = new ConcurrentSkipListSet<>();

}
