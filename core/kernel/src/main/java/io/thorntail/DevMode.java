package io.thorntail;

/**
 * Useful constants and information.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class DevMode {

    private DevMode() {
        // prevent construction
    }

    public static final String ENVIRONMENT_VAR_NAME = Info.KEY.toUpperCase() + "_DEV_MODE";

    public static final String RESTART = "RESTART";

    public static final String RELOAD = "RELOAD";

    public static final String DEBUG = "DEBUG";

    public static boolean isDevMode() {
        String devMode = System.getenv(ENVIRONMENT_VAR_NAME);
        return (devMode != null && (!devMode.trim().equals("")));
    }

    public static boolean isRestart() {
        String devMode = System.getenv(ENVIRONMENT_VAR_NAME);
        return (devMode != null && devMode.equalsIgnoreCase(RESTART));
    }

    public static boolean isReload() {
        String devMode = System.getenv(ENVIRONMENT_VAR_NAME);
        return (devMode != null && devMode.equalsIgnoreCase(RELOAD));
    }

    public static boolean isDebug() {
        String devMode = System.getenv(ENVIRONMENT_VAR_NAME);
        return (devMode != null && devMode.equalsIgnoreCase(DEBUG));
    }
}
