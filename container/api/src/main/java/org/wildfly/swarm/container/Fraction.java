package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public interface Fraction {

    default String simpleName() {
        String name = getClass().getSimpleName();
        if ( name.endsWith( "Fraction" ) ) {
            name = name.substring( 0, name.length() - "Fraction".length() );
        }
        return name;
    }

    default void initialize(Container.InitContext initContext) {
        // Do Nothing
    }

    default void postInitialize(Container.PostInitContext initContext) {
        // Do Nothing
    }

}
