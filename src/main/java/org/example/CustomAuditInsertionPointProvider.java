package org.example;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPointProvider;
import burp.api.montoya.utilities.Utilities;
import com.google.gson.*;

import java.util.*;



// Record to store JSON Key-Value pairs
// HashMap overwrites elder KV pairs if there's key duplicates with the latest one
// Since we are just traversing the JSON
record ValueKeyHolder(String key, String value) {
}

public class CustomAuditInsertionPointProvider implements AuditInsertionPointProvider {

    // Quick pre-check for JSON
    static boolean mightBeJson(final String value) {
        final int len = value.length();
        if (len < 2) return false;
        final char firstChar = value.charAt(0);
        return
                ((firstChar | (byte)0x20) == (byte)0x7b) && // '[' = 0x5b, '{' = 0x7b, former missing bit 0x20
                        (value.charAt(len - 1) == firstChar + 2);   // ']' = 0x5d, '}' = 0x7d, offset is 2 for both
    }

    // Traversing the JSON structure recursively
    static void traverse(JsonElement je, ArrayList<ValueKeyHolder> list){
        if (je.isJsonObject()){
            JsonObject jsonObject = je.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()){
                if (entry.getValue().isJsonPrimitive()){
                    ValueKeyHolder vkh = new ValueKeyHolder(entry.getKey(), entry.getValue().getAsString());
                    list.add(vkh);
                }
                traverse(entry.getValue(), list);
            }
        }else if (je.isJsonArray()){
            JsonArray jsonArray = je.getAsJsonArray();
            for (JsonElement element : jsonArray){
                traverse(element, list);
            }
        }
    }

    MontoyaApi api;
    Logging logging;
    Utilities utils;

    public CustomAuditInsertionPointProvider(MontoyaApi montoyaApi) {
        this.api = montoyaApi;
        logging = api.logging();
        utils = api.utilities();
    }

    @Override
    public List<AuditInsertionPoint> provideInsertionPoints(HttpRequestResponse httpRequestResponse) {
        List<AuditInsertionPoint> auditInsertionPoints = new ArrayList<>();

        // Extract the parameters from the base Request-Response pair
        List<ParsedHttpParameter> parameters = httpRequestResponse.request().parameters();
        // Traversing the list
        for (int i = 0; i < parameters.size(); i++) {
            // URL-decode the parameter's value, trimming it, removing extra spaces and newlines
            String url_decoded = utils.urlUtils().decode(parameters.get(i).value());
            url_decoded = url_decoded.replace("\n", "");
            url_decoded = url_decoded.trim().replace(" +", "");
            // Pre-check
            if (mightBeJson(url_decoded)) {
                JsonElement jsonElement = JsonParser.parseString(url_decoded);
                ArrayList<ValueKeyHolder> values = new ArrayList<>();
                traverse(jsonElement, values);

                for (int j = 0; j < values.size(); j++) {
                    String val_ud = values.get(j).value();
                    val_ud = val_ud.replace("\"", "");
                    // B64 Check with regex
                    if (val_ud.matches("^[-A-Za-z0-9+/]*={0,3}$")) {
                        logging.logToOutput("Value: " + values.get(j).value());
                        logging.logToOutput("Parameter value: " + parameters.get(i).value());
                        // Get the start offset for the B64 value
                        int startOffset = parameters.get(i).value().indexOf(utils.urlUtils().encode(values.get(j).value()));
                        // Get the end offset for the B64 value
                        int endOffset = startOffset + values.get(j).value().length();
                        logging.logToOutput("Start: " + startOffset);
                        logging.logToOutput("End: " + endOffset);
                        // Create a new insertion point for the KV pair
                        auditInsertionPoints.add
                                (new JSONB64InsertionPoint(api, values.get(j).key(), httpRequestResponse.request(),
                                        startOffset, endOffset, parameters.get(i)));
                    }
                }
            }
        }
        return auditInsertionPoints;
    }
}
