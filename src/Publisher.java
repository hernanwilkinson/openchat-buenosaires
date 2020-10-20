import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Publisher {
    public static final String NAME_CANNOT_BE_BLANK = "Name can not be blank";
    public static final String CAN_NOT_FOLLOW_SELF = "Can not follow self";
    public static final String CAN_NOT_FOLLOW_TWICE = "Can not follow publisher twice";
    private final String name;
    private final List<Publisher> followees = new ArrayList<>();
    private final List<Publication> publications = new ArrayList<>();

    public Publisher(String name) {
        this.name = name;
    }

    public static Publisher named(String name, String password, String about) {
        assertNameIsNotBlank(name);

        return new Publisher(name);
    }

    private static void assertNameIsNotBlank(String name) {
        if(name.isBlank()) throw new RuntimeException(NAME_CANNOT_BE_BLANK);
    }

    public boolean isNamed(String potentialName) {
        return name.equals(potentialName);
    }

    public boolean hasFollowees() {
        return !followees.isEmpty();
    }

    public void follow(Publisher potentialFollowee) {
        assertCanNotFollowSelf(potentialFollowee);
        assertCanNotFollowTwice(potentialFollowee);

        followees.add(potentialFollowee);
    }

    private void assertCanNotFollowTwice(Publisher potentialFollowee) {
        if(doesFollow(potentialFollowee)) throw new RuntimeException(CAN_NOT_FOLLOW_TWICE);
    }

    private void assertCanNotFollowSelf(Publisher potentialFollowee) {
        if(this.equals(potentialFollowee)) throw new RuntimeException(CAN_NOT_FOLLOW_SELF);
    }

    public boolean doesFollow(Publisher potentialFollowee) {
        return followees.contains(potentialFollowee);
    }

    public int numberOfFollowees() {
        return followees.size();
    }

    public boolean hasPublications() {
        return !publications.isEmpty();
    }

    public Publication publish(String message, LocalDateTime publicationTime) {
        final Publication newPublication = Publication.madeBy(this, message, publicationTime);
        publications.add(newPublication);

        return newPublication;
    }

    public List<Publication> timeLine() {
        return sortedPublications(publications);
    }

    private List<Publication> sortedPublications(List<Publication> publications) {
        return publications.stream()
                .sorted((left, right) -> left.comparePublicationTimeWith(right))
                .collect(Collectors.toList());
    }

    public List<Publication> wall() {
        final ArrayList<Publication> wall = new ArrayList<>(this.publications);
        followees.stream().forEach(followee->followee.addPublicationTo(wall));
        return sortedPublications(wall);
    }

    private void addPublicationTo(List<Publication> publicationCollector) {
        publicationCollector.addAll(publications);
    }
}
