/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile_metrics.runtime;


import java.util.Collection;
import javax.inject.Inject;
import javax.naming.NamingException;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.microprofile_metrics.MicroprofileMetricsFraction;
import org.wildfly.swarm.microprofile_metrics.runtime.app.CounterImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.app.GaugeImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.app.MeterImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.app.TimerImpl;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * @author hrupp
 */
@DeploymentScoped
public class MetricsAnnotationProcessor implements DeploymentProcessor {

  private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");

  private static final String ANNOTATION_BASE_PACKAGE = "org.eclipse.microprofile.metrics.annotation.";
  private String[] annotations = {"Gauge", "Counted", "Metered", "Metric", "Timed"};

  private static final DotName REGISTRY_TYPE =
      DotName.createSimple(ANNOTATION_BASE_PACKAGE + "RegistryType");

  private final Archive archive;
  private IndexView index;

  @Inject
  MicroprofileMetricsFraction myFraction;

  @Inject
  public MetricsAnnotationProcessor(Archive archive, IndexView index) {
    this.archive = archive;
    this.index = index;
  }

  @Override
  public void process() throws NamingException {

    for (String annotation : annotations) {
      processAnnotations(annotation);
    }

  }

  private void processAnnotations(String type) {

    DotName dotName = DotName.createSimple(ANNOTATION_BASE_PACKAGE + type);
    Collection<AnnotationInstance> annotations = index.getAnnotations(dotName);
    for (AnnotationInstance ai : annotations) {
      String name;
      String unit = null;
      String targetClazzName;
      AnnotationTarget at = ai.target();

      if (at.kind().equals(AnnotationTarget.Kind.FIELD)) {
        targetClazzName = ((FieldInfo) at).declaringClass().name().toString();
      } else if (at.kind().equals(AnnotationTarget.Kind.METHOD)) {
        targetClazzName = ((MethodInfo) at).declaringClass().name().toString();
      } else {
        throw new IllegalStateException("Unhandled kind " + at.kind() + " for " + ai.toString());
      }

      boolean absolute = ai.value("absolute") != null;

      if (ai.value("name") != null) {
        name = ai.value("name").value().toString();
      } else {
        name = getNameFromTarget(ai.target());
      }

      // Prepend the class name if not marked as absolute
      if (!absolute) {
        name = targetClazzName + "." + name;
      }

      if (ai.value("unit") != null) {
        unit = ai.value("unit").value().toString();
      }

      Metric m;
      Metadata metadata = new Metadata(name,MetricType.INVALID); // Dummy as catch all
      if (type.equals("Metric")) {
        // we need to get the type of the thing we hang on

        if (at.kind().equals(AnnotationTarget.Kind.FIELD)) {
          String aType = at.asField().type().name().local();
          m = getMetricInstanceFromType(aType);
          metadata = new Metadata(name,getMetricType(aType));
        } else if (at.kind().equals(AnnotationTarget.Kind.METHOD)) {
          m = new GaugeImpl(); // TODO wrong
        } else {
          m = new GaugeImpl(); // TODO wrong
        }


      } else {
        m = getMetricInstanceFromType(type);
        metadata = new Metadata(name,getMetricType(type));
      }

      if (unit != null) {
        metadata.setUnit(unit);
      }

      MetricRegistryFactory.getApplicationRegistry().register(name, m, metadata);

    }
  }

  private Metric getMetricInstanceFromType(String type) {
    Metric m;
    switch (type) {
      case "Gauge":
        m = new GaugeImpl();
        break;
      case "Counter":
      case "Counted":
        m = new CounterImpl();
        break;

      case "Metered":
        m = new MeterImpl();
        break;
      case "Timed":
        m = new TimerImpl();
        break;

      default:
        throw new IllegalStateException("Unknown type " + type);
    }
    return m;
  }

  private MetricType getMetricType(String type) {
    MetricType out;

    switch (type) {
      case "Gauge":
        out = MetricType.GAUGE;
        break;
      case "Counter":
      case "Counted":
        out = MetricType.COUNTER;
        break;

      case "Metered":
        out = MetricType.METERED;
        break;
      case "Timed":
        out = MetricType.TIMER;
        break;
      default:
        throw new IllegalStateException("Unknown type " + type);
    }
    return out;
  }

  private String getNameFromTarget(AnnotationTarget target) {
    AnnotationTarget.Kind kind = target.kind();
    String name;
    switch (kind) {
      case METHOD:
        MethodInfo mi = target.asMethod();
        name = mi.name();
        break;
      case TYPE:
      case CLASS:
        ClassInfo ci = target.asClass();
        name = ci.simpleName();
        break;
      case FIELD:
        FieldInfo fi = target.asField();
        name = fi.name();
        break;
      default:
        name = "-dummy-";
    }
    return name;
  }
}
