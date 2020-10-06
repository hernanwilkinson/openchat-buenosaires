import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestXX {

    public static final String PEPE_SANCHEZ_NAME = "Pepe Sanchez";
    public static final String PEPE_SANCHEZ_PASSWORD = "password";

    @Test
    public void publisherCanNotHaveBlankName() {
        RuntimeException error = assertThrows(
                RuntimeException.class,
                ()->Publisher.named(" ","password","about"));

        assertEquals(Publisher.INVALID_NAME,error.getMessage());
    }
    @Test
    public void canCreatePublisherWithNoBlankName() {
        Publisher createdPublisher = createPepeSanchez();

        assertTrue(createdPublisher.isNamed(PEPE_SANCHEZ_NAME));
    }

    @Test
    public void isNamedReturnsFalseWhenAskedWithOtherName() {
        Publisher createdPublisher = createPepeSanchez();

        assertFalse(createdPublisher.isNamed("Juan"));
    }
    private Publisher createPepeSanchez() {
        return Publisher.named(PEPE_SANCHEZ_NAME, PEPE_SANCHEZ_PASSWORD,"about");
    }

}