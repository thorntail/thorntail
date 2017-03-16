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
package org.wildfly.swarm.teiid;

public interface TeiidProperties {

    int DEFAULT_JDBC_PORT = 31000;
    int DEFAULT_ODBC_PORT = 35432;

    String JDBC_SOCKET_BINDING_NAME = "teiid-jdbc";
    String ODBC_SOCKET_BINDING_NAME = "teiid-odbc";

    String JDBC_TRANSPORT_NAME = "jdbc";
    String ODBC_TRANSPORT_NAME = "odbc";

    String PREPARED_INFINISPAN_CACHE_CONTAINER_NAME = "teiid-cache";
    String RESULTSET_INFINISPAN_CACHE_CONTAINER_NAME = "teiid-cache";
    String PREPARED_INFINISPAN_CACHE_NAME = "preparedplan";
    String RESULTSET_INFINISPAN_CACHE_NAME = "resultset";
    String RESULTSET_REPL_INFINISPAN_CACHE_NAME = "resultset-repl";
}
