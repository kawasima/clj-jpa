package cljjpa.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author kawasima
 */
@Entity
public class User implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    private String familyName;
    private String lastName;

    private String emailAddress;

    @OneToMany(mappedBy = "user")
    private List<Membership> memberships;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "MEMBERSHIP",
            joinColumns = {@JoinColumn(name = "USER_ID")},
            inverseJoinColumns = {@JoinColumn(name = "GROUP_ID")}
    )
    private List<Group> groups;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<Membership> memberships) {
        this.memberships = memberships;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
