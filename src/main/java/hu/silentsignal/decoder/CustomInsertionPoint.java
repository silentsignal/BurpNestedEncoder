package hu.silentsignal.decoder;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.Range;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;
import hu.silentsignal.decoder.encodings.EncodingTree;

import java.util.List;

public class CustomInsertionPoint implements AuditInsertionPoint {
    private final MontoyaApi api;
    private final String baseValue;
    private final String name;
    private final HttpRequest request;
    private final ParsedHttpParameter parameter;
    private final EncodingTree node;
    private final boolean replace;

    public CustomInsertionPoint(MontoyaApi api, EncodingTree leafNode, HttpRequest request, ParsedHttpParameter param, boolean replace) {
        this.baseValue = leafNode.toString();
        this.name = param.name();
        this.request = request;
        this.parameter = param;
        this.node = leafNode;
        this.replace = replace;
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
        EncodingTree temp = node.updateNode(baseValue, byteArray.toString(), true, replace);
        temp = temp.findRoot(temp);
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
