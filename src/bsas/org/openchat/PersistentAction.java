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

    ReceptionistResponse persistAction(ReceptionistResponse response,
                                       Object[] parameters, ActionPersistentReceptionist actionPersistentReceptionist) {

        if(response.isSucessStatus()) {
            JsonObject actionAsJson = new JsonObject()
                    .add(ACTION_NAME_KEY, actionName)
                    .add(PARAMETERS_KEY, parametersToJsonObjectConverter.apply(parameters))
                    .add(RETURN_KEY, returnClosure.apply(response));

            try {
                writer.write(actionAsJson.toString());
                writer.write("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return response;
    }

}
