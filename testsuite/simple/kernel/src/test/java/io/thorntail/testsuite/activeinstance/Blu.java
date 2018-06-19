package io.thorntail.testsuite.activeinstance;

import javax.inject.Inject;

@SomeBinding
public class Blu {

    @Inject
    Bla bla;

    String blabla() {
        return bla.bla();
    }

}
