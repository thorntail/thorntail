package io.thorntail.plugins.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by bob on 2/13/18.
 */
public class Plan implements Iterable<Entry> {

    public Plan() {
        this(null);
    }

    public Plan(Plan parent) {
        this.parent = parent;
    }

    public List<Entry> getEntries() {
        if ( this.parent == null ) {
            return this.entries;
        }
        List<Entry> entries = new ArrayList<>();
        entries.addAll( this.entries );
        entries.addAll( this.parent.getEntries() );
        return entries;
    }

    @Override
    public Iterator<Entry> iterator() {
        return getEntries().iterator();
    }

    void add(Entry entry) {
        this.entries.add(entry);
    }

    private final Plan parent;

    private List<Entry> entries = new ArrayList<>();

}
