package bsas.org.openchat;

import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestObjectsBucket {
    public static final String PEPE_SANCHEZ_NAME = "Pepe Sanchez";
    public static final String PEPE_SANCHEZ_PASSWORD = "password";
    public static final String JUAN_PEREZ_NAME = "Juan Perez";
    public static final String JUAN_PEREZ_PASSWORD = "otherPassword";
    public static final String JUAN_PEREZ_ABOUT = "about";
    public static final String PEPE_SANCHEZ_ABOUT = "anotherAbout";

    public static <T extends Throwable> void assertThrowsWithErrorMessage(
            Class<T> expectedType, Executable closureToFail, String errorMessage) {
        T error = assertThrows(
                expectedType,
                closureToFail);

        assertEquals(errorMessage,error.getMessage());
    }

    public User createUserJuanPerez() {
        return User.named(JUAN_PEREZ_NAME, JUAN_PEREZ_PASSWORD,"about");
    }

    public User createPepeSanchez() {
        return User.named(PEPE_SANCHEZ_NAME, PEPE_SANCHEZ_PASSWORD,"about");
    }

    public Clock fixedNowClock(){
        final LocalDateTime now = LocalDateTime.now();
        return ()->now;
    }
}
