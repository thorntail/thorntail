package org.wildfly.swarm.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/jsonMapping")
public class JsonMappingResource {

   @GET
   @Produces("application/json")
   public JsonMapperResponse get() {
      return new JsonMapperResponse();
   }

   public static class JsonMapperResponse {
      public String getSnakeCase() {
         return "";
      }

      public String getNullObject() {
         return null;
      }
   }
}
