package integration;

import com.eclipsesource.json.JsonObject;
import org.junit.Test;

import static integration.APITestSuit.BASE_URL;
import static integration.APITestSuit.UUID_PATTERN;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

public class IT_RegistrationAPI {

    @Test public void
    register_a_new_user() {
        given()
                .body(withJsonContaining("Lucy", "alki324d", "About Lucy", "www.10pines.com"))
        .when()
                .post(BASE_URL + "/users")
        .then()
                .statusCode(201)
                .contentType(JSON)
                .body("id", matchesPattern(UUID_PATTERN))
                .body("username", is("Lucy"))
                .body("about", is("About Lucy"))
                .body("url", is("www.10pines.com"))
        ;
    }

    @Test
    public void register_a_duplicate_user() throws Exception {
        given()
                .body(withJsonContaining("username", "password", "about", "www.10pines.com"))
                .post(BASE_URL+"/users");
        given()
                .body(withJsonContaining("username", "xyzzy", "about", "www.10pines.com"))
                .when()
                .post(BASE_URL + "/users")
                .then()
                .statusCode(400)
                .assertThat().body(is("Username already in use."));
    }

    private String withJsonContaining(String username, String password, String about, String url) {
        return new JsonObject()
                        .add("username", username)
                        .add("password", password)
                        .add("about", about)
                        .add("url",url)
                        .toString();
    }
}
