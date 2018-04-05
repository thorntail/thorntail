package org.jboss.unimbus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Useful constants and information.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class Info {

    private Info() {
        // prevent construction
    }

    /**
     * Human-readable project name.
     */
    public static final String NAME = "uNimbus";

    /**
     * Lowercase project key, suitable for property prefixes.
     */
    public static final String KEY = NAME.toLowerCase();

    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(Info.class.getClassLoader().getResourceAsStream("META-INF/" + KEY + "-info.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        VERSION = PROPERTIES.getProperty("version", "unknown");
    }

    /**
     * Version.
     */
    public static final String VERSION;

}
