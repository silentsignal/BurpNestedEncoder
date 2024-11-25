package hu.silentsignal.decoder;

import hu.silentsignal.decoder.encodings.EncodingTree;

import java.util.ArrayList;
import java.util.List;

// Class to simulate the behaviour of Burp
// Used for tests

public class BurpSimulation {
    private String paramValue;
    private EncodingTree root;

    public BurpSimulation(){
        this.root = new EncodingTree();
    }

    void setParamValue(String paramValue){
        this.paramValue = paramValue;
    }

    public List<EncodingTree> dummyBuildWithPayload(String payload){
        List<EncodingTree> encodingTrees = new ArrayList<>();
        root = root.fillTreeRecursively(root, paramValue);
        List<EncodingTree> leafNodes = root.getLeafNodes();
        for (EncodingTree leafNode : leafNodes) {
            EncodingTree temp = leafNode.updateNode(leafNode.getNode().getValue(), payload, true, true);
            temp = temp.findRoot(temp);
            encodingTrees.add(temp);
        }
        return encodingTrees;
    }
}
