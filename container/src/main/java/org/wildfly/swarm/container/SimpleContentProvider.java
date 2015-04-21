package org.wildfly.swarm.container;

import org.jboss.as.selfcontained.ContentProvider;
import org.jboss.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class SimpleContentProvider implements ContentProvider {

    private List<VirtualFile> contents = new ArrayList<>();


    public SimpleContentProvider() {

    }

    public synchronized byte[] addContent(VirtualFile content) {
        this.contents.add( content );
        byte[] hash = new byte[1];
        hash[0] = (byte) (this.contents.size() - 1);
        return hash;
    }

    @Override
    public VirtualFile getContent(int index) {
        if ( index >= this.contents.size() ) {
            return null;
        }

        return this.contents.get( index );
    }
}
