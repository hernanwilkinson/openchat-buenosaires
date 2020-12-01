package bsas.org.openchat;

import com.sun.istack.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class User {
    public static final String NAME_CANNOT_BE_BLANK = "Name can not be blank";

    @NotNull
    private String id;
    @NotNull
    @Column(unique = true)
    private String name;
    @NotNull
    private String about;
    @NotNull
    private String homePage;

    public User() {
    }

    public static User named(String name, String about, String homePage) {
        assertNameIsNotBlank(name);

        return new User(name, about,homePage);
    }

    static void assertNameIsNotBlank(String name) {
        if(name.isBlank()) throw new ModelException(NAME_CANNOT_BE_BLANK);
    }

    private User(String name, String about, String homePage) {
        this(name, about, homePage, UUID.randomUUID().toString());
    }

    private User(String name, String about, String homePage, String id) {
        this.name = name;
        this.about = about;
        this.homePage = homePage;
        this.id = id;
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

    public String id() {
        return id;
    }
}