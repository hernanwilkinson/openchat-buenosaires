package bsas.org.openchat;

import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestObjectsBucket {
    public static final String PEPE_SANCHEZ_NAME = "Pepe Sanchez";
    public static final String PEPE_SANCHEZ_PASSWORD = "password";
    public static final String PEPE_SANCHEZ_ABOUT = "anotherAbout";
    public static final String PEPE_SANCHEZ_URL = "www.twitter.com/pepeSanchez";

    public static final String JUAN_PEREZ_NAME = "Juan Perez";
    public static final String JUAN_PEREZ_PASSWORD = "otherPassword";
    public static final String JUAN_PEREZ_ABOUT = "about";
    public static final String JUAN_PEREZ_URL = "www.twitter.com/juanPerez";

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
                JUAN_PEREZ_PASSWORD,
                JUAN_PEREZ_ABOUT,
                JUAN_PEREZ_URL);
    }

    public User createPepeSanchez() {
        return User.named(
                PEPE_SANCHEZ_NAME,
                PEPE_SANCHEZ_PASSWORD,
                PEPE_SANCHEZ_ABOUT,
                PEPE_SANCHEZ_URL);
    }

    public Clock fixedNowClock(){
        return ()-> now;
    }
}
