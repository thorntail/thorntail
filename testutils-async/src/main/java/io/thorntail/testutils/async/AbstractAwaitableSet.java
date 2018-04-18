package io.thorntail.testutils.async;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeoutException;

/**
 * Created by bob on 2/27/18.
 */
public class AbstractAwaitableSet<T> {

    protected AbstractAwaitableSet() {

    }

    public void add(T item) {
        this.items.add(item);
    }

    public Set<T> get() {
        return this.items;
    }

    public void await(int count) throws TimeoutException, InterruptedException {
        await(count, 30*1000);
    }

    public void await(int count, long timeout) throws InterruptedException, TimeoutException {
        long startTick = System.currentTimeMillis();
        synchronized (this.items) {
            long remaining = timeout - (System.currentTimeMillis() - startTick);
            if (remaining < 0) {
                throw new TimeoutException();
            }
            while (this.items.size() < count) {
                this.items.wait(remaining);
            }
        }
    }

    private Set<T> items = new ConcurrentSkipListSet<T>();

}
