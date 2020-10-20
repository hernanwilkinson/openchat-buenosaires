import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PublisherTest {

    public static final String PEPE_SANCHEZ_NAME = "Pepe Sanchez";
    public static final String PEPE_SANCHEZ_PASSWORD = "password";
    public static final String JUAN_PEREZ_NAME = "Juan Perez";
    public static final String JUAN_PEREZ_PASSWORD = "";

    public static <T extends Throwable> void assertThrowsWithErrorMessage(
            Class<T> expectedType, Executable closureToFail, String errorMessage) {
        T error = assertThrows(
                expectedType,
                closureToFail);

        assertEquals(errorMessage,error.getMessage());
    }

    @Test
    public void canNotCreatePublisherWithBlankName() {
        assertThrowsWithErrorMessage(RuntimeException.class, () -> User.named(" ", "password", "about"), User.NAME_CANNOT_BE_BLANK);
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

        assertThrowsWithErrorMessage(RuntimeException.class, ()->follower.follow(follower), Publisher.CANNOT_FOLLOW_SELF);
        assertFalse(follower.hasFollowees());
    }
    @Test
    public void publisherCanNotFollowSamePublisherTwice() {
        Publisher follower = createPepeSanchez();
        Publisher followee = createJuanPerez();
        follower.follow(followee);

        assertThrowsWithErrorMessage(RuntimeException.class, ()->follower.follow(followee), Publisher.CANNOT_FOLLOW_TWICE);
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
        assertTrue(publication.hasPublishAt(publicationTime));

        assertFalse(publication.hasMessage(message + "something"));
        assertFalse(publication.hasPublishAt(publicationTime.plusSeconds(1)));
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
        return Publisher.relatedTo(User.named(JUAN_PEREZ_NAME, JUAN_PEREZ_PASSWORD,"about"));
    }

    private Publisher createPepeSanchez() {
        return Publisher.relatedTo(User.named(PEPE_SANCHEZ_NAME, PEPE_SANCHEZ_PASSWORD,"about"));
    }

}