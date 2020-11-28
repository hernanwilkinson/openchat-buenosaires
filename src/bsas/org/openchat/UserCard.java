package bsas.org.openchat;

import com.sun.istack.NotNull;

import javax.persistence.*;
import java.util.stream.Stream;

@Entity
@Table(name="USERS")
class UserCard {

    @Id
    @GeneratedValue
    private long pid;
    @Embedded
    private User user;
    @NotNull
    private String password;
    @OneToOne
    private Publisher publisher;

    public UserCard(User user, String password, Publisher publisher) {
        this.user = user;
        this.password = password;
        this.publisher = publisher;
    }

    public UserCard() {
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
