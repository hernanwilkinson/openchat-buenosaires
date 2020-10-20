package bsas.org.openchat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OpenChatSystemTest {

    private OpenChatSystem system;

    @Test
    public void createSystemHasNoUsers() {
        system = createSystem();

        Assertions.assertFalse(system.hasUsers());
        Assertions.assertFalse(system.hasUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME));
        Assertions.assertEquals(0, system.numberOfUsers());
    }
    @Test
    public void canRegisterUser() {
        system = createSystem();
        registerPepeSanchez();

        Assertions.assertTrue(system.hasUsers());
        Assertions.assertTrue(system.hasUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME));
        Assertions.assertEquals(1,system.numberOfUsers());
    }
    @Test
    public void canRegisterManyUsers() {
        system = createSystem();
        registerPepeSanchez();
        registerJuanPerez();

        Assertions.assertTrue(system.hasUsers());
        Assertions.assertTrue(system.hasUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME));
        Assertions.assertTrue(system.hasUserNamed(TestObjectsBucket.JUAN_PEREZ_NAME));
        Assertions.assertEquals(2,system.numberOfUsers());
    }
    @Test
    public void canNotRegisterSameUserTwice() {
        system = createSystem();
        registerPepeSanchez();

        TestObjectsBucket.assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->registerPepeSanchez(),
                OpenChatSystem.CANNOT_REGISTER_SAME_USER_TWICE);

        Assertions.assertTrue(system.hasUsers());
        Assertions.assertTrue(system.hasUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME));
        Assertions.assertEquals(1,system.numberOfUsers());
    }
    @Test
    public void canWorkWithAuthenticatedUser() {
        system = createSystem();
        registerPepeSanchez();

        final Object token = createSystem();
        final Object authenticatedToken = system.withAuthenticatedUserDo(
                TestObjectsBucket.PEPE_SANCHEZ_NAME, TestObjectsBucket.PEPE_SANCHEZ_PASSWORD,
                user->token,
                ()->fail());

        assertEquals(token,authenticatedToken);
    }
    @Test
    public void notRegisteredUserIsNotAuthenticated() {
        system = createSystem();
        assertCanNotAuthenticateWith(system, TestObjectsBucket.PEPE_SANCHEZ_PASSWORD);
    }
    @Test
    public void canNotAuthenticateWithInvalidPassword() {
        system = createSystem();
        registerPepeSanchez();

        assertCanNotAuthenticateWith(system, TestObjectsBucket.PEPE_SANCHEZ_PASSWORD+"something");
    }
    @Test
    public void registeredUserCanPublish() {
        system = createSystem();
        registerPepeSanchez();

        Publication publication = system.publishForUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME,"hello");

        List<Publication> timeLine = system.timeLineForUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME);
        assertEquals(Arrays.asList(publication),timeLine);
    }
    @Test
    public void noRegisteredUserCanNotPublish() {
        system = createSystem();

        TestObjectsBucket.assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->system.publishForUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME,"hello"),
                OpenChatSystem.USER_NOT_REGISTERED);
    }
    @Test
    public void noRegisteredUserCanAskItsTimeline() {
        system = createSystem();

        TestObjectsBucket.assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->system.timeLineForUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME),
                OpenChatSystem.USER_NOT_REGISTERED);
    }
    @Test
    public void canFollowRegisteredUser() {
        system = createSystem();
        registerPepeSanchez();
        User followee = registerJuanPerez();

        system.followForUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME, TestObjectsBucket.JUAN_PEREZ_NAME);

        List<User> followees = system.followeesOfUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME);
        assertEquals(Arrays.asList(followee),followees);
    }
    @Test
    public void canGetWallOfRegisteredUser() {
        system = createSystem();
        registerPepeSanchez();
        registerJuanPerez();
        system.followForUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME, TestObjectsBucket.JUAN_PEREZ_NAME);

        Publication followerPublication = system.publishForUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME,"hello");
        Publication followeePublication = system.publishForUserNamed(TestObjectsBucket.JUAN_PEREZ_NAME,"bye");

        List<Publication> wall = system.wallForUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME);
        assertEquals(Arrays.asList(followerPublication,followeePublication),wall);
    }

    private void assertCanNotAuthenticateWith(OpenChatSystem system, String password) {
        final Object token = createSystem();
        final Object notAuthenticatedToken = system.withAuthenticatedUserDo(
                TestObjectsBucket.PEPE_SANCHEZ_NAME, password,
                user->fail(),
                ()-> token);

        assertEquals(token,notAuthenticatedToken);
    }

    private OpenChatSystem createSystem() {
        return new OpenChatSystem();
    }

    private User registerPepeSanchez() {
        return system.register(TestObjectsBucket.PEPE_SANCHEZ_NAME, TestObjectsBucket.PEPE_SANCHEZ_PASSWORD,"about");
    }

    private User registerJuanPerez() {
        return system.register(TestObjectsBucket.JUAN_PEREZ_NAME, TestObjectsBucket.JUAN_PEREZ_PASSWORD,"about");
    }
}