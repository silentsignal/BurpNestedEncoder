package org.example;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.Range;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;

import java.util.List;

public class CustomInsertionPoint implements AuditInsertionPoint {
    private final MontoyaApi api;
    private final String baseValue;
    private final String name;
    private final HttpRequest request;
    private final ParsedHttpParameter parameter;
    private final EncodingTree node;

    public CustomInsertionPoint(MontoyaApi api, EncodingTree leafNode, HttpRequest request, ParsedHttpParameter param) {
        this.baseValue = leafNode.getNode().getValue();
        this.name = param.name();
        this.request = request;
        this.parameter = param;
        this.node = leafNode;
        this.api = api;
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
        api.logging().logToOutput("Node value before updating: " + node.getNode().getValue());
        EncodingTree temp = node.updateNode(baseValue, byteArray.toString());
        api.logging().logToOutput("Node value after updating: " + temp.getNode().getValue());
        temp = temp.findRoot(temp);
        api.logging().logToOutput("Root value after updating: " + temp.getNode().getValue());
        String newParamValue = api.utilities().urlUtils().encode(temp.getNode().getValue());
        HttpParameter updatedParam = HttpParameter.parameter(parameter.name(), newParamValue, parameter.type());
        api.logging().logToOutput("Sent out parameter is: " + newParamValue);
        return request.withParameter(updatedParam);
    }

    @Override
    public List<Range> issueHighlights(ByteArray byteArray) {
        return List.of();
    }
}
