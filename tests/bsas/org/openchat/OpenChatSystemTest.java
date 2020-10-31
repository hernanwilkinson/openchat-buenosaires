package bsas.org.openchat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;


public class OpenChatSystemTest {

    private OpenChatSystem system;
    private TestObjectsBucket testObjects = new TestObjectsBucket();

    @Test
    public void createSystemHasNoUsers() {
        //No lo inicializo en el setup por si quiero hacer restart del
        //contexto mientras debuggeo
        system = createSystem();

        assertFalse(system.hasUsers());
        assertFalse(system.hasUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME));
        assertEquals(0, system.numberOfUsers());
    }
    @Test
    public void canRegisterUser() {
        system = createSystem();
        User registeredUser = registerPepeSanchez();

        assertTrue(system.hasUsers());
        assertTrue(system.hasUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME));
        assertEquals(1,system.numberOfUsers());
        assertEquals(TestObjectsBucket.PEPE_SANCHEZ_URL,registeredUser.url());
    }
    @Test
    public void canRegisterManyUsers() {
        system = createSystem();
        registerPepeSanchez();
        registerJuanPerez();

        assertTrue(system.hasUsers());
        assertTrue(system.hasUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME));
        assertTrue(system.hasUserNamed(TestObjectsBucket.JUAN_PEREZ_NAME));
        assertEquals(2,system.numberOfUsers());
    }
    @Test
    public void canNotRegisterSameUserTwice() {
        system = createSystem();
        registerPepeSanchez();

        TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage(
                ()->registerPepeSanchez(),
                OpenChatSystem.CANNOT_REGISTER_SAME_USER_TWICE);

        assertTrue(system.hasUsers());
        assertTrue(system.hasUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME));
        assertEquals(1,system.numberOfUsers());
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

        TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage(
                ()->system.publishForUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME,"hello"),
                OpenChatSystem.USER_NOT_REGISTERED);
    }
    @Test
    public void noRegisteredUserCanAskItsTimeline() {
        system = createSystem();

        TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage(
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
        return new OpenChatSystem(testObjects.fixedNowClock());
    }

    private User registerPepeSanchez() {
        return system.register(
                TestObjectsBucket.PEPE_SANCHEZ_NAME,
                TestObjectsBucket.PEPE_SANCHEZ_PASSWORD,
                TestObjectsBucket.PEPE_SANCHEZ_ABOUT,
                TestObjectsBucket.PEPE_SANCHEZ_URL);
    }

    private User registerJuanPerez() {
        return system.register(
                TestObjectsBucket.JUAN_PEREZ_NAME,
                TestObjectsBucket.JUAN_PEREZ_PASSWORD,
                TestObjectsBucket.JUAN_PEREZ_ABOUT,
                TestObjectsBucket.JUAN_PEREZ_URL);
    }
}