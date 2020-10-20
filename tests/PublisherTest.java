import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PublisherTest {

    public static final String PEPE_SANCHEZ_NAME = "Pepe Sanchez";
    public static final String PEPE_SANCHEZ_PASSWORD = "password";

    @Test
    public void publisherCanNotHaveBlankName() {
        RuntimeException error = assertThrows(
                RuntimeException.class,
                ()->Publisher.named(" ","password","about"));

        assertEquals(Publisher.NAME_CANNOT_BE_BLANK,error.getMessage());
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
    @Test
    public void createdPusblisherHasNoPublications() {
        Publisher createdPublisher = createPepeSanchez();

        assertTrue(createdPublisher.doesNotHavePublications());
    }
    @Test
    public void publisherCanPublishMessages() {
        Publisher createdPublisher = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "a message";
        Publication publication = createdPublisher.publish(message, publicationTime);

        assertFalse(createdPublisher.doesNotHavePublications());
        assertTrue(publication.hasMessage(message));
        assertTrue(publication.hasPublishAt(publicationTime));
    }
    @Test
    public void timelineHasPublisherPublicationsSortedByPublicationTime() {
        Publisher createdPublisher = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "a message";
        Publication secondPublication = createdPublisher.publish(message, publicationTime.plusSeconds(1));
        Publication firstPublication = createdPublisher.publish(message, publicationTime);

        List<Publication> timeLine = createdPublisher.timeLine();

        assertEquals(Arrays.asList(firstPublication,secondPublication),timeLine);
    }
    @Test
    public void wallContainsPublisherPublications() {
        Publisher follower = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "a message";
        Publication firstPublication = follower.publish(message, publicationTime);

        List<Publication> wall = follower.wall();

        assertEquals(Arrays.asList(firstPublication),wall);
    }
    @Test
    public void wallContainsFolloweesPublications() {
        Publisher follower = createPepeSanchez();
        Publisher followee = createJuanPerez();

        follower.follow(followee);
        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "a message";
        Publication firstPublication = followee.publish(message, publicationTime.plusSeconds(1));

        List<Publication> wall = follower.wall();

        assertEquals(Arrays.asList(firstPublication),wall);
    }

    @Test
    public void wallContainsFolloweesPublicationsInOrder() {
        Publisher follower = createPepeSanchez();
        Publisher followee = createJuanPerez();

        follower.follow(followee);
        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "a message";
        Publication firstPublication = follower.publish(message, publicationTime);
        Publication secondPublication = followee.publish(message, publicationTime.plusSeconds(1));
        Publication thirdPublication = follower.publish(message, publicationTime.plusSeconds(2));

        List<Publication> wall = follower.wall();

        assertEquals(Arrays.asList(firstPublication,secondPublication,thirdPublication),wall);
    }

    private Publisher createJuanPerez() {
        return Publisher.named("Juan Perez", "","about");
    }

    private Publisher createPepeSanchez() {
        return Publisher.named(PEPE_SANCHEZ_NAME, PEPE_SANCHEZ_PASSWORD,"about");
    }

}