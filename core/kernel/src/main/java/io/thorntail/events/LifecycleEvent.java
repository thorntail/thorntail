package io.thorntail.events;

/**
 * Base event for higher-order lifecycle operations.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class LifecycleEvent {

    /**
     * Event emitted to signal components should perform any bootstrapping operations.
     */
    public static class Bootstrap extends LifecycleEvent {

    }

    /**
     * Event emitted to signal components should perform any scanning of deployments.
     */
    public static class Scan extends LifecycleEvent {

    }

    /**
     * Event emitted to signal components should perform initialization.
     */
    public static class Initialize extends LifecycleEvent {

    }

    /**
     * Event emitted to signal components should perform deployment of components..
     */
    public static class Deploy extends LifecycleEvent {

    }

    /**
     * Event emitted immediately before starting.
     */
    public static class BeforeStart extends LifecycleEvent {

    }

    /**
     * Event emitted to signal components should start.
     */
    public static class Start extends LifecycleEvent {

    }

    /**
     * Event emitted immediately after starting.
     */
    public static class AfterStart extends LifecycleEvent {

    }

}
