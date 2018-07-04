/**
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.plugin.maven.migrate;

import org.joox.Context;
import org.joox.Match;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.joox.JOOX.$;

final class MavenUtils {
    private MavenUtils() {
        // avoid instantiation
    }

    private static final Pattern PROPERTY_REFERENCE = Pattern.compile("\\$\\{(.*?)\\}");

    static Match $$(Context match) {
        return $(match).namespace("m", "http://maven.apache.org/POM/4.0.0");
    }

    static Match parsePomXml(Path path) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(path.toFile());

        return $(document).namespace("m", "http://maven.apache.org/POM/4.0.0");
    }

    static void writePomXml(Path path, Match pom) throws TransformerException, IOException {
        try (OutputStream out = new FileOutputStream(path.toFile())) {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Source source = new DOMSource(pom.document());
            Result target = new StreamResult(out);
            transformer.transform(source, target);
        }
    }

    static boolean refersToProperty(String version) {
        return version != null && PROPERTY_REFERENCE.matcher(version).find();
    }

    static Set<String> propertyNamesReferredFrom(String version) {
        // there should really only be one -- if there are more, should we abort?
        Set<String> result = new HashSet<>();
        Matcher matcher = PROPERTY_REFERENCE.matcher(version);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }
}
