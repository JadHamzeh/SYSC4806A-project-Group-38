package perk.manager;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String password; //hash it later

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMembership> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy")
    private List<Perk> perks = new ArrayList<>();

    public User(){}

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    //getters setters
    public Long getId() { return id; }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<UserMembership> getMemberships() { return memberships; }
    public void setMemberships(List<UserMembership> memberships) { this.memberships = memberships; }

    public List<Perk> getPerks() { return perks; }
    public void setPerks(List<Perk> perks) { this.perks = perks; }

}
