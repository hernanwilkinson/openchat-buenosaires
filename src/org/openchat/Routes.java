package org.openchat;

import bsas.org.openchat.OpenChatSystem;
import bsas.org.openchat.ReceptionistResponse;
import bsas.org.openchat.RestReceptionist;
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
        post("users/:userId/timeline", (req, res) -> "Implementar!");
        get("users/:userId/timeline", (req, res) -> "Implementar!");
        post("followings", (req, res) -> "Implementar!");
        get("followings/:followerId/followees", (req, res) -> "Implementar!");
        get("users/:userId/wall", (req, res) -> "Implementar!");
    }

    private String users(Request request, Response response) {
        return receptionistDo(()->receptionist.users(), response);
    }

    private String login(Request request, Response response) {
        return receptionistDo(() -> receptionist.login(request.body()), response);
    }

    private String registerUser(Request request, Response response) {
        return receptionistDo(()-> receptionist.registerUser(request.body()), response);
    }

    private String receptionistDo(Supplier<ReceptionistResponse> action, Response response) {
        ReceptionistResponse receptionistResponse = action.get();

        return receptionistResponse.toResponseInto(response);
    }

}
