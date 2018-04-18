package io.thorntail.testsuite.docker;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.thorntail.events.LifecycleEvent;

/**
 * Created by bob on 2/13/18.
 */
@ApplicationScoped
public class Startup {

    void startup(@Observes LifecycleEvent.Start event) {
        System.err.println("Staring, foo:" + System.getProperty("foo"));
        this.thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        this.thread.setDaemon(false);
        this.thread.start();
    }

    private Thread thread;
}
