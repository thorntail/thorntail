package org.jboss.unimbus.jndi;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class InMemoryContext implements Context {


    public InMemoryContext(Hashtable<?, ?> environment) {
        this.parent = null;
        this.environment = environment;
    }

    public InMemoryContext(InMemoryContext parent) {
        this.parent = parent;
        this.environment = null;
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        Object result = this.bindings.get(name);
        return result;
    }

    @Override
    public Object lookup(String name) throws NamingException {
        return lookup(new CompositeName(name));
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        this.bindings.put(name, obj);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        bind(new CompositeName(name), obj);
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {

    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        rebind(new CompositeName(name), obj);

    }

    @Override
    public void unbind(Name name) throws NamingException {

    }

    @Override
    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        rename(new CompositeName(oldName), new CompositeName(newName));
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return listBindings(new CompositeName(name));
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {

    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(new CompositeName(name));
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        return null;
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(new CompositeName(name));
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        return null;
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        return lookupLink(new CompositeName(name));
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        return new InMemoryNameParser();
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser(new CompositeName(name));
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        return null;
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        return null;
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return null;
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        return null;
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        if (this.parent != null) {
            return this.parent.getEnvironment();
        }
        return this.environment;
    }

    @Override
    public void close() throws NamingException {

    }

    @Override
    public String getNameInNamespace() throws NamingException {
        return null;
    }

    private final InMemoryContext parent;

    private final Hashtable<?, ?> environment;

    private final Map<Name, Object> bindings = new HashMap<>();
}
