import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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
                OpenChatSystem.CAN_NOT_REGISTER_SAME_USER_TWICE);

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
    @Test
    public void canNotAuthenticateWithInvalidPassword() {
        OpenChatSystem system = new OpenChatSystem();
        system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about");
        final boolean[] notAuthenticated = {false};

        system.withAuthenticatedUserDo(
                PublisherTest.PEPE_SANCHEZ_NAME,"",
                user->fail(),
                ()-> notAuthenticated[0] = true);

        assertTrue(notAuthenticated[0]);
    }

    @Test
    public void registeredUserCanPublish() {
        OpenChatSystem system = new OpenChatSystem();
        system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about");

        Publication publication = system.publishForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME,"hello");

        List<Publication> timeLine = system.timeLineForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME);

        assertEquals(Arrays.asList(publication),timeLine);
    }

    @Test
    public void noRegisteredUserCanNotPublish() {
        OpenChatSystem system = new OpenChatSystem();

        PublisherTest.assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->system.publishForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME,"hello"),
                OpenChatSystem.USER_NOT_REGISTERED);

    }
    @Test
    public void noRegisteredUserCanAskItsTimeline() {
        OpenChatSystem system = new OpenChatSystem();

        PublisherTest.assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->system.timeLineForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME),
                OpenChatSystem.USER_NOT_REGISTERED);

    }
    @Test
    public void canFollowRegisteredUser() {
        OpenChatSystem system = new OpenChatSystem();
        system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about");
        User followee = system.register(PublisherTest.JUAN_PEREZ_NAME,PublisherTest.JUAN_PEREZ_PASSWORD,"about");

        system.followForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.JUAN_PEREZ_NAME);

        List<User> followees = system.followeesOfUserNamed(PublisherTest.PEPE_SANCHEZ_NAME);

        assertEquals(Arrays.asList(followee),followees);
    }
    @Test
    public void canGetWallOfRegisteredUser() {
        OpenChatSystem system = new OpenChatSystem();
        system.register(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.PEPE_SANCHEZ_PASSWORD,"about");
        system.register(PublisherTest.JUAN_PEREZ_NAME,PublisherTest.JUAN_PEREZ_PASSWORD,"about");
        system.followForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME,PublisherTest.JUAN_PEREZ_NAME);

        Publication followerPublication = system.publishForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME,"hello");
        Publication followeePublication = system.publishForUserNamed(PublisherTest.JUAN_PEREZ_NAME,"bye");

        List<Publication> wall = system.wallForUserNamed(PublisherTest.PEPE_SANCHEZ_NAME);

        assertEquals(Arrays.asList(followerPublication,followeePublication),wall);
    }


}