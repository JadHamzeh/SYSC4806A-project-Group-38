package perk.manager;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an application user in the system.
 *
 * This entity is stored in the "users" table and maintains:
 *  - Login credentials (username and password)
 *  - Memberships the user belongs to
 *  - Perks created by this user
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Primary key for the User entity.
     * Uses auto-increment identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** Username used for authentication. */
    private String username;

    /** Password used for authentication (should be stored hashed). */
    private String password;

    /**
     * List of memberships the user is associated with.
     *
     * - mappedBy = "user": UserMembership has a 'user' field that owns the relationship.
     * - CascadeType.ALL: membership rows will be updated/deleted when the user changes.
     * - orphanRemoval = true: removing a membership from this list deletes it from the DB.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMembership> memberships = new ArrayList<>();

    /**
     * List of perks created by this user.
     *
     * This relationship is owned by the Perk entity (via its 'createdBy' field).
     * No cascading is configured to avoid unintended deletion.
     */
    @OneToMany(mappedBy = "createdBy")
    private List<Perk> perks = new ArrayList<>();

    /** Default constructor required by JPA. */
    public User() {}

    /**
     * Convenience constructor to create a user with credentials.
     */
    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    // Getters and setters

    public Long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<UserMembership> getMemberships() { return memberships; }
    public void setMemberships(List<UserMembership> memberships) { this.memberships = memberships; }

    public List<Perk> getPerks() { return perks; }
    public void setPerks(List<Perk> perks) { this.perks = perks; }
}
