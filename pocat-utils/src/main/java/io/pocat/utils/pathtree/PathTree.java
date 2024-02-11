package io.pocat.utils.pathtree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PathTree<T> {
    private static final String ROOT_NODE_NAME = "";
    private final PathTreeNode<T> rootNode = new PathTreeNode<>(ROOT_NODE_NAME);
    private final Lock readLock;
    private final Lock writeLock;
    private String delimiter = "/";
    private String singleLevelWildCard = "*";
    private String multipleLevelWildCard = "#";

    public PathTree() {
        this(false);
    }

    public PathTree(boolean fairness) {
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock(fairness);
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setSingleLevelWildCard(String singleLevelWildCard) {
        this.singleLevelWildCard = singleLevelWildCard;
    }

    public void setMultipleLevelWildCard(String multipleLevelWildCard) {
        this.multipleLevelWildCard = multipleLevelWildCard;
    }

    public void addItem(String path, T item) throws InvalidPathException {
        String refinedPath = refinePath(path);
        String[] nodeNames = refinedPath.split(delimiter);

        writeLock.lock();
        try {
            PathTreeNode<T> node = addChildNode(rootNode, nodeNames, 0);
            node.setItem(item);
        } finally {
            writeLock.unlock();
        }
    }

    // /a/b/c
    private PathTreeNode<T> addChildNode(PathTreeNode<T> currentNode, String[] nodeNames, int index) {
        if(nodeNames.length == index) {
            return currentNode;
        }
        PathTreeNode<T> childNode;
        if(nodeNames[index].equals(singleLevelWildCard)) {
            if(currentNode.getSingleLevelNode() == null) {
                currentNode.setSingleLevelNode(new PathTreeNode<>(singleLevelWildCard));
            }
            childNode = currentNode.getSingleLevelNode();
        } else if(nodeNames[index].equals(multipleLevelWildCard)) {
            if(currentNode.getMultipleLevelNode() == null) {
                currentNode.setMultipleLevelNode(new PathTreeNode<>(multipleLevelWildCard));
            }
            childNode = currentNode.getMultipleLevelNode();
        } else if(!currentNode.hasChildNode(nodeNames[index])) {
            childNode = new PathTreeNode<>(nodeNames[index]);
            currentNode.addChildNode(childNode);
        } else {
            childNode = currentNode.getChildNode(nodeNames[index]);
        }
        return addChildNode(childNode, nodeNames, index+1);
    }

    public T findItem(String path) throws InvalidPathException {
        String refinedPath = refinePath(path);
        String[] nodeNames = refinedPath.split(delimiter);
        if(nodeNames.length == 0) {
            return null;
        }
        readLock.lock();
        try {
            return findChildNode(rootNode, nodeNames, 0);
        } finally {
            readLock.unlock();
        }
    }

    public List<T> findAll(String path) throws InvalidPathException {
        String refinedPath = refinePath(path);
        String[] nodeNames = refinedPath.split(delimiter);
        if(nodeNames.length == 0) {
            return null;
        }

        readLock.lock();
        try {
            return findAllChildNode(rootNode, nodeNames, 0);
        } finally {
            readLock.unlock();
        }
    }

    private List<T> findAllChildNode(PathTreeNode<T> currentNode, String[] nodeNames, int index) {
        List<T> gather = new ArrayList<>();
        if(nodeNames.length == index) {
            if(currentNode.getItem() != null) {
                gather.add(currentNode.getItem());
            }
            return gather;
        }
        if(currentNode.hasChildNode(nodeNames[index])) {
            PathTreeNode<T> childNode = currentNode.getChildNode(nodeNames[index]);
            gather.addAll(findAllChildNode(childNode, nodeNames, index+1));
        }
        if(currentNode.getSingleLevelNode() != null) {
            gather.addAll(findAllChildNode(currentNode.getSingleLevelNode(), nodeNames, index+1));
        }
        if(currentNode.getMultipleLevelNode() != null) {
            PathTreeNode<T> multipleNode = currentNode.getMultipleLevelNode();
            while(index < nodeNames.length) {
                gather.addAll(findAllChildNode(multipleNode, nodeNames, ++index));
            }
        }

        return gather;
    }

    private T findChildNode(PathTreeNode<T> currentNode, String[] nodeNames, int index) {
        if(nodeNames.length == index) {
            return currentNode.getItem();
        }

        T item = null;
        if(currentNode.hasChildNode(nodeNames[index])) {
            PathTreeNode<T> childNode = currentNode.getChildNode(nodeNames[index]);
            item = findChildNode(childNode, nodeNames, index+1);
        }
        if(item == null) {
            if(currentNode.getSingleLevelNode() != null) {
                item = findChildNode(currentNode.getSingleLevelNode(), nodeNames, index+1);
            }
            if(item == null) {
                if (currentNode.getMultipleLevelNode() != null) {
                    PathTreeNode<T> multipleNode = currentNode.getMultipleLevelNode();
                    while (item == null && index < nodeNames.length) {
                        item = findChildNode(multipleNode, nodeNames, ++index);
                    }
                    return item;
                }
            }
            return item;
        }
        PathTreeNode<T> childNode = currentNode.getChildNode(nodeNames[index]);
        return findChildNode(childNode, nodeNames, index+1);
    }

    private String refinePath(final String path) throws InvalidPathException {
        if(path == null || path.isEmpty()) {
            throw new InvalidPathException("Path is empty");
        }
        String refinedPath = path;
        if(refinedPath.startsWith(delimiter)) {
            refinedPath = refinedPath.substring(1);
        }
        if(refinedPath.endsWith(delimiter)) {
            refinedPath = refinedPath.substring(0, refinedPath.length()-1);
        }
        return refinedPath;
    }
}
