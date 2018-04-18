package io.thorntail.testsuite.vertx;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by bob on 2/12/18.
 */
@ApplicationScoped
public class Results {

    public void add(String message) {
        synchronized ( results ) {
            this.results.add(message);
            this.results.notifyAll();;
        }
    }

    public List<String> get() {
        return this.results;
    }

    public void await(int number, int ms) throws InterruptedException {
        synchronized (  this.results ) {
            while ( this.results.size() < number ) {
                this.results.wait(ms);
            }
        }
    }

    private List<String> results = new ArrayList<>();
}
