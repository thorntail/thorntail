package org.wildfly.swarm.container.util;

import java.lang.reflect.Method;

/**
 * @author Bob McWhirter
 */
public class Invoker {

    public static Object invoke(Object self, String methodName, Class[] paramClasses, Object[] paramObjects) throws Exception {
        Method method = self.getClass().getMethod(methodName, paramClasses);
        return method.invoke( self, paramObjects );
    }
}
