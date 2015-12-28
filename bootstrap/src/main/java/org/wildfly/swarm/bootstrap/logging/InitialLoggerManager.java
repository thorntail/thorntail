package org.wildfly.swarm.bootstrap.logging;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class InitialLoggerManager implements BackingLoggerManager {

    public static final InitialLoggerManager INSTANCE = new InitialLoggerManager();

    private static final String PREFIX = "swarm.log.";

    private final LevelNode root;

    private InitialLoggerManager() {
        Properties props = System.getProperties();
        Set<String> names = props.stringPropertyNames();
        List<String> categories = new ArrayList<>();
        Map<String, BootstrapLogger.Level> levels = new HashMap<>();

        for (String name : names) {
            if (name.startsWith(PREFIX)) {
                String category = name.substring(PREFIX.length());
                BootstrapLogger.Level level;
                String levelStr = props.getProperty(name);
                if (levelStr.equals("")) {
                    level = BootstrapLogger.Level.INFO;
                } else {
                    try {
                        level = Enum.valueOf(BootstrapLogger.Level.class, levelStr);
                    } catch (IllegalArgumentException e) {
                        level = BootstrapLogger.Level.INFO;
                    }
                }
                categories.add(category);
                levels.put(category, level);
            }
        }

        categories.sort((l, r) -> l.compareTo(r));

        BootstrapLogger.Level rootLevel = levels.get( "ROOT" );
        if ( rootLevel == null ) {
            rootLevel = BootstrapLogger.Level.NONE;
        }

        this.root = new LevelNode( "", rootLevel );

        for (String each : categories) {
            if ( each.equals( "ROOT" ) ) {
                continue;
            }

            this.root.add( each, levels.get( each ) );
        }
    }

    public LevelNode getRoot() {
        return this.root;
    }

    @Override
    public BackingLogger getBackingLogger(String name) {
        return new InitialBackingLogger(name, this.root.getLevel( name ) );
    }

    public synchronized void log(InitialBackingLogger logger, BootstrapLogger.Level level, Object message) {
        if ( level.ordinal() < logger.getLevel().ordinal() ) {
            return;
        }
        if ( message instanceof Throwable ) {
            log( logger, level, (Throwable) message );
        } else {
            log( logger, level, message.toString() );
        }
    }

    public synchronized void log(InitialBackingLogger logger, BootstrapLogger.Level level, Object message, Throwable t) {
        if ( level.ordinal() < logger.getLevel().ordinal() ) {
            return;
        }
        log( logger, level, message );
        log( logger, level, t );
    }

    private void log(InitialBackingLogger logger, BootstrapLogger.Level level, String message) {
        Date now = new Date();
        String[] lines = message.split("\n");

        for (String line : lines) {
            System.err.println(String.format("%s %s [%s] (%s) %s",
                    now,
                    level.toString(),
                    logger.getCategory(),
                    Thread.currentThread().getName(),
                    line));
        }
    }


    private void log(InitialBackingLogger logger, BootstrapLogger.Level level, Throwable t) {
        System.err.println(String.format("%s %s [%s] (%s) %s",
                new Date().toString(),
                level,
                logger.getCategory(),
                Thread.currentThread().getName(),
                t.getMessage()));
        for (StackTraceElement stackTraceElement : t.getStackTrace()) {
            System.err.println("  " + stackTraceElement.toString());
        }
    }

}
