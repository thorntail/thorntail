package org.jboss.unimbus.index.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.unimbus.UNimbus;
import org.jboss.unimbus.impl.CoreMessages;

/**
 * @author Ken Finnigan
 */
public class IndexProducer {

    private static final String INDEX_LOCATION = "META-INF/unimbus.idx";

    @Inject
    private UNimbus uNimbus;

    @Produces
    @ApplicationScoped
    IndexView produceIndex() {
        List<IndexView> indexes = new ArrayList<>();
        URL indexUrl = uNimbus.getClassLoader().getResource(INDEX_LOCATION);

        if (indexUrl != null) {
            // Load Index
            try (InputStream indexStream = indexUrl.openStream()) {
                CoreMessages.MESSAGES.loadIndex(indexUrl.getPath());
                return new IndexReader(indexStream).read();
            } catch (IOException ioe) {
                CoreMessages.MESSAGES.loadingIndexFileFailed(indexUrl.getPath(), ioe);
            }
        }

        // Return empty index
        CoreMessages.MESSAGES.indexNotFound(INDEX_LOCATION);
        return CompositeIndex.create(Collections.EMPTY_LIST);
    }
}
