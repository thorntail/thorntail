/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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

public interface JoseProperties {

    String DEFAULT_KEYSTORE_TYPE = "jks";
    String DEFAULT_KEYSTORE_PATH = "application.keystore";
    String DEFAULT_KEYSTORE_PASSWORD = "password";
    String DEFAULT_KEY_PASSWORD = "password";
    String DEFAULT_KEY_ALIAS = "server";

    String DEFAULT_SIGNATURE_ALGORITHM = "RS256";
    boolean DEFAULT_SIGNATURE_DATA_ENCODING = true;
    boolean DEFAULT_SIGNATURE_DATA_DETACHED = false;
    String DEFAULT_KEY_ENCRYPTION_ALGORITHM = "RSA-OAEP";
    String DEFAULT_CONTENT_ENCRYPTION_ALGORITHM = "A128GCM";
    boolean DEFAULT_INCLUDE_ENCRYPTION_KEY_ALIAS = true;
    boolean DEFAULT_INCLUDE_SIGNATURE_KEY_ALIAS = true;
    boolean DEFAULT_ACCEPT_DECRYPTION_ALIAS = false;
    boolean DEFAULT_ACCEPT_VERIFICATION_ALIAS = false;

    JoseFormat DEFAULT_JOSE_FORMAT = JoseFormat.COMPACT;
}
