package perk.manager;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Perk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String description;
    private String region;
    private LocalDate expiryDate;
    private int votes = 0;

    @ManyToOne
    private MembershipType membershipType;

    @ManyToOne
    private User createdBy;

    public Perk() {}

    public Perk(String title, String description, String region, LocalDate expiryDate, MembershipType membershipType, User createdBy) {
        this.title = title;
        this.description = description;
        this.region = region;
        this.expiryDate = expiryDate;
        this.membershipType = membershipType;
        this.createdBy = createdBy;
    }

    public Long getId() {return id;}

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
