package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoadExample {
    private static final int NUMBER_OF_PUBLICATIONS = 10;
    public static final int NUMBER_OF_USERS = 1000;
    public static final int NUMBER_OF_FOLLOWERS = 20;
    public static final String USER_NAME_HEADER = "AAAAA";

    private String userNamePrefix;
    private List<String> followers;
    private RestReceptionist receptionist;
    private int publicationMaxSize = 0;

    public static void main(String[] args) {
        new LoadExample().load(new RestReceptionist(
                Environment.current().systemFactory()));
    }

    public void load(RestReceptionist receptionist) {
        final long startTime = System.currentTimeMillis();
        printMemoryUsage();

        this.receptionist = receptionist;

        userNamePrefix = "";
        followers = new ArrayList<>();
        for(int currentUserNumber = 0; currentUserNumber< NUMBER_OF_USERS; currentUserNumber++) {
            final String userName = createUserName(currentUserNumber);

            String newUserId = registerUser(userName);
            addFollowers(newUserId);
            addPublications(userName, newUserId);

            calculatePrefix(currentUserNumber);
            if(currentUserNumber%50==0)
                System.out.println(currentUserNumber);
        }

        final long endTime = System.currentTimeMillis();
        printMemoryUsage();
        System.out.println("Elapsed millis: " + (endTime-startTime));
        System.out.println(publicationMaxSize);
    }

    private void calculatePrefix(int currentUserNumber) {
        if (currentUserNumber % 26 == 0) userNamePrefix += "A";
    }

    private String createUserName(int currentUserNumber) {
        return USER_NAME_HEADER
                + userNamePrefix
                + Character.toString(
                Character.valueOf('A') + currentUserNumber % 26);
    }

    private String registerUser(String userName) {
        JsonObject registration = new JsonObject()
                .add(RestReceptionist.USERNAME_KEY, userName)
                .add(RestReceptionist.ABOUT_KEY, userName + userName + userName)
                .add(RestReceptionist.PASSWORD_KEY, userName)
                .add(RestReceptionist.HOME_PAGE_KEY,"www.10pines.com");

        ReceptionistResponse response = receptionist.registerUser(registration);

        return response.idFromBody();
    }

    private void addFollowers(String newUserId) {
        followers.stream().forEach(follower-> receptionist.followings(
                new JsonObject()
                        .add(RestReceptionist.FOLLOWED_ID_KEY, newUserId)
                        .add(RestReceptionist.FOLLOWER_ID_KEY,follower)));
        followers.add(newUserId);

        keepFollowersSize();
    }

    private void keepFollowersSize() {
        if(followers.size()> NUMBER_OF_FOLLOWERS)
            followers.remove(0);
    }

    private void addPublications(String userName, String newUserId) {
        char[] publication = createPublication(userName).toCharArray();
        publicationMaxSize = Math.max(publication.length,publicationMaxSize);
        for(int publicationNumber=0;publicationNumber<NUMBER_OF_PUBLICATIONS;publicationNumber++) {
            receptionist.addPublication(
                    newUserId,
                    new JsonObject()
                            .add(RestReceptionist.TEXT_KEY,new String(publication)));
        }
    }

    private String createPublication(String userName) {
        String publication = "";

        for(int currentIteration = 0; currentIteration< userName.length(); currentIteration++)
            publication += userName;

        return publication;
    }

    private void printMemoryUsage() {
        System.gc();

        Runtime rt = Runtime.getRuntime();
        long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;

        System.out.println("used megabytes: " + usedMB);
    }

}