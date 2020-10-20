import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;

public class Publication {
    public static final String INAPPROPRIATE_WORD = "Inappropriate word";
    private final String message;
    private final LocalDateTime publicationTime;

    public Publication(Publisher publisher, String message, LocalDateTime publicationTime) {

        this.message = message;
        this.publicationTime = publicationTime;
    }

    public static Publication madeBy(Publisher publisher, String message, LocalDateTime publicationTime) {
        if(message.equals("elephant")) throw new RuntimeException(INAPPROPRIATE_WORD);

        return new Publication(publisher,message,publicationTime);
    }

    public boolean hasMessage(String potentialMessage) {
        return message.equals(potentialMessage);
    }

    public boolean hasPublishAt(LocalDateTime potentialTime) {
        return publicationTime.equals(potentialTime);
    }

    public int comparePublicationTimeWith(Publication publicationToCompare) {
        return publicationTime.compareTo(publicationToCompare.publicationTime());
    }

    private LocalDateTime publicationTime() {
        return publicationTime;
    }
}
