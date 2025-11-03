package perk.manager;

import jakarta.persistence.*;

/**
 * Represents a user's membership. Linking each user to a unique ID.
 */
@Entity
public class UserMembership {

    /**
     * Automatically generate a unique ID for the user's membership.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    /**
     * Represents a many-to-one relationship since multiple
     * membership records can belong to the same user.
     */
    @ManyToOne
    private User user;

    /**
     * Represents a many-to-one relationship since multiple
     * users can have the same membership type.
     */
    @ManyToOne
    private MembershipType membershipType;

    public UserMembership(){}

    /**
     * Make a new user membership with the specified user & membership type.
     *
     * @param user the user associated with this membership.
     * @param membershipType the type of membership assigned to the user.
     */
    public UserMembership(User user, MembershipType membershipType){
        this.user = user;
        this.membershipType = membershipType;
    }

    /**
     * @return ID associated with the user membership.
     */
    public long getId() {return Id;}

    /**
     * @return the user object of the user membership.
     */
    public User getUser() {return user;}

    /**
     * Set the user object to the user membership.
     * @param user the user chosen to be set to.
     */
    public void setUser(User user) {this.user = user;}

    /**
     * @return the membership type of the user membership.
     */
    public MembershipType getMembershipType() {return membershipType;}

    /**
     * Set the membership type to the user membership.
     * @param membershipType the type of membership to be set.
     */
    public void setMembershipType(MembershipType membershipType) {this.membershipType = membershipType;}
}
