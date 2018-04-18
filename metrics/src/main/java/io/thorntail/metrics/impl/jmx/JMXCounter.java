/*
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
 * /
 */
package io.thorntail.metrics.impl.jmx;

import org.eclipse.microprofile.metrics.Counter;

/**
 * @author hrupp
 */
public class JMXCounter implements Counter {
    private static final String MUST_NOT_BE_CALLED = "Must not be called";


    public JMXCounter(JMXHelper helper, String mbeanExpression) {
        this.helper = helper;
        this.mbeanExpression = mbeanExpression;
    }

    @Override
    public void inc() {
        throw new IllegalStateException(MUST_NOT_BE_CALLED);
    }

    @Override
    public void inc(long n) {
        throw new IllegalStateException(MUST_NOT_BE_CALLED);
    }

    @Override
    public void dec() {
        throw new IllegalStateException(MUST_NOT_BE_CALLED);
    }

    @Override
    public void dec(long n) {
        throw new IllegalStateException(MUST_NOT_BE_CALLED);
    }

    @Override
    public long getCount() {
        return this.helper.getValue(mbeanExpression).longValue();
    }

    private final JMXHelper helper;

    private String mbeanExpression;
}
