package io.thorntail.testsuite.tracing;

import javax.enterprise.context.Dependent;

import org.eclipse.microprofile.opentracing.Traced;

@Traced
@Dependent
public class TracedComponent {

    public void alpha() {
    }

    public void bravo() {
    }

}
