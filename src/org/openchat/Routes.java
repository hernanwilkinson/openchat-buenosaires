package org.openchat;

import bsas.org.openchat.OpenChatSystem;
import bsas.org.openchat.ReceptionistResponse;
import bsas.org.openchat.RestReceptionist;
import spark.Request;
import spark.Response;

import static spark.Spark.get;
import static spark.Spark.post;

public class Routes {

    private RestReceptionist receptionist = new RestReceptionist(new OpenChatSystem());

    public void create() {
        openchatRoutes();
    }

    private void openchatRoutes() {
        get("status", (req, res) -> "OpenChat: OK!");
        post("users", (req, res) -> registerUser(req,res));
        post("login", (req, res) -> login(req,res));
        get("users", (req, res) -> "Implementar!");
        post("users/:userId/timeline", (req, res) -> "Implementar!");
        get("users/:userId/timeline", (req, res) -> "Implementar!");
        post("followings", (req, res) -> "Implementar!");
        get("followings/:followerId/followees", (req, res) -> "Implementar!");
        get("users/:userId/wall", (req, res) -> "Implementar!");
    }

    private String login(Request request, Response response) {
        ReceptionistResponse receptionistResponse = receptionist.login(request.body());

        response.status(receptionistResponse.status());
        response.type("application/json");
        return receptionistResponse.responseBody();
    }

    private String registerUser(Request request, Response response) {
        ReceptionistResponse receptionistResponse = receptionist.registerUser(request.body());

        response.status(receptionistResponse.status());
        response.type("application/json");
        return receptionistResponse.responseBody();
    }
}
