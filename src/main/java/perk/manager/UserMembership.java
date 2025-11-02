package perk.manager;

import jakarta.persistence.*;

@Entity
public class UserMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    @ManyToOne
    private User user;

    @ManyToOne
    private MembershipType membershipType;

    public UserMembership(){}

    public UserMembership(User user, MembershipType membershipType){
        this.user = user;
        this.membershipType = membershipType;
    }

    public long getId() {return Id;}

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    public MembershipType getMembershipType() {return membershipType;}
    public void setMembershipType(MembershipType membershipType) {this.membershipType = membershipType;}
}
