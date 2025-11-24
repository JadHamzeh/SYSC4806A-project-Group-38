package perk.manager;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Represents a type or category of membership that can be assigned to users.
 *
 * Examples of membership types might include "CAA", "Costco", or "Scene+".
 *
 * This entity will be prepopulated in the database during application
 * startup to provide default membership options.
 */
@Entity
public class MembershipType {

    /**
     * Automatically generate a unique ID for the membership type.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name; // prepopulate a database on launch/deployment

    public MembershipType() {}

    /**
     * Constructs a new MembershipType with the specified name.
     *
     * @param name the name of the membership type.
     */
    public MembershipType(String name) {
        this.name = name;
    }

    /**
     * @return the ID of this membership type
     */
    public Long getId(){return id;}

    /**
     * Sets the ID for this membership type.
     *
     * @param id the ID to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the name of the membership type
     */
    public String getName(){return name;}

    /**
     * Sets the name of this membership type.
     *
     * @param name the new name for this membership type
     */
    public void setName(String name){this.name = name;}
}
