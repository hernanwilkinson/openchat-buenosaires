import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpenChatSystemTest {
    @Test
    public void createSystemHasNoUsers() {
        OpenChatSystem system = new OpenChatSystem();

        assertFalse(system.hasUsers());
    }
    @Test
    public void canRegisterUser() {
        OpenChatSystem system = new OpenChatSystem();

        system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about");

        assertTrue(system.hasUsers());
        assertTrue(system.hasUserNamed(PublisherTest.PEPE_SANCHEZ_NAME));
        assertEquals(1,system.numberOfUsers());
    }
}