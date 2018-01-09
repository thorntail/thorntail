module org.jboss.unimbus.weld {
    requires java.desktop;
    requires java.logging;
    requires java.xml;

    exports javax.enterprise.context;
    exports javax.enterprise.event;
    exports javax.enterprise.inject;
    exports javax.inject;

    exports org.jboss.weld.bean.proxy;
    exports org.jboss.weld.environment.se;
    exports org.jboss.weld.inject;
    exports org.jboss.weld.interceptor.proxy;
    exports org.jboss.weld.interceptor.util.proxy;
    exports org.jboss.weld.proxy;
}