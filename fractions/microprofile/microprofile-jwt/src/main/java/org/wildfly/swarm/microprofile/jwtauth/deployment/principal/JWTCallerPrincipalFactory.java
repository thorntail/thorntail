/**
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.wildfly.swarm.microprofile.jwtauth.deployment.principal;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ServiceLoader;

import org.jboss.logging.Logger;

/**
 * The factory class that provides the token string to JWTCallerPrincipal parsing for a given implementation.
 */
public abstract class JWTCallerPrincipalFactory {
    private static Logger log = Logger.getLogger(JWTCallerPrincipalFactory.class);
    private static JWTCallerPrincipalFactory instance;

    /**
     * Obtain the JWTCallerPrincipalFactory that has been set or by using the ServiceLoader pattern.
     *
     * @return the factory instance
     * @see #setInstance(JWTCallerPrincipalFactory)
     */
    public static JWTCallerPrincipalFactory instance() {
        if (instance == null) {
            synchronized (JWTCallerPrincipalFactory.class) {
                if (instance != null) {
                    return instance;
                }

                ClassLoader cl = AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
                if (cl == null) {
                    cl = JWTCallerPrincipalFactory.class.getClassLoader();
                }

                JWTCallerPrincipalFactory newInstance = loadSpi(cl);

                if (newInstance == null && cl != JWTCallerPrincipalFactory.class.getClassLoader()) {
                    cl = JWTCallerPrincipalFactory.class.getClassLoader();
                    newInstance = loadSpi(cl);
                }
                if (newInstance == null) {
                    throw new IllegalStateException("No JWTCallerPrincipalFactory implementation found!");
                }

                instance = newInstance;
            }
        }

        return instance;
    }

    /**
     * Look for a JWTCallerPrincipalFactory service implementation using the ServiceLoader.
     *
     * @param cl - the ClassLoader to pass into the {@link ServiceLoader#load(Class, ClassLoader)} method.
     * @return the JWTCallerPrincipalFactory if found, null otherwise
     */
    private static JWTCallerPrincipalFactory loadSpi(ClassLoader cl) {
        if (cl == null) {
            return null;
        }

        // start from the root CL and go back down to the TCCL
        JWTCallerPrincipalFactory instance = loadSpi(cl.getParent());

        if (instance == null) {
            ServiceLoader<JWTCallerPrincipalFactory> sl = ServiceLoader.load(JWTCallerPrincipalFactory.class, cl);
            URL u = cl.getResource("/META-INF/services/org.eclipse.microprofile.jwt.principal.JWTCallerPrincipalFactory");
            log.debugf("loadSpi, cl=%s, u=%s, sl=%s", cl, u, sl);
            try {
                for (JWTCallerPrincipalFactory spi : sl) {
                    if (instance != null) {
                        throw new IllegalStateException(
                                "Multiple JWTCallerPrincipalFactory implementations found: "
                                        + spi.getClass().getName() + " and "
                                        + instance.getClass().getName());
                    } else {
                        log.debugf("sl=%s, loaded=%s", sl, spi);
                        instance = spi;
                    }
                }
            } catch (Throwable e) {
                log.warn("Failed to locate JWTCallerPrincipalFactory provider", e);
            }
        }
        return instance;
    }

    /**
     * Set the instance. It is used by OSGi environment where service loader pattern is not supported.
     *
     * @param resolver the instance to use.
     */
    public static void setInstance(JWTCallerPrincipalFactory resolver) {
        instance = resolver;
    }

    /**
     * Parse the given bearer token string into a JWTCallerPrincipal instance.
     *
     * @param token - the bearer token provided for authorization
     * @return A JWTCallerPrincipal representation for the token.
     * @throws ParseException on parse or verification failure.
     */
    public abstract JWTCallerPrincipal parse(String token, JWTAuthContextInfo authContextInfo) throws ParseException;
}
