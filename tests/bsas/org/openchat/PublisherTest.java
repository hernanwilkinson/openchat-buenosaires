package bsas.org.openchat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static bsas.org.openchat.TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage;

public class PublisherTest {

    private final TestObjectsBucket testObjects = new TestObjectsBucket();

    @Test
    public void createdPublisherHasNoFollowees() {
        Publisher createdPublisher = createPepeSanchez();

        assertFalse(createdPublisher.hasFollowees());
    }
    @Test
    public void publisherCanFollowOtherPublisher() {
        Publisher follower = createPepeSanchez();
        Publisher followee = createJuanPerez();

        follower.follow(followee);

        assertTrue(follower.hasFollowees());
        assertTrue(follower.doesFollow(followee));
        assertEquals(1,follower.numberOfFollowees());
    }
    @Test
    public void publisherCanNotFollowSelf() {
        Publisher follower = createPepeSanchez();

        assertThrowsModelExceptionWithErrorMessage(()->follower.follow(follower), Publisher.CANNOT_FOLLOW_SELF);
        assertFalse(follower.hasFollowees());
    }
    @Test
    public void publisherCanNotFollowSamePublisherTwice() {
        Publisher follower = createPepeSanchez();
        Publisher followee = createJuanPerez();
        follower.follow(followee);

        assertThrowsModelExceptionWithErrorMessage(()->follower.follow(followee), Publisher.CANNOT_FOLLOW_TWICE);
        assertTrue(follower.hasFollowees());
        assertTrue(follower.doesFollow(followee));
        assertEquals(1,follower.numberOfFollowees());
    }
    @Test
    public void createdPusblisherHasNoPublications() {
        Publisher createdPublisher = createPepeSanchez();

        assertFalse(createdPublisher.hasPublications());
    }
    @Test
    public void publisherCanPublishMessages() {
        Publisher createdPublisher = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "a message";
        Publication publication = createdPublisher.publish(message, publicationTime);

        assertTrue(createdPublisher.hasPublications());
        assertTrue(publication.hasMessage(message));
        assertTrue(publication.wasPublishedAt(publicationTime));

        assertFalse(publication.hasMessage(message + "something"));
        assertFalse(publication.wasPublishedAt(publicationTime.plusSeconds(1)));
    }
    @Test
    public void timelineHasPublisherPublicationsSortedWithLatestPublicationsFirst() {
        Publisher createdPublisher = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "a message";
        Publication secondPublication = createdPublisher.publish(message, publicationTime.plusSeconds(1));
        Publication firstPublication = createdPublisher.publish(message, publicationTime);

        List<Publication> timeLine = createdPublisher.timeLine();

        assertEquals(Arrays.asList(secondPublication,firstPublication),timeLine);
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
    public void wallContainsFolloweesPublicationsWithLatestPublicationsFirst() {
        Publisher follower = createPepeSanchez();
        Publisher followee = createJuanPerez();

        follower.follow(followee);
        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "a message";
        Publication firstPublication = follower.publish(message, publicationTime);
        Publication secondPublication = followee.publish(message, publicationTime.plusSeconds(1));
        Publication thirdPublication = follower.publish(message, publicationTime.plusSeconds(2));

        List<Publication> wall = follower.wall();

        assertEquals(Arrays.asList(thirdPublication, secondPublication, firstPublication),wall);
    }
    @Test
    public void canNotPublishWithInappropriateWord() {
        Publisher createdPublisher = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "elephant";
        assertThrowsModelExceptionWithErrorMessage(
                ()->createdPublisher.publish(message, publicationTime),
                Publication.INAPPROPRIATE_WORD);

        assertFalse(createdPublisher.hasPublications());
    }
    @Test
    public void canNotPublishWithInappropriateWordInUpperCase() {
        Publisher createdPublisher = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "ELEPHANT";
        assertThrowsModelExceptionWithErrorMessage(
                ()->createdPublisher.publish(message, publicationTime),
                Publication.INAPPROPRIATE_WORD);

        assertFalse(createdPublisher.hasPublications());
    }
    @Test
    public void canNotPublishAMessageContainingInappropriateWord() {
        Publisher createdPublisher = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "abc ELEPHANT xx";
        assertThrowsModelExceptionWithErrorMessage(
                ()->createdPublisher.publish(message, publicationTime),
                Publication.INAPPROPRIATE_WORD);

        assertFalse(createdPublisher.hasPublications());
    }
    @Test
    public void canNotPublishAnyInappropriateWord() {
        Publisher createdPublisher = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        Arrays.asList("elephant", "ice cream", "orange").forEach(
                message -> {
                    assertThrowsModelExceptionWithErrorMessage(
                            () -> createdPublisher.publish(message, publicationTime),
                            Publication.INAPPROPRIATE_WORD);
                    assertFalse(createdPublisher.hasPublications());
                });
    }

    private Publisher createJuanPerez() {
        return Publisher.relatedTo(testObjects.createUserJuanPerez());
    }

    private Publisher createPepeSanchez() {
        return Publisher.relatedTo(testObjects.createPepeSanchez());
    }

}