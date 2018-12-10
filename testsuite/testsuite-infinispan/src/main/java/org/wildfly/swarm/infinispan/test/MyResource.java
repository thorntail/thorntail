package org.wildfly.swarm.infinispan.test;

import java.util.UUID;

import javax.naming.InitialContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * @author mwolf
 */
@Path("/")
public class MyResource {

    private final String key = "myKey";

    @GET
    @Produces("text/plain")
    public String get() throws Exception {
        EmbeddedCacheManager cacheContainer
                = (EmbeddedCacheManager) new InitialContext().lookup("java:jboss/infinispan/container/server");
        Cache<String,String> cache = cacheContainer.getCache("default");
        if (cache.keySet().contains(key)) {
            return (String) cache.get(key);
        }

        String result = UUID.randomUUID().toString();
        cache.put(key, result);
        return result;
    }

}