package org.example;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPointProvider;
import burp.api.montoya.utilities.Utilities;
import java.util.*;

public class CustomAuditInsertionPointProvider implements AuditInsertionPointProvider {

    MontoyaApi api;
    Logging logging;
    Utilities utils;
    EncodingTree root;
    List<HandleEncoding> encodings;
    EncodingFactory encodingFactory;

    public CustomAuditInsertionPointProvider(MontoyaApi montoyaApi) {
        encodingFactory = new EncodingFactory();
        api = montoyaApi;
        logging = api.logging();
        utils = api.utilities();

        // "Registering" the encodings
        encodings = new ArrayList<>();
        encodings.add(new B64Encoding());
        encodings.add(new JSONEncoding());
        encodings.add(new CommaSeparatedEncoding());
    }

    @Override
    public List<AuditInsertionPoint> provideInsertionPoints(HttpRequestResponse httpRequestResponse) {
        List<AuditInsertionPoint> auditInsertionPoints = new ArrayList<>();

        // Extract the parameters from the base Request-Response pair
        List<ParsedHttpParameter> parameters = httpRequestResponse.request().parameters();
        // Traversing the list
        for (ParsedHttpParameter parameter : parameters) {
            String url_decoded = utils.urlUtils().decode(parameter.value());
            root = fillTreeRecursively(root, url_decoded);
            List<EncodingTree> leafNodes = root.getLeafNodes();
            for (EncodingTree leafNode : leafNodes) {
                // Add the leaf nodes as custom insertion points
                auditInsertionPoints.add(new CustomInsertionPoint(api, leafNode, httpRequestResponse.request(), parameter));
            }
        }
        return auditInsertionPoints;
    }

    // Method to find the encoding of a String value
    private HandleEncoding findEncoding(String value, String key){
        HandleEncoding finalEncoding = null;
        boolean found = false;
        // Traverse through each implemented encoding class except No Encoding
        for (HandleEncoding encoding : encodings) {
            if (encoding.isApplicable(value)) {
                found = true;
                encoding.setValue(value);
                encoding.setKey(key);
                // Creating a new encoding with the value and it's key
                finalEncoding = encodingFactory.createNewEncoding(encoding);
            }
        }
        // If the String parameter didn't fit for either encoding
        // Then it's simply a String value
        if (!found) {
            finalEncoding = new NoEncoding();
            finalEncoding.setValue(value);
            finalEncoding.setKey(key);
        }
        return finalEncoding;
    }

    // Method to find the children of a node
    private EncodingTree findChildren(EncodingTree currentNode){
        HandleEncoding currentEncoding = currentNode.getNode();
        // Extracting the values
        List<KeyValueTuple> kvt = currentEncoding.getValues();
        // If the list is empty it means it doesn't have any children
        if (kvt.isEmpty()) {
            // So it's a leaf node
            currentNode.setLeaf(true);
        }else{
            // Otherwise traverse through its children
            // Find the encoding for every child and add them to the node as children
            for (KeyValueTuple keyValueTuple : kvt){
                currentNode.addChild(findEncoding(keyValueTuple.getValue(), keyValueTuple.getKey()));
            }
        }
        // Returning the current node with its children
        return currentNode;
    }

    // Recursive method to populate the tree
    private EncodingTree fillTreeRecursively(EncodingTree currentNode, String value){
        // Check if the current node is null
        // It means we are at the root
        if (currentNode == null){
            // Find the encoding for the current value
            HandleEncoding encoding = findEncoding(value, "");
            // Creating the node based on the encoding
            currentNode = new EncodingTree(encoding);
            // Finding its children
            currentNode = findChildren(currentNode);
            List<EncodingTree> children = currentNode.getChildren();
            for (EncodingTree node : children){
                // Recursively traverse each child
                fillTreeRecursively(node, node.getNode().getValue());
            }
        } else{
            currentNode = findChildren(currentNode);
            List<EncodingTree> children = currentNode.getChildren();
            for (EncodingTree node : children){
                fillTreeRecursively(node, node.getNode().getValue());
            }
        }
        return currentNode;
    }
}
