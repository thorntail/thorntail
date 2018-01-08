package org.wildfly.swarm.container.runtime.wildfly;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by bob on 1/3/18.
 */
public class SizingOutputStream extends OutputStream {

    public SizingOutputStream() {

    }

    @Override
    public void write(int b) throws IOException {
        ++this.size;
    }

    public long getSize() {
        return this.size;
    }

    private long size;
}
