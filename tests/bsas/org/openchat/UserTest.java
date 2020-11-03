package bsas.org.openchat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static bsas.org.openchat.TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage;

public class UserTest {
    private final TestObjectsBucket testObjects = new TestObjectsBucket();

    @Test
    public void canNotCreateUserWithBlankName() {
        assertThrowsModelExceptionWithErrorMessage(
                () -> User.named(" ", "password", "about", "www.10pines.com"),
                User.NAME_CANNOT_BE_BLANK);
    }

    @Test
    public void canCreateUserWithNoBlankName() {
        User createdUser = testObjects.createPepeSanchez();

        assertTrue(createdUser.isNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME));
    }

    @Test
    public void isNamedReturnsFalseWhenAskedWithOtherName() {
        User createdUser = testObjects.createPepeSanchez();

        assertFalse(createdUser.isNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME+"x"));
    }

    @Test
    public void userRemembersItsUrl() {
        User createdUser = User.named(
                TestObjectsBucket.PEPE_SANCHEZ_NAME,
                TestObjectsBucket.PEPE_SANCHEZ_PASSWORD,
                TestObjectsBucket.PEPE_SANCHEZ_ABOUT,
                TestObjectsBucket.PEPE_SANCHEZ_URL);

        assertEquals(TestObjectsBucket.PEPE_SANCHEZ_URL,createdUser.url());
        assertNotEquals(TestObjectsBucket.PEPE_SANCHEZ_URL+"X",createdUser.url());
    }

}