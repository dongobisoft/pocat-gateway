package io.pocat.utils.pathtree;

import java.util.HashMap;
import java.util.Map;

public class PathTreeNode<T> {
    private final String name;
    private final Map<String, PathTreeNode<T>> children = new HashMap<>();
    private PathTreeNode<T> singleMatchNode;
    private PathTreeNode<T> multipleMatchNode;
    private T item;

    public PathTreeNode(String name) {
        this.name = name;
    }

    public boolean hasChildNode(String nodeName) {
        return children.containsKey(nodeName);
    }

    public void addChildNode(PathTreeNode<T> pathTreeNode) {
        children.put(pathTreeNode.name, pathTreeNode);
    }

    public PathTreeNode<T> getChildNode(String nodeName) {
        return children.get(nodeName);
    }

    public void setItem(T item) {
        this.item = item;
    }

    public T getItem() {
        return item;
    }

    public PathTreeNode<T> getSingleLevelNode() {
        return singleMatchNode;
    }

    public void setSingleLevelNode(PathTreeNode<T> singleMatchNode) {
        this.singleMatchNode = singleMatchNode;
    }

    public PathTreeNode<T> getMultipleLevelNode() {
        return multipleMatchNode;
    }

    public void setMultipleLevelNode(PathTreeNode<T> multipleMatchNode) {
        this.multipleMatchNode = multipleMatchNode;
    }
}
