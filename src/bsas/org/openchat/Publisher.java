package bsas.org.openchat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Publisher {
    public static final String CANNOT_FOLLOW_SELF = "Can not follow self";
    public static final String CANNOT_FOLLOW_TWICE = "Can not follow publisher twice";

    private final List<Publisher> followees = new ArrayList<>();
    private final List<Publication> publications = new ArrayList<>();
    private final User user;

    public static Publisher relatedTo(User user){
        return new Publisher(user);
    }

    private Publisher(User user) {
        this.user = user;
    }

    public boolean isNamed(String potentialName) {
        return user.isNamed(potentialName);
    }

    public boolean hasFollowees() {
        return !followees.isEmpty();
    }

    public void follow(Publisher potentialFollowee) {
        assertCanNotFollowSelf(potentialFollowee);
        assertCanNotFollowTwice(potentialFollowee);

        followees.add(potentialFollowee);
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

    public List<Publication> wall() {
        final ArrayList<Publication> wall = new ArrayList<>(this.publications);
        followees.stream().forEach(followee->followee.addPublicationTo(wall));
        return sortedPublications(wall);
    }

    public List<Publisher> followees() {
        return Collections.unmodifiableList(followees);
    }

    public User relatedUser() {
        return user;
    }

    private void assertCanNotFollowTwice(Publisher potentialFollowee) {
        if(doesFollow(potentialFollowee)) throw new RuntimeException(CANNOT_FOLLOW_TWICE);
    }

    private void assertCanNotFollowSelf(Publisher potentialFollowee) {
        if(this.equals(potentialFollowee)) throw new RuntimeException(CANNOT_FOLLOW_SELF);
    }

    private List<Publication> sortedPublications(List<Publication> publications) {
        return publications.stream()
                .sorted((left, right) -> left.comparePublicationTimeWith(right))
                .collect(Collectors.toList());
    }

    private void addPublicationTo(List<Publication> publicationCollector) {
        publicationCollector.addAll(publications);
    }

}
