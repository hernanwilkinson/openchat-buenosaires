package org.openchat.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openchat.model.User;

import static org.openchat.test.TestObjectsBucket.assertThrowsWithErrorMessage;

public class UserTest {
    private final TestObjectsBucket testObjects = new TestObjectsBucket();

    @Test
    public void canNotCreateUserWithBlankName() {
        assertThrowsWithErrorMessage(
                RuntimeException.class,
                () -> User.named(" ", "password", "about"),
                User.NAME_CANNOT_BE_BLANK);
    }

    @Test
    public void canCreateUserWithNoBlankName() {
        User createdUser = testObjects.createPepeSanchez();

        Assertions.assertTrue(createdUser.isNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME));
    }

    @Test
    public void isNamedReturnsFalseWhenAskedWithOtherName() {
        User createdUser = testObjects.createPepeSanchez();

        Assertions.assertFalse(createdUser.isNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME+"x"));
    }
}