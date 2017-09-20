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
package org.wildfly.swarm.microprofile_metrics.runtime.mp;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.metrics.Snapshot;

/**
 * @author hrupp
 */
public class TimerImpl implements org.eclipse.microprofile.metrics.Timer {
  @Override
  public void update(long duration, TimeUnit unit) {
    // TODO: Customise this generated block
  }

  @Override
  public <T> T time(Callable<T> event) throws Exception {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public void time(Runnable event) {
    // TODO: Customise this generated block
  }

  @Override
  public Context time() {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public long getCount() {
    return 0;  // TODO: Customise this generated block
  }

  @Override
  public double getFifteenMinuteRate() {
    return 0;  // TODO: Customise this generated block
  }

  @Override
  public double getFiveMinuteRate() {
    return 0;  // TODO: Customise this generated block
  }

  @Override
  public double getMeanRate() {
    return 0;  // TODO: Customise this generated block
  }

  @Override
  public double getOneMinuteRate() {
    return 0;  // TODO: Customise this generated block
  }

  @Override
  public Snapshot getSnapshot() {
    return null;  // TODO: Customise this generated block
  }
}
