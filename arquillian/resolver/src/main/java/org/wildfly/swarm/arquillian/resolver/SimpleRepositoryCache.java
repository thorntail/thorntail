package org.wildfly.swarm.arquillian.resolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.aether.RepositoryCache;
import org.eclipse.aether.RepositorySystemSession;

/**
 * A simplistic repository cache backed by a thread-safe map. The simplistic nature of this cache makes it only suitable
 * for use with short-lived repository system sessions where pruning of cache data is not required.
 */
public final class SimpleRepositoryCache
    implements RepositoryCache
{

    private final Map<Object, Object> cache = new ConcurrentHashMap<Object, Object>( 256 );

    public Object get(RepositorySystemSession session, Object key )
    {
        return cache.get( key );
    }

    public void put( RepositorySystemSession session, Object key, Object data )
    {
        if ( data != null )
        {
            cache.put( key, data );
        }
    }

}
