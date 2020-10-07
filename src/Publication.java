import java.time.LocalDateTime;

public class Publication {
    private final String message;
    private final LocalDateTime publicationTime;

    public Publication(Publisher publisher, String message, LocalDateTime publicationTime) {

        this.message = message;
        this.publicationTime = publicationTime;
    }

    public static Publication madeBy(Publisher publisher, String message, LocalDateTime publicationTime) {
        return new Publication(publisher,message,publicationTime);
    }

    public boolean hasMessage(String potentialMessage) {
        return message.equals(potentialMessage);
    }

    public boolean hasPublishAt(LocalDateTime potentialTime) {
        return publicationTime.equals(potentialTime);
    }
}
