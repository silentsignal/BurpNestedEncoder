package org.example;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.Range;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;

import java.util.List;

public class JSONB64InsertionPoint implements AuditInsertionPoint {
    String name;
    HttpRequest request;
    int startIndexInclusive;
    int endIndexExclusive;
    ParsedHttpParameter param;
    String baseValue;
    MontoyaApi api;

    public JSONB64InsertionPoint(MontoyaApi api, String name, HttpRequest baseRequest,
                                 int startIndexInclusive, int endIndexExclusive, ParsedHttpParameter param) {
        this.api = api;
        this.name = name;
        this.request = baseRequest;
        this.startIndexInclusive = startIndexInclusive;
        this.endIndexExclusive = endIndexExclusive;
        this.param = param;
        // Extracting the base value using the baseRequest and the offsets
        baseValue = param.value().substring(startIndexInclusive, endIndexExclusive);
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
        // B64 encode the payload
        String b64_payload = api.utilities().base64Utils().encodeToString(byteArray);
        // URL-encode the B64-encoded payload
        b64_payload = api.utilities().urlUtils().encode(b64_payload);
        // Crafting the new parameter value
        String updatedParamValue = param.value().substring(0, startIndexInclusive) + b64_payload + param.value().substring(endIndexExclusive);
        // creating the new HttpParameter
        HttpParameter updatedParam = HttpParameter.parameter(param.name(), updatedParamValue, param.type());
        api.logging().logToOutput(request.withUpdatedParameters(updatedParam).toString());

        // Send it!
        return request.withUpdatedParameters(updatedParam);
    }

    @Override
    public List<Range> issueHighlights(ByteArray byteArray) {
        return List.of();
    }
}

