package org.wildfly.swarm.tools.exec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

/**
 * @author Bob McWhirter
 */
public class IOBridge implements Runnable {

    private final InputStream in;
    private final OutputStream out;

    private FileOutputStream fileOut;

    private Exception error;

    private final CountDownLatch latch;

    public IOBridge(CountDownLatch latch, InputStream in, OutputStream out, Path file) throws IOException {
        this.in = in;
        this.out = out;
        if ( file != null ) {
            Files.createDirectories( file.getParent() );
            this.fileOut = new FileOutputStream( file.toFile() );
        }
        this.latch = latch;
    }

    public Exception getError() {
        return this.error;
    }

    @Override
    public void run() {

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            this.error = e;
            this.latch.countDown();
        }
    }

    protected void processLine(String line) throws IOException {
        out.write(line.getBytes());
        out.write('\n');
        out.flush();
        if ( this.fileOut != null ) {
            this.fileOut.write(line.getBytes());
            this.fileOut.write('\n');
            this.fileOut.flush();
        }
        if (line.contains("WFLYSRV0010")) {
            this.latch.countDown();
        }
    }

    public void close() throws IOException {
        this.in.close();
        if ( this.fileOut != null ) {
            this.fileOut.close();
        }
    }
}
