package io.vertx.resourceadapter.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Lin Gao <lgao@redhat.com>
 */
class SecurityActions {

    private SecurityActions() {
    }

    /**
     * Gets current context class loader
     *
     * @return the current context class loader
     */
    static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    /**
     * Sets current context classloader
     *
     * @param classLoader
     */
    static void setCurrentContextClassLoader(final ClassLoader classLoader) {
        if (System.getSecurityManager() == null) {
            Thread.currentThread().setContextClassLoader(classLoader);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    Thread.currentThread().setContextClassLoader(classLoader);
                    return null;
                }
            });
        }
    }

    /**
     * Gets the system property.
     *
     * @param propName the property name
     * @return the property value
     */
    static String getSystemProperty(final String propName) {
        if (System.getSecurityManager() == null) {
            return System.getProperty(propName);
        }
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty(propName);
            }
        });
    }

    // =========================================================
    // Some Util Methods Below
    // =========================================================

    /**
     * Whether the string is a valid expression.
     *
     * @param string the string
     * @return true if the string starts with '${' and ends with '}', false
     * otherwise
     */
    static boolean isExpression(final String string) {
        if (string == null) {
            return false;
        }
        return string.startsWith("${") && string.endsWith("}");
    }

    /**
     * Gets the express value by the key.
     *
     * @param key the key where the system property is set.
     * @return the expression value or the key itself if the system property is
     * not set.
     */
    static String getExpressValue(final String key) {
        if (isExpression(key)) {
            String keyValue = getSystemProperty(key.substring(2, key.length() - 1));
            return keyValue == null ? key : keyValue;
        } else {
            return key;
        }
    }

}
