package org.wildfly.swarm.jpa.swarm513;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * @author Heiko Braun
 * @since 15/06/16
 */
@Stateless
@Path("/tickets")
public class TicketEndpoint {

    @PersistenceContext(unitName = "primary")
    private EntityManager em;

    // If Hibernate fraction fails to add Hibernate dependency to application, session variable will be null
    @PersistenceContext(unitName = "primary")
    private org.hibernate.Session session;

    // Ensure that TicketEndpoint can be compiled with (Envers) AuditQuery class reference, no runtime check is needed.
    private org.hibernate.envers.query.AuditQuery auditQuery;

    @POST
    @Consumes("text/xml")
    public Response create(TicketDTO dto) {
        Ticket entity = dto.fromDTO(null, em);
        em.persist(entity);
        if (session == null) {
            throw new RuntimeException("Hibernate fraction failed to inject persistence context into org.hibernate.Session");
        }
        return Response.created(UriBuilder.fromResource(TicketEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
    }

    @GET
    @Produces({"text/xml", "application/json"})
    public List<TicketDTO> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult) {
        TypedQuery<Ticket> findAllQuery = em.createQuery("SELECT DISTINCT t FROM Ticket t ORDER BY t.id", Ticket.class);
        if (startPosition != null) {
            findAllQuery.setFirstResult(startPosition);
        }
        if (maxResult != null) {
            findAllQuery.setMaxResults(maxResult);
        }
        final List<Ticket> searchResults = findAllQuery.getResultList();
        final List<TicketDTO> results = new ArrayList<TicketDTO>();
        for (Ticket searchResult : searchResults) {
            TicketDTO dto = new TicketDTO(searchResult.getId(), searchResult.getPrice());
            results.add(dto);
        }
        return results;
    }
}
