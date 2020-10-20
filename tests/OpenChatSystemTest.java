import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpenChatSystemTest {
    @Test
    public void createSystemHasNoUsers() {
        OpenChatSystem system = new OpenChatSystem();

        assertFalse(system.hasUsers());
        assertFalse(system.hasUserNamed(PublisherTest.PEPE_SANCHEZ_NAME));
        assertEquals(0,system.numberOfUsers());
    }
    @Test
    public void canRegisterUser() {
        OpenChatSystem system = new OpenChatSystem();

        system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about");

        assertTrue(system.hasUsers());
        assertTrue(system.hasUserNamed(PublisherTest.PEPE_SANCHEZ_NAME));
        assertEquals(1,system.numberOfUsers());
    }
    @Test
    public void canRegisterManyUsers() {
        OpenChatSystem system = new OpenChatSystem();

        system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about");
        system.register(PublisherTest.JUAN_PEREZ_NAME,PublisherTest.JUAN_PEREZ_PASSWORD,"about");

        assertTrue(system.hasUsers());
        assertTrue(system.hasUserNamed(PublisherTest.PEPE_SANCHEZ_NAME));
        assertTrue(system.hasUserNamed(PublisherTest.JUAN_PEREZ_NAME));
        assertEquals(2,system.numberOfUsers());
    }
    @Test
    public void canNotRegisterSameUserTwice() {
        OpenChatSystem system = new OpenChatSystem();

        system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about");

        PublisherTest.assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about"),
                OpenChatSystem.CANNOT_REGISTER_SAME_USER_TWICE);

        assertTrue(system.hasUsers());
        assertTrue(system.hasUserNamed(PublisherTest.PEPE_SANCHEZ_NAME));
        assertEquals(1,system.numberOfUsers());
    }
    @Test
    public void canWorkWithAuthenticatedUser() {
        OpenChatSystem system = new OpenChatSystem();
        system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about");

        system.withAuthenticatedUserDo(
                PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,
                user->assertTrue(user.isNamed(PublisherTest.PEPE_SANCHEZ_NAME)),
                ()->fail());
    }
    @Test
    public void notRegisteredUserIsNotAuthenticated() {
        OpenChatSystem system = new OpenChatSystem();
        final boolean[] notAuthenticated = {false};

        system.withAuthenticatedUserDo(
                PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,
                user->fail(),
                ()-> notAuthenticated[0] = true);

        assertTrue(notAuthenticated[0]);
    }

}