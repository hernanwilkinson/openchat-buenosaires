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
    @Test
    public void createdPublisherHasNoFollowees() {
        Publisher createdPublisher = createPepeSanchez();

        assertTrue(createdPublisher.hasNoFollowees());
    }
    @Test
    public void publisherCanFollowOtherPublisher() {
        Publisher follower = createPepeSanchez();
        Publisher followee = createJuanPerez();

        follower.follow(followee);

        assertFalse(follower.hasNoFollowees());
        assertTrue(follower.doesFollow(followee));
        assertEquals(1,follower.numberOfFollowees());
    }
    @Test
    public void publisherCanNotFollowSelf() {
        Publisher follower = createPepeSanchez();

        RuntimeException error = assertThrows(
                RuntimeException.class,
                ()->follower.follow(follower));

        assertEquals(Publisher.CAN_NOT_FOLLOW_SELF,error.getMessage());
        assertTrue(follower.hasNoFollowees());
    }
    @Test
    public void publisherCanNotFollowSamePublisherTwice() {
        Publisher follower = createPepeSanchez();
        Publisher followee = createJuanPerez();
        follower.follow(followee);

        RuntimeException error = assertThrows(
                RuntimeException.class,
                ()->follower.follow(followee));

        assertEquals(Publisher.CAN_NOT_FOLLOW_TWICE,error.getMessage());
        assertFalse(follower.hasNoFollowees());
        assertTrue(follower.doesFollow(followee));
        assertEquals(1,follower.numberOfFollowees());
    }

    private Publisher createJuanPerez() {
        return Publisher.named("Juan Perez", "","about");
    }

    private Publisher createPepeSanchez() {
        return Publisher.named(PEPE_SANCHEZ_NAME, PEPE_SANCHEZ_PASSWORD,"about");
    }

}