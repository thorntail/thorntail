package io.thorntail.migrate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bob on 3/13/18.
 */
public abstract class FileMigrator<IN,OUT> {

    public FileMigrator(Path file, Iterable<? extends Rule<IN,OUT>> rules) {
        this.file = file;
        this.rules = rules;
    }

    protected abstract IN createInputContext() throws Exception;
    protected abstract OUT createOutputContext() throws Exception;
    protected abstract void finish(OUT output) throws Exception;

    public void migrate() throws Exception {
        System.err.println("migrate: " + this.file );
        IN context = createInputContext();

        List<Action<IN,OUT>> actions = new ArrayList<>();

        this.rules.forEach(rule -> {
            actions.addAll(rule.match(context) );
        });

        OUT out = createOutputContext();

        actions.forEach(e->{
            //System.err.println( "match: " + e.getRule() );
            e.apply(out);
        });

        finish(out);
    }


    protected Path file;

    private final Iterable<? extends Rule<IN,OUT>> rules;
}
