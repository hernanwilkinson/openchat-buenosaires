package bsas.org.openchat;

import com.eclipsesource.json.JsonObject;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Function;

public class PersistentAction {
    public static final String ACTION_NAME_KEY = "actionName";
    public static final String PARAMETERS_KEY = "parameters";
    public static final String RETURN_KEY = "return";

    private final String actionName;
    private final Function<ReceptionistResponse, JsonObject> returnClosure;
    private final Writer writer;
    private Function<Object[], JsonObject> parametersToJsonObjectConverter;

    public PersistentAction(String actionName, Function<ReceptionistResponse, JsonObject> returnClosure, Writer writer) {
        this(actionName,returnClosure, writer, args->(JsonObject) args[0]);
    }

    public PersistentAction(String actionName, Function<ReceptionistResponse, JsonObject> returnClosure, Writer writer, Function<Object[], JsonObject> parametersToJsonObjectConverter) {
        this.actionName = actionName;
        this.returnClosure = returnClosure;
        this.writer = writer;
        this.parametersToJsonObjectConverter = parametersToJsonObjectConverter;
    }

    public void persist(ReceptionistResponse response, Object[] parameters) {
        if(response.isSucessStatus())
            persistActionAsJson(createActionAsJson(response, parameters));
    }

    private void persistActionAsJson(JsonObject actionAsJson) {
        try {
            writer.write(actionAsJson.toString());
            writer.write("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonObject createActionAsJson(ReceptionistResponse response, Object[] parameters) {
        return new JsonObject()
                .add(ACTION_NAME_KEY, actionName)
                .add(PARAMETERS_KEY, parametersToJsonObjectConverter.apply(parameters))
                .add(RETURN_KEY, returnClosure.apply(response));
    }

}
