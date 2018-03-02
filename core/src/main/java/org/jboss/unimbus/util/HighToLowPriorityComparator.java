package org.jboss.unimbus.util;

import java.util.Comparator;

import javax.annotation.Priority;

/**
 * Created by bob on 3/2/18.
 */
class HighToLowPriorityComparator implements Comparator {

    public static final HighToLowPriorityComparator INSTANCE = new HighToLowPriorityComparator();
    @Override
    public int compare(Object left, Object right) {
        int leftPrio = priorityOf(left);
        int rightPrio = priorityOf(right);
        return Integer.compare(rightPrio, leftPrio);
    }

    private int priorityOf(Object left) {
        Priority anno = Annotations.getAnnotation(left, Priority.class);
        if (anno != null) {
            return anno.value();
        }

        return 0;
    }
}
