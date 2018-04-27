package io.thorntail.jca.impl.ironjacamar.parser;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.jca.common.metadata.ironjacamar.IronJacamarParser;

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
