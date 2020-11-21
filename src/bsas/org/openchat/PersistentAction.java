package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

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
}
