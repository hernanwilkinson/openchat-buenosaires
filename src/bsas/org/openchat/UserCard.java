package bsas.org.openchat;

import javax.persistence.*;
import java.util.stream.Stream;

@Entity
@Table(name="USERS")
class UserCard {

    @Embedded
    private User user;
    private String password;
    @OneToOne
    private Publisher publisher;

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
