package bsas.org.openchat;

import java.time.LocalDateTime;
import java.util.*;

public class Publication {
    public static final String INAPPROPRIATE_WORD = "Post contains inappropriate language.";
    public static final List<String> inappropriateWords =
            Collections.unmodifiableList(Arrays.asList("elephant","ice cream","orange"));

    private final Publisher publisher;
    private final String message;
    private final LocalDateTime publicationTime;
    private final Set<Publisher> likers;

    public Publication(Publisher publisher, String message, LocalDateTime publicationTime) {
        this.publisher = publisher;
        this.message = message;
        this.publicationTime = publicationTime;
        this.likers = new HashSet<>();
    }

    public static Publication madeBy(Publisher publisher, String message, LocalDateTime publicationTime) {
        assertIsAppropriate(message);

        return new Publication(publisher,message,publicationTime);
    }

    private static void assertIsAppropriate(String message) {
        if(isInappropriate(message))
            throw new ModelException(INAPPROPRIATE_WORD);
    }

    private static boolean isInappropriate(String message) {
        final String lowerCaseMessage = message.toLowerCase();

        return inappropriateWords.stream().anyMatch(inappropriateWord ->
                lowerCaseMessage.contains(inappropriateWord));
    }

    public boolean hasMessage(String potentialMessage) {
        return message.equals(potentialMessage);
    }

    public boolean wasPublishedAt(LocalDateTime potentialTime) {
        return publicationTime.equals(potentialTime);
    }

    public int comparePublicationTimeWith(Publication publicationToCompare) {
        return publicationTime.compareTo(publicationToCompare.publicationTime());
    }

    public LocalDateTime publicationTime() {
        return publicationTime;
    }

    public String message() {
        return message;
    }

    public User publisherRelatedUser(){
        return publisher.relatedUser();
    }

    // Acceso de paquete para que no puedan agregar por fuera del sistema
    void addLiker(Publisher liker) {
        likers.add(liker);
    }

    public int likes() {
        return likers.size();
    }
}
