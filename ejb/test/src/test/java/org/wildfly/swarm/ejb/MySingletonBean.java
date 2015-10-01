package org.wildfly.swarm.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

/**
 * @author Bob McWhirter
 */
@Singleton
public class MySingletonBean implements MySingleton {

    @PostConstruct
    public void postConstruct() {
        System.err.println( "constructed" );
    }

    @Override
    public void sayHowdy() {
        System.err.println( "howdy!" );
    }
}
