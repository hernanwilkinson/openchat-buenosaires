package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestObjectsBucket {
    public static final String PEPE_SANCHEZ_NAME = "Pepe Sanchez";
    public static final String PEPE_SANCHEZ_PASSWORD = "password";
    public static final String PEPE_SANCHEZ_ABOUT = "anotherAbout";
    public static final String PEPE_SANCHEZ_HOME_PAGE = "www.twitter.com/pepeSanchez";

    public static final String JUAN_PEREZ_NAME = "Juan Perez";
    public static final String JUAN_PEREZ_PASSWORD = "otherPassword";
    public static final String JUAN_PEREZ_ABOUT = "about";
    public static final String JUAN_PEREZ_HOME_PAGE = "www.twitter.com/juanPerez";

    private final LocalDateTime now = LocalDateTime.now();

    public static <T extends Throwable> void assertThrowsModelExceptionWithErrorMessage(
            Executable closureToFail, String errorMessage) {
        ModelException error = assertThrows(
                ModelException.class,
                closureToFail);

        assertEquals(errorMessage,error.getMessage());
    }

    public User createUserJuanPerez() {
        return User.named(
                JUAN_PEREZ_NAME,
                JUAN_PEREZ_ABOUT,
                JUAN_PEREZ_HOME_PAGE);
    }

    public User createPepeSanchez() {
        return User.named(
                PEPE_SANCHEZ_NAME,
                PEPE_SANCHEZ_ABOUT,
                PEPE_SANCHEZ_HOME_PAGE);
    }

    public Clock fixedNowClock(){
        return ()-> now;
    }

    public JsonObject juanPerezLoginBodyAsJson() {
        return new JsonObject()
                .add(RestReceptionist.USERNAME_KEY, JUAN_PEREZ_NAME)
                .add(RestReceptionist.PASSWORD_KEY, JUAN_PEREZ_PASSWORD);
    }

    public JsonObject juanPerezRegistrationBodyAsJson() {
        return juanPerezLoginBodyAsJson()
                .add(RestReceptionist.ABOUT_KEY, JUAN_PEREZ_ABOUT)
                .add(RestReceptionist.HOME_PAGE_KEY, JUAN_PEREZ_HOME_PAGE);
    }
}
