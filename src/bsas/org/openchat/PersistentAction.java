package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

import java.io.IOException;
import java.util.function.Function;

public class PersistentAction {
    private final String actionName;
    private final Function<ReceptionistResponse, JsonObject> returnClosure;

    public PersistentAction(String actionName, Function<ReceptionistResponse, JsonObject> returnClosure) {
        this.actionName = actionName;
        this.returnClosure = returnClosure;
    }

    public String getActionName() {
        return actionName;
    }

    public Function<ReceptionistResponse, JsonObject> getReturnClosure() {
        return returnClosure;
    }

    ReceptionistResponse persistAction(ReceptionistResponse response,
                                       JsonObject parameters, ActionPersistentReceptionist actionPersistentReceptionist) {

        if(response.isSucessStatus()) {
            JsonObject actionAsJson = new JsonObject()
                    .add(ActionPersistentReceptionist.ACTION_NAME_KEY, getActionName())
                    .add(ActionPersistentReceptionist.PARAMETERS_KEY, parameters)
                    .add(ActionPersistentReceptionist.RETURN_KEY, getReturnClosure().apply(response));

            try {
                actionPersistentReceptionist.writer.write(actionAsJson.toString());
                actionPersistentReceptionist.writer.write("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return response;
    }
}
