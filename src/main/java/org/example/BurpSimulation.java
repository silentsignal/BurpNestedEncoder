package org.example;

import java.util.ArrayList;
import java.util.List;

public class Test {
    private final String paramValue;
    private EncodingTree root;

    public Test(String paramValue){
        this.paramValue = paramValue;
        this.root = new EncodingTree();
    }

    public List<EncodingTree> dummyBuildWithPayload(String payload){
        List<EncodingTree> encodingTrees = new ArrayList<>();
        root = root.fillTreeRecursively(root, paramValue);
        System.out.print("Detected structure is the following: \r\n");
        System.out.println(root.toString());
        List<EncodingTree> leafNodes = root.getLeafNodes();
        for (EncodingTree leafNode : leafNodes) {
            EncodingTree temp = leafNode.updateNode(leafNode.getNode().getValue(), payload);
            temp = temp.findRoot(temp);
            encodingTrees.add(temp);
        }
        return encodingTrees;
    }
}
