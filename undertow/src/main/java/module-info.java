module org.jboss.unimbus.undertow {
    requires java.management;
    requires java.naming;
    requires java.security.jgss;
    requires java.security.sasl;
    requires java.sql;
    requires jdk.unsupported;

    requires xnio.api;

    requires transitive undertow.servlet;
    requires transitive undertow.core;
    requires transitive javax.servlet.api;
}