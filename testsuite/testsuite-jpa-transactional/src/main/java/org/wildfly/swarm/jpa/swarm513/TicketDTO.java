package org.wildfly.swarm.jpa.swarm513;

/**
 * @author Heiko Braun
 * @since 15/06/16
 */
import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TicketDTO implements Serializable
{

   private Long id;
   private float price;

   public TicketDTO()
   {
   }

   public TicketDTO(long id, float price)
   {

      this.id = id;
      this.price = price;
   }

   public Ticket fromDTO(Ticket entity, EntityManager em)
   {
      if (entity == null)
      {
         entity = new Ticket();
      }
      entity.setPrice(this.price);
      entity = em.merge(entity);
      return entity;
   }

   public Long getId()
   {
      return this.id;
   }

   public void setId(final Long id)
   {
      this.id = id;
   }

   public float getPrice()
   {
      return this.price;
   }

   public void setPrice(final float price)
   {
      this.price = price;
   }

}