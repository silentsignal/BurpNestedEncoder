package org.example;


import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.utilities.Utilities;

import java.util.List;

public class Main implements BurpExtension {

    MontoyaApi api;
    Logging logging;
    Utilities utils;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        api = montoyaApi;
        logging = api.logging();
        utils = api.utilities();
        api.extension().setName("Custom Insertion Point Provider");
        logging.logToOutput("Custom Insertion Point Provider");
        api.scanner().registerInsertionPointProvider(new CustomAuditInsertionPointProvider(api));
    }

    public static void main(String [] args) {
        if (args.length != 2) {
            System.out.println("Usage: ./test.sh 'dummy parameter value' 'dummy payload'");
        } else{
            Test test = new Test(args[0]);
            List<EncodingTree> modified = test.dummyBuildWithPayload(args[1]);
            for (EncodingTree node : modified) {
                System.out.println("Modified parameter is the following: \n\r" + node.getNode().getValue());
            }
        }
    }
}

