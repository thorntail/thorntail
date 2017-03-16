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

import java.io.IOException;
import java.io.InputStream;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Assignable;

/**
 * Provide a way to deploy vdb xml
 * @author kylin
 *
 */
public interface VDBArchive extends Assignable, Archive<VDBArchive> {

    /**
     * Form a VDBArchive by a -vdb.xml name, the -vdb.xml either can be under class path, or under current folder
     * @param name
     * @return
     */
    VDBArchive vdb(String name) throws IOException;

    /**
     * Form a VDBArchive by a -vdb.xml by InputStream
     * @param in
     * @return
     */
    VDBArchive vdb(InputStream in) throws IOException;
}
