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
package org.wildfly.swarm.jose;

import java.util.Collections;
import java.util.Map;

public class DecryptedData {
    private Map<String, Object> metadata;
    private String data;
    public DecryptedData(String data) {
        this(Collections.emptyMap(), data);
    }
    public DecryptedData(Map<String, Object> metadata, String data) {
        this.metadata = metadata;
        this.data = data;
    }
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    public String getData() {
        return data;
    }
}
