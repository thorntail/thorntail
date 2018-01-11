package org.jboss.unimbus.jndi;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

public class InMemoryNameParser implements NameParser {

    public InMemoryNameParser() {
    }

    @Override
    public Name parse(String name) throws NamingException {
        return new CompositeName(name);
    }
}
