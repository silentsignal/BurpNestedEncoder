package hu.silentsignal.decoder.encodings;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

// Tree-like class to store encodings and their parent-child hierarchy
// Implements TreeModel in order to use JTree in the GUI
public class EncodingTree implements TreeModel {
    private HandleEncoding node;
    private final List<EncodingTree> children;
    private EncodingTree parent;
    private boolean isLeaf = false;
    private final EncodingHelper encodingHelper;

    public EncodingTree(){
        this.encodingHelper = new EncodingHelper();
        this.children = new ArrayList<>();
    }

    // Constructor
    public EncodingTree(HandleEncoding node) {
        encodingHelper = new EncodingHelper();
        this.node = encodingHelper.getEncodingFactory().createNewEncoding(node);
        children = new ArrayList<>();
    }

    // Constructor with an additional parent parameter
    public EncodingTree(HandleEncoding node, EncodingTree parent) {
        encodingHelper = new EncodingHelper();
        this.node = encodingHelper.getEncodingFactory().createNewEncoding(node);
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public EncodingTree(EncodingTree tree){
        encodingHelper = new EncodingHelper();
        this.children = new ArrayList<>();
        this.node = encodingHelper.getEncodingFactory().createNewEncoding(tree.getNode());
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
        EncodingTree childNode = new EncodingTree(encodingHelper.getEncodingFactory().createNewEncoding(child), this);
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
    public EncodingTree updateNode(String previousValue, String value, boolean isFirst, boolean replace){
        // Updating the node's value
        //this.node = encoding;
        EncodingTree ret = new EncodingTree(this);
        if (isFirst && !replace){
            ret.node.appendValue(value);
        }else{
            ret.node.setValue(value);
        }

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
            ret = parentCopy.updateNode(previousValue, parentCopy.node.getValue(), false, true);
        }
        return ret;
    }

    public EncodingTree findRoot(EncodingTree node){
        while(node != null && node.parent != null){
            node = node.parent;
        }
        return node;
    }

    // Recursive method to populate the tree
    public EncodingTree fillTreeRecursively(EncodingTree currentNode, String value){
        // Check if the current node is null
        // It means we are at the root
        if (currentNode.getNode() == null){
            // Find the encoding for the current value
            HandleEncoding encoding = encodingHelper.findEncoding(value, "");
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
                currentNode.addChild(encodingHelper.findEncoding(keyValueTuple.getValue(), keyValueTuple.getKey()));
            }
        }
        // Returning the current node with its children
        return currentNode;
    }

    public String toString(){
        return node.getValue();
    }

    @Override
    public Object getRoot() {
        return findRoot(this);
    }

    @Override
    public Object getChild(Object o, int i) {
        EncodingTree tree = (EncodingTree) o;
        return tree.children.get(i);
    }

    @Override
    public int getChildCount(Object o) {
        EncodingTree tree = (EncodingTree) o;
        return tree.children.size();
    }

    @Override
    public boolean isLeaf(Object o) {
        EncodingTree tree = (EncodingTree) o;
        return tree.isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath treePath, Object o) {

    }

    @Override
    public int getIndexOfChild(Object o, Object o1) {
        EncodingTree parent = (EncodingTree) o;
        EncodingTree child = (EncodingTree) o1;
        return parent.getChildren().indexOf(child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener treeModelListener) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener treeModelListener) {

    }
}
