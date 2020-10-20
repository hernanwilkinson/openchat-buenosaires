package bsas.org.openchat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static bsas.org.openchat.TestObjectsBucket.assertThrowsWithErrorMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PublisherTest {

    private final TestObjectsBucket testObjects = new TestObjectsBucket();

    @Test
    public void createdPublisherHasNoFollowees() {
        Publisher createdPublisher = createPepeSanchez();

        Assertions.assertFalse(createdPublisher.hasFollowees());
    }
    @Test
    public void publisherCanFollowOtherPublisher() {
        Publisher follower = createPepeSanchez();
        Publisher followee = createJuanPerez();

        follower.follow(followee);

        Assertions.assertTrue(follower.hasFollowees());
        Assertions.assertTrue(follower.doesFollow(followee));
        assertEquals(1,follower.numberOfFollowees());
    }
    @Test
    public void publisherCanNotFollowSelf() {
        Publisher follower = createPepeSanchez();

        assertThrowsWithErrorMessage(RuntimeException.class, ()->follower.follow(follower), Publisher.CANNOT_FOLLOW_SELF);
        Assertions.assertFalse(follower.hasFollowees());
    }
    @Test
    public void publisherCanNotFollowSamePublisherTwice() {
        Publisher follower = createPepeSanchez();
        Publisher followee = createJuanPerez();
        follower.follow(followee);

        assertThrowsWithErrorMessage(RuntimeException.class, ()->follower.follow(followee), Publisher.CANNOT_FOLLOW_TWICE);
        Assertions.assertTrue(follower.hasFollowees());
        Assertions.assertTrue(follower.doesFollow(followee));
        assertEquals(1,follower.numberOfFollowees());
    }
    @Test
    public void createdPusblisherHasNoPublications() {
        Publisher createdPublisher = createPepeSanchez();

        Assertions.assertFalse(createdPublisher.hasPublications());
    }
    @Test
    public void publisherCanPublishMessages() {
        Publisher createdPublisher = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "a message";
        Publication publication = createdPublisher.publish(message, publicationTime);

        Assertions.assertTrue(createdPublisher.hasPublications());
        Assertions.assertTrue(publication.hasMessage(message));
        Assertions.assertTrue(publication.hasPublishAt(publicationTime));

        Assertions.assertFalse(publication.hasMessage(message + "something"));
        Assertions.assertFalse(publication.hasPublishAt(publicationTime.plusSeconds(1)));
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
    @Test
    public void canNotPublishWithInappropriateWord() {
        Publisher follower = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "elephant";
        assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->follower.publish(message, publicationTime),
                Publication.INAPPROPRIATE_WORD);
    }
    @Test
    public void canNotPublishWithInappropriateWordInUpperCase() {
        Publisher follower = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "ELEPHANT";
        assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->follower.publish(message, publicationTime),
                Publication.INAPPROPRIATE_WORD);
    }
    @Test
    public void canNotPublishAMessageContainingInappropriateWord() {
        Publisher follower = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        final String message = "abc ELEPHANT xx";
        assertThrowsWithErrorMessage(
                RuntimeException.class,
                ()->follower.publish(message, publicationTime),
                Publication.INAPPROPRIATE_WORD);
    }
    @Test
    public void canNotPublishAnyInappropriateWord() {
        Publisher follower = createPepeSanchez();

        final LocalDateTime publicationTime = LocalDateTime.now();
        Arrays.asList("elephant","ice cream","orange").forEach(
                message-> assertThrowsWithErrorMessage(
                        RuntimeException.class,
                        ()->follower.publish(message, publicationTime),
                        Publication.INAPPROPRIATE_WORD));
    }

    private Publisher createJuanPerez() {
        return Publisher.relatedTo(testObjects.createUserJuanPerez());
    }

    private Publisher createPepeSanchez() {
        return Publisher.relatedTo(testObjects.createPepeSanchez());
    }

}