/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.tools.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

/**
 * @author Bob McWhirter
 */
public class IOBridge implements Runnable, Closeable {

    public IOBridge(CountDownLatch latch, InputStream in, OutputStream out, Path file) throws IOException {
        this.in = in;
        this.out = (out instanceof PrintStream ? (PrintStream) out : new PrintStream(out));
        if (file != null) {
            Files.createDirectories(file.getParent());
            this.fileOut = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
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

    @Override
    public void close() throws IOException {
        try {
            this.in.close();
        } finally {
            if (this.fileOut != null) {
                this.fileOut.close();
            }
        }
    }

    protected void processLine(String line) throws IOException {
        out.println(line);
        out.flush();
        if (this.fileOut != null) {
            fileOut.write(line);
            fileOut.newLine();
            fileOut.flush();
        }
        if (line.contains("WFSWARM99999")) {
            this.latch.countDown();
        }
    }

    private final InputStream in;

    private final PrintStream out;

    private final CountDownLatch latch;

    private BufferedWriter fileOut;

    private Exception error;
}
