package org.jboss.unimbus.events;

/**
 * Created by bob on 1/16/18.
 */
public class LifecycleEvent {

    public static class Scan extends LifecycleEvent {

    }

    public static class Initialize extends LifecycleEvent {

    }

    public static class Deploy extends LifecycleEvent {

    }

    public static class BeforeStart extends LifecycleEvent {

    }

    public static class Start extends  LifecycleEvent {

    }

    public static class AfterStart extends LifecycleEvent {

    }

}
