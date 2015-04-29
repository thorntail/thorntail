package org.wildfly.swarm.container.util;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Bob McWhirter
 */
public class XmlWriter implements AutoCloseable {

    private final Writer out;

    public XmlWriter(Writer out) {
        this.out = out;
    }

    public Element element(String name) throws IOException {
        return new Element(name);
    }

    public void close() throws IOException {
        this.out.close();
    }

    public class Element {
        private String name;
        private boolean hasContent = false;

        Element(String name) throws IOException {
            this.name = name;
            out.write( "<" + name );
        }

        public Element attr(String name, String value) throws IOException {
            out.write( " " + name + "=\"" + value + "\"" );
            return this;
        }

        public Element element(String name) throws IOException {
            if ( ! this.hasContent ) {
                this.hasContent = true;
                out.write( ">" );
            }
            return new Element(name);
        }

        public Element content(String content) throws IOException {
            if ( ! this.hasContent ) {
                this.hasContent = true;
                out.write( ">" );
            }

            out.write( content );
            return this;
        }

        public void end() throws IOException {
            if ( hasContent ) {
                out.write( "</" + name  + ">" );
            } else {
                out.write( "/>" );
            }
        }
    }
}
