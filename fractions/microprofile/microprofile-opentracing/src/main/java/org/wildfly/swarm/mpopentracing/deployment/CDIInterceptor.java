/**
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.mpopentracing.deployment;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import java.lang.reflect.Method;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.Path;
import org.eclipse.microprofile.opentracing.Traced;

/**
 * @author Pavol Loffay
 */
@Traced
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
public class CDIInterceptor {

  @Inject
  private Tracer tracer;

  @AroundInvoke
  public Object interceptTraced(InvocationContext ctx) throws Exception {
    Scope activeScope = null;
    try {
      if (!isJaxRs(ctx.getMethod()) && isTraced(ctx.getMethod())) {
        activeScope = tracer.buildSpan(getOperationName(ctx.getMethod()))
            .startActive(true);
      }
      return ctx.proceed();
    } finally {
      if (activeScope != null) {
        activeScope.close();
      }
    }
  }

  /**
   * Determines whether invoked method is jax-rs endpoint
   * @param method invoked method
   * @return true if invoked method is jax-rs endpoint
   */
  protected boolean isJaxRs(Method method) {
    if (method.getAnnotation(Path.class) != null ||
        method.getDeclaringClass().getAnnotation(Path.class) != null) {
      return true;
    }
    return false;
  }

  /**
   * Determines whether invoked method should be traced or not
   * @param method invoked method
   * @return true if {@link Traced} defined on method or class has value true
   */
  protected boolean isTraced(Method method) {
    Traced classTraced = method.getDeclaringClass().getAnnotation(Traced.class);
    Traced methodTraced = method.getAnnotation(Traced.class);
    if (methodTraced != null) {
      return methodTraced.value();
    }
    return classTraced.value();
  }

  /**
   * Returns operation name for given method
   *
   * @param method invoked method
   * @return operation name
   */
  protected String getOperationName(Method method) {
    Traced classTraced = method.getDeclaringClass().getAnnotation(Traced.class);
    Traced methodTraced = method.getAnnotation(Traced.class);
    if (methodTraced != null && methodTraced.operationName().length() > 0) {
      return methodTraced.operationName();
    } else if (classTraced != null && classTraced.operationName().length() > 0) {
      return classTraced.operationName();
    }
    return String.format("%s.%s", method.getDeclaringClass().getName(), method.getName());
  }
}
