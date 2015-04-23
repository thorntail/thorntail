package org.wildfly.swarm.container;

import java.util.Comparator;

/**
 * @author Bob McWhirter
 */
public class PriorityComparator implements Comparator<Fraction> {

    @Override
    public int compare(Fraction o1, Fraction o2) {
        if ( o1 instanceof Fraction && o2 instanceof Fraction) {
            return Integer.compare( o1.getPriority(), o2.getPriority() );
        }
        return 0;
    }
}
