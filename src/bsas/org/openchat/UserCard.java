package bsas.org.openchat;

import javax.persistence.*;
import java.util.stream.Stream;

@Entity
@Table(name="USERCARDS")
class UserCard {

    @Id
    @GeneratedValue
    private long id;
    @OneToOne
    private User user;
    @Basic(fetch = FetchType.LAZY)
    private String password;
    @OneToOne
    @Basic(fetch = FetchType.LAZY)
    private Publisher publisher;

    public UserCard(){}

    public UserCard(User user, String password, Publisher publisher) {
        this.user = user;
        this.password = password;
        this.publisher = publisher;
    }

    public static UserCard of(User user, String password, Publisher publisher) {
        return new UserCard(user, password, publisher);
    }

    public User user() {
        return user;
    }

    public boolean isPassword(String potentialPassword) {
        return password.equals(potentialPassword);
    }

    public Publisher publisher() {
        return publisher;
    }

    public Stream<Publication> publications() {
        return publisher.publications();
    }

    public boolean isUserNamed(String potentialUserName) {
        return user.isNamed(potentialUserName);
    }
}
