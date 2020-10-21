package org.openchat;

import bsas.org.openchat.OpenChatSystem;
import bsas.org.openchat.ReceptionistResponse;
import bsas.org.openchat.RestReceptionist;
import com.eclipsesource.json.Json;
import spark.Request;
import spark.Response;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static spark.Spark.get;
import static spark.Spark.post;

public class Routes {

    private RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem(
            ()-> LocalDateTime.now()));

    public void create() {
        openchatRoutes();
    }

    private void openchatRoutes() {
        get("status", (req, res) -> "OpenChat: OK!");
        post("users", (req, res) -> registerUser(req,res));
        post("login", (req, res) -> login(req,res));
        get("users", (req, res) -> users(req,res));
        post("users/:userId/timeline", (req, res) -> publish(req,res));
        get("users/:userId/timeline", (req, res) -> timeLine(req,res));
        post("followings", (req, res) -> followings(req,res));
        get("followings/:followerId/followees", (req, res) -> followees(req,res));
        get("users/:userId/wall", (req, res) -> wall(req,res));
    }

    private String wall(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.wallOf(userIdFrom(request)),
                response);
    }

    private String userIdFrom(Request request) {
        return request.params("userId");
    }

    private String followees(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.followeesOf(request.params("followerId")),
                response);
    }

    private String followings(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.followings(Json.parse(request.body()).asObject()),
                response);
    }

    private String timeLine(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.timelineOf(userIdFrom(request)),
                response);
    }

    private String publish(Request request, Response response) {
        return receptionistDo(
                ()->receptionist.addPublication(userIdFrom(request), Json.parse(request.body()).asObject()),
                response);
    }

    private String users(Request request, Response response) {
        return receptionistDo(()->receptionist.users(), response);
    }

    private String login(Request request, Response response) {
        return receptionistDo(() -> receptionist.login(Json.parse(request.body()).asObject()), response);
    }

    private String registerUser(Request request, Response response) {
        return receptionistDo(()-> receptionist.registerUser(Json.parse(request.body()).asObject()), response);
    }

    private String receptionistDo(Supplier<ReceptionistResponse> action, Response response) {
        ReceptionistResponse receptionistResponse = action.get();
        response.status(receptionistResponse.status());
        response.type("application/json");

        return receptionistResponse.responseBody();
    }

}
