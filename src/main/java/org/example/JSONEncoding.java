package org.example;

import com.google.gson.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSONEncoding implements HandleEncoding{
    private String value;
    private Gson gson;
    private JsonElement jsonElement;
    private String originalValue;
    private String key;

    // Returns whether a value is JSON-like or not
    @Override
    public boolean isApplicable(String value) {
        return isJson(value);
    }

    // Not needed for JSON
    public String encodeValue(String value) {
        return value;
    }

    // Inserts a value into the JSON based on the KV Pair
    @Override
    public void insertPayload(String key, String value, int n) {
        JsonElement jsonElementCopy = jsonElement;
        updateJSON(key, value, jsonElementCopy);
        jsonElement = jsonElementCopy;
        this.value = jsonElement.toString();
    }

    // Setter for the value field
    @Override
    public void setValue(String value) {
        this.value = value;
        gson = new Gson();
        jsonElement = gson.fromJson(value, JsonElement.class);
    }

    // Returns the KV pairs in the JSON In a list
    @Override
    public List<KeyValueTuple> getValues() {
        List<KeyValueTuple> list = new ArrayList<>();
        traverseJSON(jsonElement, list);
        return list;
    }

    // Getter for the value field
    public String getValue() {
        return jsonElement.toString();
    }

    // Not needed here
    // A little trick to store the previous value
    @Override
    public int findElement(String value) {
        originalValue = value;
        return 0;
    }

    // Getter for the key
    @Override
    public String getKey() {
        return key;
    }

    // Setter for the key
    @Override
    public void setKey(String key) {
        this.key = key;
    }

    // Quick way to pre-check if a String value is a potential JSON object
    // Only parse after this method passed
    private boolean preCheckJson(String value) {
        int len = value.length();
        if (len < 2) return false;
        final char firstChar = value.charAt(0);
        return ((firstChar | (byte) 0x20) == (byte) 0x7b) &&
                (value.charAt(len - 1) == firstChar + 2);
    }

    // Removes any extra newlines and spaces
    // Returns the trimmed JSON String
    private String trimJson(String value) {
        return value.replace("\n", "").trim().replaceAll(" +", "");
    }

    // Checks whether a String might be JSON or not
    private boolean isJson(String value){
        String trimmedValue = trimJson(value);
        //Pre-check
        if (preCheckJson(trimmedValue)) {
            Gson gson = new Gson();
            try{
                // Trying to parse it using GSON
                gson.fromJson(trimmedValue, Object.class);
                return true;
            }catch (JsonSyntaxException jse){
                // If the parsing fails
                return false;
            }
        }else{
            // If pre-check fails
            return false;
        }
    }

    // Recursively traversing the JSON to extract the KV pairs
    // Since the structure is unknown we have to do it like this
    private void traverseJSON(JsonElement je, List<KeyValueTuple> list) {
        // Check if it's a JSON Object
        if (je.isJsonObject()) {
            JsonObject jsonObject = je.getAsJsonObject();
            // Traversing through the object
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                // If it's a pritimive we add the KV pair to our list
                if (entry.getValue().isJsonPrimitive()) {
                    list.add(new KeyValueTuple(entry.getKey(), entry.getValue().toString().replaceAll("\"", "")));
                }
                traverseJSON(entry.getValue(), list);
            }
            // If it's a JSON Array
        } else if (je.isJsonArray()) {
            // Parsing the Object as an Array
            JsonArray jsonArray = je.getAsJsonArray();
            // Traversing through its elements
            for (JsonElement element : jsonArray) {
                // Calling the recursive function on each element
                traverseJSON(element, list);
            }
        }
    }

    //Method to update the JSON Structure
    // The parameter "value" here is the modified value
    // The original one is stored in a private field
    // Basically the same as the traversing method
    private void updateJSON(String key, String value, JsonElement jsonElement){
        if (jsonElement.isJsonObject()){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()){
                if (entry.getValue().isJsonPrimitive()){
                    // If the key and the original value matches we update
                    if (entry.getKey().equals(key) && entry.getValue().getAsString().equals(originalValue)){
                        jsonElement.getAsJsonObject().addProperty(key, value);
                    }
                } else{
                    updateJSON(key, value, entry.getValue());
                }
            }
        } else if(jsonElement.isJsonArray()){
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                updateJSON(key, value, element);
            }
        }
    }
}
