package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoadExample {
    private static final int NUMBER_OF_PUBLICATIONS = 300;
    public static final int NUMBER_OF_USERS = 1000;
    public static final int NUMBER_OF_FOLLOWEES = 20;
    public static final String USER_NAME_HEADER = "AAAAA";

    private String userNamePrefix;
    private List<String> followees;
    private RestReceptionist receptionist;

    public static void main(String[] args) {
        new LoadExample().load();
    }

    public void load() {
        printMemoryUsage();

        receptionist = new RestReceptionist(
                new OpenChatSystem(()-> LocalDateTime.now()));

        userNamePrefix = "";
        followees = new ArrayList<>();
        for(int currentUserNumber = 0; currentUserNumber< NUMBER_OF_USERS; currentUserNumber++) {
            final String userName = createUserName(currentUserNumber);

            String newUserId = registerUser(userName);
            addFollowees(newUserId);
            addPublications(userName, newUserId);

            calculatePrefix(currentUserNumber);
        }
        System.out.println("-------");
        printMemoryUsage();
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
        String newUserId = response.responseBodyAsJson().getString(RestReceptionist.ID_KEY,"");

        return newUserId;
    }

    private void addFollowees(String newUserId) {
        followees.stream().forEach(followee-> receptionist.followings(
                new JsonObject()
                        .add(RestReceptionist.FOLLOWER_ID_KEY, newUserId)
                        .add(RestReceptionist.FOLLOWEE_ID_KEY,followee)));
        followees.add(newUserId);

        keepFolloweesSize();
    }

    private void keepFolloweesSize() {
        if(followees.size()> NUMBER_OF_FOLLOWEES)
            followees.remove(0);
    }

    private void addPublications(String userName, String newUserId) {
        String publication = createPublication(userName);

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