package integration;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import integration.dsl.OpenChatTestDSL;
import integration.dsl.PostDSL.ITPost;
import integration.dsl.UserDSL.ITUser;
import io.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.reverse;
import static integration.APITestSuit.*;
import static integration.dsl.OpenChatTestDSL.*;
import static integration.dsl.PostDSL.ITPostBuilder.aPost;
import static integration.dsl.UserDSL.ITUserBuilder.aUser;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.matchesPattern;

public class IT_TimelineAPI {

    private static ITUser DAVID = aUser().withUsername("David").build();
    private static ITUser JOHN = aUser().withUsername("John").build();

    private JsonArray timeline;
    private static List<ITPost> POSTS;

    @BeforeClass
    public static void initialise() {
        DAVID = register(DAVID);
        POSTS = createPostsFor(DAVID, 2);
    }

    @Test public void
    retrieve_a_timeline_with_all_posts_from_a_user_in_reverse_chronological_order() {
        givenDavidPosts(POSTS);

        whenHeChecksHisTimeline();

        thenHeShouldSee(reverse(POSTS));
    }
    @Test
    public void can_add_one_post() throws Exception {
        ITPost post = aPost().withUserId(DAVID.id()).build();
        given()
                .body(withPostJsonContaining(post.text()))
                .when()
                .post(BASE_URL + "/users/" + post.userId() + "/timeline")
                .then()
                .statusCode(201)
                .contentType(JSON)
                .body("postId", matchesPattern(UUID_PATTERN))
                .body("userId", is(post.userId()))
                .body("text", is(post.text()))
                .body("dateTime", matchesPattern(DATE_PATTERN))
                .body("likes", is(0));
    }
    @Test
    public void user_can_like_post() throws Exception {
        JOHN = register(JOHN);

        ITPost post = aPost().withUserId(DAVID.id()).build();
        Response response =
                given()
                        .body(withPostJsonContaining(post.text()))
                        .when()
                        .post(BASE_URL + "/users/" + post.userId() + "/timeline");
        JsonObject responseJson = Json.parse(response.body().asString()).asObject();
        String postId = responseJson.getString("postId", "");

        given()
                .body(new JsonObject().add("userId", JOHN.id()).toString())
                .when()
                .post(BASE_URL + "/publications/" + postId + "/like")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("likes", is(1));
    }

    @Test
    public void cannot_add_one_inappropriate_post() throws Exception {
        ITPost post = aPost().withUserId(DAVID.id()).build();
        given()
                .body(withPostJsonContaining("orange"))
                .when()
                .post(BASE_URL + "/users/" + post.userId() + "/timeline")
                .then()
                .statusCode(400)
                .body(is("Post contains inappropriate language."));
    }

    private static List<ITPost> createPostsFor(ITUser user, int numberOfPosts) {
        List<ITPost> posts = new ArrayList<>();
        for (int i = 0; i < numberOfPosts; i++) {
            ITPost post = aPost().withUserId(user.id()).withText("Post " + i).build();
            posts.add(post);
        }
        return posts;
    }

    public static String withPostJsonContaining(String text) {
        return new JsonObject().add("text", text).toString();
    }

    private void givenDavidPosts(List<ITPost> posts) {
        posts.forEach(OpenChatTestDSL::create);
    }

    private void whenHeChecksHisTimeline() {
        Response response = when().get(BASE_URL + "/users/" + DAVID.id() + "/timeline");
        timeline = Json.parse(response.asString()).asArray();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.contentType()).isEqualTo(JSON.toString());
    }

    private void thenHeShouldSee(List<ITPost> posts) {
        for (int index = 0; index < posts.size(); index++) {
            assertThatJsonPostMatchesPost(timeline.get(index), posts.get(index), 0);
        }
    }

}
