package perk.manager;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Represents a perk available to be viewed by users.
 *
 * A perk might include discounts, exclusive offers, or special access privileges.
 * Each perk can be limited by region, have an expiration date, and may require a specific MembershipType.
 */
@Entity
public class Perk {

    /**
     * Automatically generate a unique ID for the perk.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String description;
    private String region;
    private LocalDate expiryDate;
    private int votes = 0;

    /**
     * The membership type required to access this perk.
     * Represents a many-to-one relationship as multiple
     * perks can belong to the same MembershipType.
     */
    @ManyToOne
    private MembershipType membershipType;

    /**
     * The user who created this perk.
     * Represents a many-to-one relationship as multiple
     * perks can be created by the same user.
     */
    @ManyToOne
    private User createdBy;

    public Perk() {}

    /**
     * Constructs a new Perk with the specified details.
     *
     * @param title           the title of the perk
     * @param description     a detailed description of the perk
     * @param region          the region where the perk is available
     * @param expiryDate      the date when the perk expires
     * @param membershipType  the membership type required to access the perk
     * @param createdBy       the user who created the perk
     */
    public Perk(String title, String description, String region, LocalDate expiryDate, MembershipType membershipType, User createdBy) {
        this.title = title;
        this.description = description;
        this.region = region;
        this.expiryDate = expiryDate;
        this.membershipType = membershipType;
        this.createdBy = createdBy;
    }

    public Long getId() {return id;}

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public String getRegion() {return region;}
    public void setRegion(String region) {this.region = region;}

    public LocalDate getExpiryDate() {return expiryDate;}
    public void setExpiryDate(LocalDate expiryDate) {this.expiryDate = expiryDate;}

    public int getVotes() {return votes;}
    public void setVotes(int votes) {this.votes = votes;}

    public MembershipType getMembershipType() {return membershipType;}
    public void setMembershipType(MembershipType membershipType) {this.membershipType = membershipType;}

    public User getCreatedBy() {return createdBy;}
    public void setCreatedBy(User createdBy) {this.createdBy = createdBy;}
}
