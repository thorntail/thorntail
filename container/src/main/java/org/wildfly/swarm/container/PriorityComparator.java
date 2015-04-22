package org.wildfly.swarm.container;

import java.util.Comparator;

/**
 * @author Bob McWhirter
 */
public class PriorityComparator implements Comparator<Subsystem> {

    @Override
    public int compare(Subsystem o1, Subsystem o2) {
        if ( o1 instanceof Subsystem && o2 instanceof Subsystem ) {
            return Integer.compare( o1.getPriority(), o2.getPriority() );
        }
        return 0;
    }
}
