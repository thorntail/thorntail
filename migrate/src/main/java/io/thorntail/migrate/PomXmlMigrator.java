package io.thorntail.migrate;

import java.io.FileInputStream;
import java.io.Writer;
import java.nio.file.Path;

import io.thorntail.migrate.maven.ModelRule;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.jdom.MavenJDOMWriter;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.WriterFactory;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;

/**
 * Created by bob on 3/13/18.
 */
public class PomXmlMigrator extends FileMigrator<Model, Model> {

    public PomXmlMigrator(Path file, Iterable<ModelRule> rules) throws Exception {
        super(file, rules);
        final SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringBoundaryWhitespace(false);
        builder.setIgnoringElementContentWhitespace(false);

        this.doc = builder.build(this.file.toFile());
        MavenXpp3Reader reader = new MavenXpp3Reader();
        this.model = reader.read(new FileInputStream(this.file.toFile()));
    }

    @Override
    protected Model createInputContext() throws Exception {
        return this.model;
    }

    @Override
    protected Model createOutputContext() throws Exception {
        return this.model;
    }

    @Override
    protected void finish(Model output) throws Exception {
        MavenJDOMWriter jdomWriter = new MavenJDOMWriter();
        String encoding = model.getModelEncoding();
        if ( encoding == null ) {
            encoding = "UTF-8";
        }

        Writer writer = WriterFactory.newWriter(this.file.toFile(), encoding);
        Format format = Format.getRawFormat().setEncoding( encoding ).setTextMode(Format.TextMode.PRESERVE );
        format.setLineSeparator("\n");
        jdomWriter.write(model, this.doc, writer,  format );
    }

    private final Model model;

    private final Document doc;
}
