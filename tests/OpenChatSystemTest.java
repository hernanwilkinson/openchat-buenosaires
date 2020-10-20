import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OpenChatSystemTest {

    private OpenChatSystem system;

    @Test
    public void createSystemHasNoUsers() {
        system = createSystem();

        assertFalse(system.hasUsers());
        assertFalse(system.hasUserNamed(PublisherTest.PEPE_SANCHEZ_NAME));
        assertEquals(0, system.numberOfUsers());
    }
    @Test
    public void canRegisterUser() {
        system = createSystem();
        registerPepeSanchez();

        assertTrue(system.hasUsers());
        assertTrue(system.hasUserNamed(PublisherTest.PEPE_SANCHEZ_NAME));
        assertEquals(1,system.numberOfUsers());
    }
    @Test
    public void canRegisterManyUsers() {
        system = createSystem();
        registerPepeSanchez();
        registerJuanPerez();

        assertTrue(system.hasUsers());
        assertTrue(system.hasUserNamed(PublisherTest.PEPE_SANCHEZ_NAME));
        assertTrue(system.hasUserNamed(PublisherTest.JUAN_PEREZ_NAME));
        assertEquals(2,system.numberOfUsers());
    }
    @Test
    public void canNotRegisterSameUserTwice() {
        system = createSystem();
        registerPepeSanchez();

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
        system = createSystem();
        registerPepeSanchez();

        final Object token = createSystem();
        final Object authenticatedToken = system.withAuthenticatedUserDo(
                PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,
                user->token,
                ()->fail());

        assertEquals(token,authenticatedToken);
    }
    @Test
    public void notRegisteredUserIsNotAuthenticated() {
        system = createSystem();
        assertCanNotAuthenticateWith(system, PublisherTest.PEPE_SANCHEZ_PASSWORD);
    }
    @Test
    public void canNotAuthenticateWithInvalidPassword() {
        system = createSystem();
        registerPepeSanchez();

        assertCanNotAuthenticateWith(system, PublisherTest.PEPE_SANCHEZ_PASSWORD+"something");
    }
    @Test
    public void registeredUserCanPublish() {
        system = createSystem();
        registerPepeSanchez();

        Publication publication = system.publishForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME,"hello");

        List<Publication> timeLine = system.timeLineForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME);
        assertEquals(Arrays.asList(publication),timeLine);
    }
    @Test
    public void noRegisteredUserCanNotPublish() {
        system = createSystem();

        PublisherTest.assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->system.publishForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME,"hello"),
                OpenChatSystem.USER_NOT_REGISTERED);
    }
    @Test
    public void noRegisteredUserCanAskItsTimeline() {
        system = createSystem();

        PublisherTest.assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->system.timeLineForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME),
                OpenChatSystem.USER_NOT_REGISTERED);
    }
    @Test
    public void canFollowRegisteredUser() {
        system = createSystem();
        registerPepeSanchez();
        User followee = registerJuanPerez();

        system.followForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.JUAN_PEREZ_NAME);

        List<User> followees = system.followeesOfUserNamed(PublisherTest.PEPE_SANCHEZ_NAME);
        assertEquals(Arrays.asList(followee),followees);
    }
    @Test
    public void canGetWallOfRegisteredUser() {
        system = createSystem();
        registerPepeSanchez();
        registerJuanPerez();
        system.followForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.JUAN_PEREZ_NAME);

        Publication followerPublication = system.publishForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME,"hello");
        Publication followeePublication = system.publishForUserNamed(PublisherTest.JUAN_PEREZ_NAME,"bye");

        List<Publication> wall = system.wallForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME);
        assertEquals(Arrays.asList(followerPublication,followeePublication),wall);
    }

    private void assertCanNotAuthenticateWith(OpenChatSystem system, String password) {
        final Object token = createSystem();
        final Object notAuthenticatedToken = system.withAuthenticatedUserDo(
                PublisherTest.PEPE_SANCHEZ_NAME, password,
                user->fail(),
                ()-> token);

        assertEquals(token,notAuthenticatedToken);
    }

    private OpenChatSystem createSystem() {
        return new OpenChatSystem();
    }

    private User registerPepeSanchez() {
        return system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about");
    }

    private User registerJuanPerez() {
        return system.register(PublisherTest.JUAN_PEREZ_NAME,PublisherTest.JUAN_PEREZ_PASSWORD,"about");
    }


}