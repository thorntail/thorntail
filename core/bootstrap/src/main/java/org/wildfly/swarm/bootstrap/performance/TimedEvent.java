package org.wildfly.swarm.bootstrap.performance;

/**
 * @author Bob McWhirter
 */
class TimedEvent implements AutoCloseable {

    private final long start;

    private long stop = -1;

    TimedEvent() {
        this.start = System.currentTimeMillis();
    }

    boolean isOpen() {
        return this.stop < 0;
    }

    @Override
    public void close() throws Exception {
        this.stop = System.currentTimeMillis();
    }

    long durationMs() {
        if (this.stop < 0) {
            return System.currentTimeMillis() - this.start;
        }

        return this.stop - this.start;
    }
}
