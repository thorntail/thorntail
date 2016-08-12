package org.wildfly.swarm.jpa.swarm513;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * <p>
 * A ticket represents a seat sold for a particular price.
 * </p>
 *
 * @author Shane Bryzak
 * @author Marius Bogoevici
 * @author Pete Muir
 */
/*
 * We suppress the warning about not specifying a serialVersionUID, as we are
 * still developing this app, and want the JVM to generate the serialVersionUID
 * for us. When we put this app into production, we'll generate and embed the
 * serialVersionUID
 */
@SuppressWarnings("serial")
@Entity
public class Ticket implements Serializable {

	/* Declaration of fields */

	/**
	 * The synthetic id of the object.
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;


	/**
	 * The price which was charged for the ticket.
	 */
	private float price;

	/** No-arg constructor for persistence */
	public Ticket() {

	}

	public Ticket(float price) {
		this.price = price;
	}

	/* Boilerplate getters and setters */

	public Long getId() {
		return id;
	}


	public float getPrice() {
		return price;
	}

    public void setId(Long id) {
		this.id = id;
	}

	public void setPrice(float price) {
		this.price = price;
	}

}