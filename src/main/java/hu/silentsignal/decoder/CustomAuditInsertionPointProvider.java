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

public class CustomAuditInsertionPointProvider implements AuditInsertionPointProvider {

    MontoyaApi api;
    Logging logging;
    Utilities utils;
    EncodingTree root;

    public CustomAuditInsertionPointProvider(MontoyaApi montoyaApi) {
        api = montoyaApi;
        logging = api.logging();
        utils = api.utilities();
        root = new EncodingTree();
    }

    @Override
    public List<AuditInsertionPoint> provideInsertionPoints(HttpRequestResponse httpRequestResponse) {
        List<AuditInsertionPoint> auditInsertionPoints = new ArrayList<>();

        // Extract the parameters from the base Request-Response pair
        List<ParsedHttpParameter> parameters = httpRequestResponse.request().parameters();
        // Traversing the list
        for (ParsedHttpParameter parameter : parameters) {
            String url_decoded = utils.urlUtils().decode(parameter.value());
            root = root.fillTreeRecursively(root, url_decoded);
            List<EncodingTree> leafNodes = root.getLeafNodes();
            for (EncodingTree leafNode : leafNodes) {
                // Add the leaf nodes as custom insertion points
                // The default payload inserting method is to replace the leaf nodes value with the payload
                auditInsertionPoints.add(new CustomInsertionPoint(api, leafNode, httpRequestResponse.request(), parameter, true));
            }
        }
        return auditInsertionPoints;
    }
}
