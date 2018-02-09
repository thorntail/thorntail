package org.jboss.unimbus.jca.ironjacamar.parser;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.jca.common.metadata.ironjacamar.IronJacamarParser;
import org.jboss.jca.common.metadata.spec.RaParser;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class IronJacamarParserProducer {

    @Produces
    IronJacamarParser parser() {
        return new IronJacamarParser();
    }
}
