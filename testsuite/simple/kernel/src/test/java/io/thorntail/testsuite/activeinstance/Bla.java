package io.thorntail.testsuite.activeinstance;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;

@Dependent
public class Bla {

    static final AtomicBoolean DESTROYED = new AtomicBoolean(false);

    private String bla;

    public Bla() {
        this.bla = "yak";
    }

    String bla() {
        return bla;
    }

    @PreDestroy
    void destroy() {
        DESTROYED.set(true);
    }

}
