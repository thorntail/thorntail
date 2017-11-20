/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.health.runtime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.xnio.IoUtils;
import org.xnio.channels.StreamSourceChannel;
import org.xnio.conduits.AbstractStreamSinkConduit;
import org.xnio.conduits.ConduitWritableByteChannel;
import org.xnio.conduits.Conduits;
import org.xnio.conduits.StreamSinkConduit;

/**
 * Conduit that saves all the data that is written through it
 *
 * @author Heiko Braun
 */
public class BufferingSinkConduit extends AbstractStreamSinkConduit<StreamSinkConduit> {

    private final List<byte[]> data = new CopyOnWriteArrayList<>();

    /**
     * Construct a new instance.
     *
     * @param next the delegate conduit to set
     */
    public BufferingSinkConduit(StreamSinkConduit next) {
        super(next);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int pos = src.position();
        int res = super.write(src);
        if (res > 0) {
            byte[] d = new byte[res];
            for (int i = 0; i < res; ++i) {
                d[i] = src.get(i + pos);
            }
            data.add(d);
        }
        return res;
    }

    @Override
    public long write(ByteBuffer[] dsts, int offs, int len) throws IOException {
        for (int i = offs; i < len; ++i) {
            if (dsts[i].hasRemaining()) {
                return write(dsts[i]);
            }
        }
        return 0;
    }

    @Override
    public long transferFrom(final FileChannel src, final long position, final long count) throws IOException {
        return src.transferTo(position, count, new ConduitWritableByteChannel(this));
    }

    @Override
    public long transferFrom(final StreamSourceChannel source, final long count, final ByteBuffer throughBuffer) throws IOException {
        return IoUtils.transfer(source, count, throughBuffer, new ConduitWritableByteChannel(this));
    }

    @Override
    public int writeFinal(ByteBuffer src) throws IOException {
        return Conduits.writeFinalBasic(this, src);
    }

    @Override
    public long writeFinal(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return Conduits.writeFinalBasic(this, srcs, offset, length);
    }

    @Override
    public void terminateWrites() throws IOException {
        super.terminateWrites();
        data.clear();
    }

    public void flushTo(StringBuffer sb) {
        if (!data.isEmpty()) {
            for (int i = 0; i < data.size(); ++i) {
                try {
                    sb.append(new String(data.get(i), "UTF-8"));  // TODO retrieve the encoding from the http response headers
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

