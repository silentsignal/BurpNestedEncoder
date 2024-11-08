package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Tree-like class to store encodings and their parent-child hierarchy
public class EncodingTree implements Serializable {
    private final HandleEncoding node;
    private final List<EncodingTree> children;
    private EncodingTree parent;
    private boolean isLeaf = false;
    private final EncodingFactory encodingFactory;

    // Constructor
    public EncodingTree(HandleEncoding node) {
        encodingFactory = new EncodingFactory();
        this.node = encodingFactory.createNewEncoding(node);
        children = new ArrayList<>();
    }

    // Constructor with an additional parent parameter
    public EncodingTree(HandleEncoding node, EncodingTree parent) {
        encodingFactory = new EncodingFactory();
        this.node = encodingFactory.createNewEncoding(node);
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public EncodingTree(EncodingTree tree){
        this.encodingFactory = new EncodingFactory();
        this.children = new ArrayList<>();
        this.node = encodingFactory.createNewEncoding(tree.getNode());
        if (tree.getParent() != null) {
            this.parent = tree.getParent();
        }
        this.isLeaf = tree.isLeaf();
        for (EncodingTree child : tree.getChildren()){
            children.add(new EncodingTree(child));
        }
    }

    // Adding a child to the node
    public void addChild(HandleEncoding child) {
        EncodingTree childNode = new EncodingTree(encodingFactory.createNewEncoding(child), this);
        children.add(childNode);
    }

    // Getter for the node field
    public HandleEncoding getNode() {
        return node;
    }

    // Getter for the children field
    public List<EncodingTree> getChildren() {
        return children;
    }

    // Returns whether a node is a leaf (doesn't have any children)
    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    // Getter for the parent field
    public EncodingTree getParent() {
        return parent;
    }

    // Recursive method to find the leaf nodes
    public List<EncodingTree> getLeafNodes(){
        // List init
        List<EncodingTree> leafNodes = new ArrayList<>();
        // Add the node to the list if it's a leaf
        if (isLeaf) {
            leafNodes.add(this);
            // Otherwise check for it's children
        }else{
            for (EncodingTree child : children) {
                // Calling the recursion function on every child
                leafNodes.addAll(child.getLeafNodes());
            }
        }
        return leafNodes;
    }

    public boolean hasParent() {
        return parent != null;
    }

    // Recursive function to update the nodes after inserting the payload
    public EncodingTree updateNode(String previousValue, String value){
        // Updating the node's value
        //this.node = encoding;
        EncodingTree ret = new EncodingTree(this);
        ret.node.setValue(value);
        // If it has a parent, update it accordingly
        if (hasParent()){
            // Finds the original value in the parent
            EncodingTree parentCopy = new EncodingTree(parent);
            int elemIndex = parentCopy.node.findElement(parentCopy.node.encodeValue((previousValue)));
            // Saving the parent's original value
            previousValue = parentCopy.node.getValue();
            // Updating the parent
            parentCopy.node.insertPayload(ret.node.getKey(), ret.node.getValue(), elemIndex);
            // Updating the parent
            ret = parentCopy.updateNode(previousValue, parentCopy.node.getValue());
        }
        return ret;
    }

    public EncodingTree findRoot(EncodingTree node){
        while(node != null && node.parent != null){
            node = node.parent;
        }
        return node;
    }
}
