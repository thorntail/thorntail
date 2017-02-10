package org.wildfly.swarm.fractions.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Consumer;

import org.wildfly.swarm.spi.meta.FractionDetector;
import org.wildfly.swarm.spi.meta.WebXmlFractionDetector;

/**
 * @author Ken Finnigan
 */
public class WebXmlDescriptorScanner implements Scanner<InputStream> {
    @Override
    public String extension() {
        return "xml";
    }

    @Override
    public void scan(String name, final InputStream input, Collection<FractionDetector<InputStream>> detectors, Consumer<File> handleFileAsZip) throws IOException {
        if (name.endsWith("web.xml")) {
            detectors.stream()
                    .filter(d -> WebXmlFractionDetector.class.isAssignableFrom(d.getClass()))
                    .forEach(d -> d.detect(input));
        }
    }
}
