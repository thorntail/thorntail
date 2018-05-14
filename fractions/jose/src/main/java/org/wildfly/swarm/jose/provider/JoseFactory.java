/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.jose.provider;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ServiceLoader;

import org.jboss.logging.Logger;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseFraction;

public abstract class JoseFactory {

    private static Logger log = Logger.getLogger(JoseFactory.class);
    private static JoseFactory instance;

    /**
     * Obtain the JoseFactory using the ServiceLoader pattern.
     *
     * @return the factory instance
     */
    public static JoseFactory instance() {
        if (instance == null) {
            synchronized (JoseFactory.class) {
                if (instance != null) {
                    return instance;
                }

                ClassLoader cl = AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
                if (cl == null) {
                    cl = JoseFactory.class.getClassLoader();
                }

                JoseFactory newInstance = loadSpi(cl);

                if (newInstance == null && cl != JoseFactory.class.getClassLoader()) {
                    cl = JoseFactory.class.getClassLoader();
                    newInstance = loadSpi(cl);
                }
                if (newInstance == null) {
                    newInstance = new DefaultJoseFactory();
                }

                instance = newInstance;
            }
        }

        return instance;
    }

    /**
     * Look for a JoseFactory service implementation using the ServiceLoader.
     *
     * @param cl - the ClassLoader to pass into the {@link ServiceLoader#load(Class, ClassLoader)} method.
     * @return the JoseFactory if found, null otherwise
     */
    private static JoseFactory loadSpi(ClassLoader cl) {
        if (cl == null) {
            return null;
        }

        // start from the root CL and go back down to the TCCL
        JoseFactory instance = loadSpi(cl.getParent());

        if (instance == null) {
            ServiceLoader<JoseFactory> sl = ServiceLoader.load(JoseFactory.class, cl);
            URL u = cl.getResource("/META-INF/services/org.wildfly.swarm.jose.JoseFactory");
            log.debugf("loadSpi, cl=%s, u=%s, sl=%s", cl, u, sl);
            try {
                for (Object spi : sl) {
                    if (spi instanceof JoseFactory) {
                        if (instance != null) {
                            log.warn("Multiple JoseFactory implementations found: "
                                + spi.getClass().getName() + " and " + instance.getClass().getName());
                            break;
                        } else {
                            log.debugf("sl=%s, loaded=%s", sl, spi);
                            instance = (JoseFactory)spi;
                        }
                    }
                }
            } catch (Throwable e) {
                log.warn("Failed to locate JoseFactory provider", e);
            }
        }
        return instance;
    }

    public abstract Jose getJose(JoseFraction fraction);
}
