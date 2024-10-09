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

import static org.example.JSONInsertionPoint.RecursiveJSONB64;

public class B64InsertionPoint implements AuditInsertionPoint {
    String name;
    HttpRequest request;
    ParsedHttpParameter param;
    String baseValue;
    static MontoyaApi api;

    public B64InsertionPoint(MontoyaApi api, HttpRequest baseRequest, ParsedHttpParameter param) {
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
        String updatedParamValue = RecursiveB64JSON(baseValue(), byteArray.toString());
        api.logging().logToOutput("Modified result is the following: " + updatedParamValue);
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

    public static String RecursiveB64JSON(String b64Encoded, String payload) {
        String decoded;
        api.logging().logToOutput("RecursiveB64JSON reached");
        int b64RegexResult = JSONHelper.isBase64Encoded(b64Encoded);
        if (b64RegexResult == 0) {
            api.logging().logToOutput("The parameter is a default B64 encoded string");
            decoded = new String(Base64.getDecoder().decode(b64Encoded));
        } else {
            api.logging().logToOutput("URL-Safe B64 encoded");
            decoded = new String(Base64.getUrlDecoder().decode(b64Encoded));
        }
        if (JSONHelper.isBase64Encoded(decoded)!=-1){
            api.logging().logToOutput("B64 inside B64");
            decoded = RecursiveB64JSON(decoded, payload);
            api.logging().logToOutput(decoded);
        } else if (JSONHelper.mightBeJson(decoded)) {
            JsonElement jsonElement = RecursiveJSONB64(decoded, payload);
            return Base64.getEncoder().encodeToString(jsonElement.toString().getBytes());
        }
        return Base64.getEncoder().encodeToString(payload.getBytes());
    }
}

