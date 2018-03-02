package org.jboss.unimbus.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilities for sorting objects which may have {@code @Priority} annotations.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class Priorities {
    private Priorities() {

    }

    /** Sort from low to high, with default of {@code 0} for items without a {@code @Priority} annotation.
     *
     * @param items The items to sort.
     * @param <T> The type of the items.
     * @return The sorted list.
     */
    public static <T> List<T> highToLow(Iterable<T> items) {
        List<T> sorted = new ArrayList<>();

        for (T item : items) {
            sorted.add(item);
        }

        Collections.sort(sorted, HighToLowPriorityComparator.INSTANCE );

        return sorted;
    }
}
