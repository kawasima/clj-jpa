package cljjpa.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author kawasima
 */
@Entity
public class Membership implements Serializable {
    @Id
    @ManyToOne
    private User user;

    @Id
    @ManyToOne
    private Group group;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
