package org.example;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.Range;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class JSONInsertionPoint implements AuditInsertionPoint {
    String name;
    HttpRequest request;
    ParsedHttpParameter param;
    String baseValue;
    MontoyaApi api;

    public JSONInsertionPoint(MontoyaApi api, HttpRequest baseRequest, ParsedHttpParameter param) {
        this.api = api;
        this.name = param.name();
        this.request = baseRequest;
        this.param = param;
        // Extracting the base value using the baseRequest and the offsets
        baseValue = param.value();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String baseValue() {
        return baseValue;
    }

    @Override
    public HttpRequest buildHttpRequestWithPayload(ByteArray byteArray) {
        baseValue = api.utilities().urlUtils().decode(baseValue);
        JsonElement result = RecursiveJSONB64(baseValue(), byteArray.toString());
        api.logging().logToOutput("Modified payload is the following: " + result);
        String updatedParamValue = result.toString();
        updatedParamValue = api.utilities().urlUtils().encode(updatedParamValue);
        // creating the new HttpParameter
        HttpParameter updatedParam = HttpParameter.parameter(param.name(), updatedParamValue, param.type());
        // Send it!
        return request.withUpdatedParameters(updatedParam);
    }

    @Override
    public List<Range> issueHighlights(ByteArray byteArray) {
        return List.of();
    }

    public static JsonElement RecursiveJSONB64(String decoded, String payload) {
        JsonElement jsonElement = new JsonParser().parse(decoded);
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                JsonElement value = entry.getValue();

                if (value.isJsonPrimitive()) {
                    String valueString = value.toString();
                    String trimmed_valueString = valueString.replace("\"", "");
                    int b64RegexResult = JSONHelper.isBase64Encoded(trimmed_valueString);
                    if (b64RegexResult != -1) {
                        String decodedB64;
                        if (b64RegexResult == 0) {
                            decodedB64 = new String(Base64.getDecoder().decode(trimmed_valueString));
                        } else {
                            decodedB64 = new String(Base64.getUrlDecoder().decode(trimmed_valueString));
                        }

                        if (JSONHelper.mightBeJson(decodedB64)) {
                            JsonElement modifiedJsonElement = RecursiveJSONB64(decodedB64, payload);

                            String modifiedBase64 =
                                    Base64.getEncoder().encodeToString(modifiedJsonElement.toString().getBytes());
                            jsonObject.addProperty(entry.getKey(), modifiedBase64);
                        }
                    } else {
                        jsonObject.addProperty(entry.getKey(), payload);
                    }
                } else {
                    jsonObject.add(entry.getKey(), RecursiveJSONB64(value.toString(), payload));
                }
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement arrayElement = jsonArray.get(i);
                JsonElement modifiedElement = RecursiveJSONB64(arrayElement.toString(), payload);
                jsonArray.set(i, modifiedElement);
            }
        } else if (jsonElement.isJsonPrimitive()) {
            jsonElement = new JsonPrimitive(payload);
        }
        return jsonElement;
    }
}

