package bsas.org.openchat;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name="USERS")
public class User {
    public static final String NAME_CANNOT_BE_BLANK = "Name can not be blank";

    @Id
    @GeneratedValue
    private long id;
    @Column(unique = true)
    private String name;
    @Basic(fetch = FetchType.LAZY)
    private String about;
    @Basic(fetch = FetchType.LAZY)
    private String homePage;
    @Column(unique = true)
    private String restId;

    public static User named(String name, String about, String homePage) {
        return named(name, about, homePage, UUID.randomUUID().toString());
    }

    public static User named(String name, String about, String homePage, String restId) {
        assertNameIsNotBlank(name);

        return new User(name, about,homePage,restId);
    }

    static void assertNameIsNotBlank(String name) {
        if(name.isBlank()) throw new ModelException(NAME_CANNOT_BE_BLANK);
    }

    public User(){
    }

    private User(String name, String about, String homePage, String restId) {
        this.name = name;
        this.about = about;
        this.homePage = homePage;
        this.restId = restId;
    }

    public boolean isNamed(String potentialName) {
        return name.equals(potentialName);
    }

    public String name() {
        return name;
    }

    public String about() {
        return about;
    }

    public String homePage() {
        return homePage;
    }

    public String restId() {
        return restId;
    }
}