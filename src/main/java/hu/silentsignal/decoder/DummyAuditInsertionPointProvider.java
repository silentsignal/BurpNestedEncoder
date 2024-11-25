package hu.silentsignal.decoder;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPointProvider;
import burp.api.montoya.utilities.Utilities;
import hu.silentsignal.decoder.encodings.EncodingTree;

import java.util.*;

// Dummy Audit Insertion Point Provider class used in the GUI

public class DummyAuditInsertionPointProvider implements AuditInsertionPointProvider {

    private MontoyaApi api;
    private Logging logging;
    private Utilities utils;
    private List<EncodingTree> nodes;
    private ParsedHttpParameter param;
    private boolean replace;

    public DummyAuditInsertionPointProvider(MontoyaApi montoyaApi, List<EncodingTree> nodes, ParsedHttpParameter param, boolean replace) {
        api = montoyaApi;
        logging = api.logging();
        utils = api.utilities();
        this.nodes = nodes;
        this.param = param;
        this.replace = replace;
    }

    @Override
    public List<AuditInsertionPoint> provideInsertionPoints(HttpRequestResponse httpRequestResponse) {
        List<AuditInsertionPoint> auditInsertionPoints = new ArrayList<>();
            for (EncodingTree node : nodes) {
                // Creating CustomInsertionPoint instances from the nodes
                // And adding them to the to-be returned list
                auditInsertionPoints.add(new CustomInsertionPoint(api, node, httpRequestResponse.request(), param, replace));
            }
        return auditInsertionPoints;
        }
    }

