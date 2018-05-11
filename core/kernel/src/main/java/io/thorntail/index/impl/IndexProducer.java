package io.thorntail.index.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.thorntail.impl.KernelMessages;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import io.thorntail.Thorntail;

/**
 * @author Ken Finnigan
 */
public class IndexProducer {

    private static final String INDEX_LOCATION = "META-INF/thorntail.idx";

    @Inject
    private Thorntail thorntail;

    @Produces
    @ApplicationScoped
    IndexView produceIndex() {
        URL indexUrl = thorntail.getClassLoader().getResource(INDEX_LOCATION);

        if (indexUrl != null) {
            // Load Index
            try (InputStream indexStream = indexUrl.openStream()) {
                KernelMessages.MESSAGES.loadIndex(indexUrl.getPath());
                return new IndexReader(indexStream).read();
            } catch (IOException ioe) {
                KernelMessages.MESSAGES.loadingIndexFileFailed(indexUrl.getPath(), ioe);
            }
        }

        // Return empty index
        KernelMessages.MESSAGES.indexNotFound(INDEX_LOCATION);
        return CompositeIndex.create(Collections.EMPTY_LIST);
    }
}
