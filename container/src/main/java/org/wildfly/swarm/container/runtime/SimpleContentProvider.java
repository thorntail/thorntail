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
package org.wildfly.swarm.container.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Vetoed;

import org.jboss.as.selfcontained.ContentProvider;
import org.jboss.vfs.VirtualFile;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class SimpleContentProvider implements ContentProvider {

    public SimpleContentProvider() {

    }

    public synchronized byte[] addContent(VirtualFile content) {
        this.contents.add(content);
        byte[] hash = new byte[1];
        hash[0] = (byte) (this.contents.size() - 1);
        return hash;
    }

    @Override
    public VirtualFile getContent(int index) {
        if (index >= this.contents.size()) {
            return null;
        }

        return this.contents.get(index);
    }

    private List<VirtualFile> contents = new ArrayList<>();
}
