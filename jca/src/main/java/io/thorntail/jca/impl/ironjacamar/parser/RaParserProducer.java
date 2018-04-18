package io.thorntail.jca.impl.ironjacamar.parser;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.jca.common.metadata.spec.RaParser;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class RaParserProducer {

    @Produces
    RaParser parser() {
        return new RaParser();
    }
}
