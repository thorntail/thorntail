package org.jboss.unimbus.logging;

import java.util.logging.Level;

/**
 * Created by bob on 1/16/18.
 */
public interface Logging {
    void initialize();
    void setLevel(String name, Level level);
}
