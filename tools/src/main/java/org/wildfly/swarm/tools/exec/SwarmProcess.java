package org.wildfly.swarm.tools.exec;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Bob McWhirter
 */
public class SwarmProcess {

    private final Process process;

    private final IOBridge stdout;

    private final IOBridge stderr;

    private final CountDownLatch latch;

    public SwarmProcess(Process process, OutputStream stdout, Path stdoutFile, OutputStream stderr, Path stderrFile) throws IOException {
        this.process = process;
        this.latch = new CountDownLatch(1);
        this.stdout = new IOBridge(this.latch, process.getInputStream(), stdout, stdoutFile);
        this.stderr = new IOBridge(this.latch, process.getErrorStream(), stderr, stderrFile);

        new Thread(this.stdout).start();
        new Thread(this.stderr).start();
    }

    public Exception getError() {
        if ( this.stdout.getError() != null ) {
            return this.stdout.getError();
        }
        if ( this.stderr.getError() != null ) {
            return this.stderr.getError();
        }
        return null;
    }

    public OutputStream getOutputStream() {
        return process.getOutputStream();
    }

    public void destroy() {
        process.destroy();
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public Process destroyForcibly() {
        return process.destroyForcibly();
    }

    public int waitFor() throws InterruptedException {
        return process.waitFor();
    }

    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
        return process.waitFor(timeout, unit);
    }

    public int exitValue() {
        return process.exitValue();
    }

    public void awaitDeploy(long timeout, TimeUnit timeUnit) throws InterruptedException {
        this.latch.await(timeout, timeUnit);
    }

    public int stop() {
        return stop( 10, TimeUnit.SECONDS );
    }

    public int stop(long timeout, TimeUnit timeUnit) {
        this.process.destroy();
        try {
            if (!this.process.waitFor(timeout, timeUnit)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to destroy process", e);
        }
        try {
            this.stdout.close();
        } catch (IOException e) {
            // ignore
        }

        try {
            this.stderr.close();
        } catch (IOException e) {
            // ignore
        }

        if ( ! process.isAlive() ) {
            return process.exitValue();
        }

        return -1;
    }
}
