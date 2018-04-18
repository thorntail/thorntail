/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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
package io.thorntail.metrics.impl.jmx;

import java.util.Map;

/**
 * @author hrupp
 */
@SuppressWarnings("unused")
public class Tag {
    String key;

    String value;

    public Tag() {
    }

    public Tag(String kvString) {
        if (kvString == null || kvString.isEmpty() || !kvString.contains("=")) {
            throw new IllegalArgumentException("Not a k=v pair: " + kvString);
        }
        String[] kv = kvString.split("=");
        if (kv.length != 2) {
            throw new IllegalArgumentException("Not a k=v pair: " + kvString);
        }
        key = kv[0].trim();
        value = kv[1].trim();
    }

    public Tag(String key, String value) {
        this.key = key.trim();
        this.value = value.trim();
    }

    public Tag(Map<String, String> tag) {
        this(tag.get("key"), tag.get("value"));
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tag tag = (Tag) o;

        if (!key.equals(tag.key)) {
            return false;
        }
        return value.equals(tag.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Tag{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String toKVString() {
        final StringBuilder sb = new StringBuilder(key);
        sb.append('=');
        sb.append(value);

        return sb.toString();
    }
}
