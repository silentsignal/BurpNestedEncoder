package org.example;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.Map;

// Record to store JSON Key-Value pairs
// HashMap overwrites elder KV pairs if there's key duplicates with the latest one
// Since we are just traversing the JSON
class ValueKeyHolder {
    private String key;
    private String value;

    public ValueKeyHolder(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

public class JSONHelper {
    final private Gson gson = new Gson();
    private final JsonElement jsonElement;

    JSONHelper(String json) {
        String finalJson = trimJson(json);
        try {
            jsonElement = JsonParser.parseString(finalJson);
        } catch (JsonSyntaxException jse) {
            throw new JsonSyntaxException(jse);
        }
    }

    static public String trimJson(String json) {
        return json.replace("\n", "").trim().replaceAll(" +", "");
    }

    public JsonElement getJsonElement() {
        return jsonElement;
    }

    static public void traverseJSON(JsonElement je, ArrayList<ValueKeyHolder> list) {
        if (je.isJsonObject()) {
            JsonObject jsonObject = je.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    ValueKeyHolder vkh = new ValueKeyHolder(entry.getKey(), entry.getValue().getAsString());
                    list.add(vkh);
                }
                traverseJSON(entry.getValue(), list);
            }
        } else if (je.isJsonArray()) {
            JsonArray jsonArray = je.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                traverseJSON(element, list);
            }
        }
    }

    static JsonObject rebuildJson(JsonElement original, ArrayList<ValueKeyHolder> modifiedKV) {
        JsonObject newJson = new JsonObject();
        for (ValueKeyHolder kv : modifiedKV) {
            newJson.addProperty(kv.getKey(), kv.getValue());
        }
        return newJson;
    }

    static boolean mightBeJson(final String value) {
        final int len = value.length();
        if (len < 2) return false;
        final char firstChar = value.charAt(0);
        return ((firstChar | (byte) 0x20) == (byte) 0x7b) && // '[' = 0x5b, '{' = 0x7b, former missing bit 0x20
                (value.charAt(len - 1) == firstChar + 2);   // ']' = 0x5d, '}' = 0x7d, offset is 2 for both
    }

     static int isBase64Encoded(String string) {
        // Return is -1 if not B64
        // Return is 0 if normal B64
        // Return is 1 if URL-safe B64
        if (string.length() < 16) {
            return -1;
        }
        String b64Regex = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/][AQgw]==|[A-Za-z0-9+/]{2}[AEIMQUYcgkosw048]=)?$";
        String b64UrlRegex = "^[A-Za-z0-9_-]+$";
        if (string.matches(b64Regex)) {
            return 0;
        } else if (string.matches(b64UrlRegex)) {
            return 1;
        } else {
            return -1;
        }
    }
}
