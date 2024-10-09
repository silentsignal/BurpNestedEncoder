package org.example;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPointProvider;
import burp.api.montoya.utilities.Utilities;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.*;
import javax.xml.*;

public class CustomAuditInsertionPointProvider implements AuditInsertionPointProvider {

    MontoyaApi api;
    Logging logging;
    Utilities utils;

    public CustomAuditInsertionPointProvider(MontoyaApi montoyaApi) {
        api = montoyaApi;
        logging = api.logging();
        utils = api.utilities();
    }

    @Override
    public List<AuditInsertionPoint> provideInsertionPoints(HttpRequestResponse httpRequestResponse) {
        List<AuditInsertionPoint> auditInsertionPoints = new ArrayList<>();

        // Extract the parameters from the base Request-Response pair
        List<ParsedHttpParameter> parameters = httpRequestResponse.request().parameters();
        // Traversing the list
        for (ParsedHttpParameter parameter : parameters) {
            String url_decoded = utils.urlUtils().decode(parameter.value());
            if (JSONHelper.mightBeJson(url_decoded)) {
                logging.logToOutput("It is JSON:" + url_decoded);
                auditInsertionPoints.add(new JSONInsertionPoint(api, httpRequestResponse.request(), parameter));
            } else if (JSONHelper.isBase64Encoded(url_decoded) != -1) {
                logging.logToOutput("It is B64");
                auditInsertionPoints.add(new B64InsertionPoint(api, httpRequestResponse.request(), parameter));
            }
        }
        return auditInsertionPoints;
    }
}
