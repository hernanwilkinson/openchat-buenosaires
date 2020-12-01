package org.openchat;

import bsas.org.openchat.Environment;
import bsas.org.openchat.OpenChatSystem;
import bsas.org.openchat.ReceptionistResponse;
import bsas.org.openchat.RestReceptionist;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import spark.Request;
import spark.Response;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static spark.Spark.get;
import static spark.Spark.post;

public class Routes {

    private RestReceptionist receptionist;

    public void create() {
        receptionist = new RestReceptionist(Environment.current().systemFactory());

        openchatRoutes();
    }

    private void openchatRoutes() {
        get("status", (req, res) -> "OpenChat: OK!");
        post("users", (req, res) -> registerUser(req,res));
        post("login", (req, res) -> login(req,res));
        get("users", (req, res) -> users(res));
        post("users/:userId/timeline", (req, res) -> publish(req,res));
        get("users/:userId/timeline", (req, res) -> timeLine(req,res));
        post("followings", (req, res) -> followings(req,res));
        get("followings/:followerId/followees", (req, res) -> followees(req,res));
        get("users/:userId/wall", (req, res) -> wall(req,res));
        post("publications/:publicationId/like", (req, res) -> likePublication(req,res));
    }

    private String likePublication(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.likePublicationIdentifiedAs(
                        publicationIdFromParamsOf(request),requestBodyAsJson(request)),
                response);
    }

    private String publicationIdFromParamsOf(Request request) {
        return request.params(RestReceptionist.PUBLICATION_ID_KEY);
    }

    private String wall(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.wallOf(userIdFromParamsOf(request)),
                response);
    }

    private String followees(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.followersOf(followerIdFromParamsOf(request)),
                response);
    }

    private String followings(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.followings(requestBodyAsJson(request)),
                response);
    }

    private String timeLine(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.timelineOf(userIdFromParamsOf(request)),
                response);
    }

    private String publish(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.addPublication(userIdFromParamsOf(request), requestBodyAsJson(request)),
                response);
    }

    private String users(Response response) {
        return receptionistDo(()->receptionist.users(), response);
    }

    private String login(Request request, Response response) {
        return receptionistDo(
                () -> receptionist.login(requestBodyAsJson(request)),
                response);
    }

    private String registerUser(Request request, Response response) {
        return receptionistDo(
                ()-> receptionist.registerUser(requestBodyAsJson(request)),
                response);
    }

    private String receptionistDo(Supplier<ReceptionistResponse> action, Response response) {
        ReceptionistResponse receptionistResponse = action.get();
        response.status(receptionistResponse.status());
        response.type("application/json");

        return receptionistResponse.responseBody();
    }

    private String userIdFromParamsOf(Request request) {
        return request.params(RestReceptionist.USER_ID_KEY);
    }

    private String followerIdFromParamsOf(Request request) {
        return request.params(RestReceptionist.FOLLOWED_ID_KEY);
    }

    private JsonObject requestBodyAsJson(Request request) {
        return Json.parse(request.body()).asObject();
    }
}
