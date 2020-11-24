package bsas.org.openchat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Publisher {
    public static final String CANNOT_FOLLOW_SELF = "Can not follow self";
    public static final String CANNOT_FOLLOW_TWICE = "Can not follow publisher twice";

    private final List<Publisher> followers = new ArrayList<>();
    private final List<Publication> publications = new ArrayList<>();
    private final User user;

    public static Publisher relatedTo(User user){
        return new Publisher(user);
    }

    private Publisher(User user) {
        this.user = user;
    }

    public boolean hasFollowers() {
        return !followers.isEmpty();
    }

    public void followedBy(Publisher potentialFollower) {
        assertCanNotFollowSelf(potentialFollower);
        assertCanNotFollowTwice(potentialFollower);

        followers.add(potentialFollower);
    }

    public boolean isFollowedBy(Publisher potentialFollower) {
        return followers.contains(potentialFollower);
    }

    public int numberOfFollowers() {
        return followers.size();
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
        followers.stream().forEach(follower->follower.addPublicationTo(wall));
        return sortedPublications(wall);
    }

    public List<Publisher> followers() {
        return Collections.unmodifiableList(followers);
    }

    public User relatedUser() {
        return user;
    }

    private void assertCanNotFollowTwice(Publisher potentialFollower) {
        if(isFollowedBy(potentialFollower)) throw new ModelException(CANNOT_FOLLOW_TWICE);
    }

    private void assertCanNotFollowSelf(Publisher potentialFollower) {
        if(this.equals(potentialFollower)) throw new ModelException(CANNOT_FOLLOW_SELF);
    }

    private List<Publication> sortedPublications(List<Publication> publications) {
        return publications.stream()
                .sorted((left, right) -> right.comparePublicationTimeWith(left))
                .collect(Collectors.toList());
    }

    private void addPublicationTo(List<Publication> publicationCollector) {
        publicationCollector.addAll(publications);
    }

}
