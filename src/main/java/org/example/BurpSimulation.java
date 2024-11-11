package org.example;

import java.util.ArrayList;
import java.util.List;

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
            EncodingTree temp = leafNode.updateNode(leafNode.getNode().getValue(), payload);
            temp = temp.findRoot(temp);
            encodingTrees.add(temp);
        }
        return encodingTrees;
    }
}
