package convex_layers.prior_tree;

import convex_layers.InputVertex;
import lombok.*;
import tools.Pair;
import tools.Var;
import tools.data.array.ArrayTools;
import tools.log.Logger;
import tools.log.StreamLogger;

import java.util.*;

public class PriorTree<T extends PriorTreeNode<T>>
            implements Iterable<T> {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The root node of the tree. */
    private Node<T> root;
    
    
    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    /**
     * Data class for the nodes used in the priority tree.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @RequiredArgsConstructor
    protected static class Node<T extends PriorTreeNode<T>>
            implements PriorTreeNode<Node<T>> {
        /** The data of the node */
        final T data;
        /** The parent of this node. */
        private Node parent;
        /** The left child of the node. */
        private Node left;
        /** The right child of the node. */
        private Node right;
        /** The y-coordinate this node splits on. */
        private double ySplit;
        
        
        @Override
        public double getX() {
            return data.getX();
        }
        
        @Override
        public double getY() {
            return data.getY();
        }
        
        @Override
        public int hashCode() {
            return (data == null ? 0 : data.hashCode());
        }
        
        @Override
        public boolean equals(Object obj) { // TODO
            if (!(obj instanceof Node)) return false;
            else return Objects.equals(data, ((Node) obj).data);
        }
        
        @Override
        public String toString() {
            return getClass().getCanonicalName() + "[" + Var.LS +
                    "    data  : " + (data == null ? "null" : data.toString()) + Var.LS +
                    "    parent: " + (parent == null ? "null" : parent.toString()) + Var.LS +
                    "    left  : " + (left == null ? "null" : left.toString()) + Var.LS +
                    "    right : " + (right == null ? "null" : right.toString()) + Var.LS +
                    "]";
        }
        
        
    }
    
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Elem<T extends PriorTreeNode<T>> {
        private Node<T> node;
        private Node<T>[] leftX;
        private Node<T>[] leftY;
        private Node<T>[] rightX;
        private Node<T>[] rightY;
        
        
    }
    
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /**
     * @apiNote Runs in {@code n*log(n)}.
     * 
     * @param nodes The nodes to be added.
     */
    @SuppressWarnings("unchecked")
    public PriorTree(Collection<T> nodes) {
        if (nodes.isEmpty()) return;
        if (nodes.size() == 1) {
            root = new Node(nodes.iterator().next());
            return;
        }
        // Preprocess data.
        Node<T>[] xData = new Node[nodes.size()];
        {
            int i = 0;
            for (T elem : nodes) {
                xData[i++] = new Node<>(elem);
            }
        }
        Node<T>[] yData = Arrays.copyOf(xData, xData.length);
        Arrays.sort(xData, Node::compareToX);
        Arrays.sort(yData, Node::compareToY);
        Logger.write("xData: " + ArrayTools.toDeepString(xData));
        Logger.write("yData: " + ArrayTools.toDeepString(yData));
        Logger.write(Var.LS);
        Logger.write("------------------------------------------------");
        // Build the tree.
        
        List<Elem<T>> prevState = new ArrayList<Elem<T>>();
        List<Elem<T>> nextState = null;
        prevState.add(new Elem(null, xData, yData, null, null));
        
        while (!prevState.isEmpty()) {
            nextState = new ArrayList<>();
            for (Elem<T> elem : prevState) {
                for (int side = 0; side < 2; side++) {
                    Node<T>[] xArr = (side == 0 ? elem.leftX : elem.rightX);
                    Node<T>[] yArr = (side == 0 ? elem.leftY : elem.rightY);
                    Node<T> node = elem.node;
                    
                    // Note that the xArr and yArr contain the same elements.
                    if (xArr == null || xArr.length == 0) continue;
                    
                    Node<T> first = xArr[0];
                    if (side == 0) setLeft(node, first);
                    else setRight(node, first);
                    
                    int medianIndex = (yArr.length - 1) / 2;
                    Node<T> median = yArr[medianIndex];
                    Node<T>[] newLeftY = new Node[medianIndex];
                    Node<T>[] newRightY = new Node[yArr.length - 1 - medianIndex];
                    int srcI = 0;
                    for (int dstI = 0; srcI < yArr.length && dstI < newLeftY.length; srcI++) {
                        if (yArr[srcI] != first) {
                            newLeftY[dstI++] = yArr[srcI];
                        }
                    }
                    for (int dstI = 0; srcI < yArr.length && dstI < newRightY.length; srcI++) {
                        if (yArr[srcI] != first) {
                            newRightY[dstI++] = yArr[srcI];
                        }
                    }
                    
                    Node<T>[] newLeftX = new Node[newLeftY.length];
                    Node<T>[] newRightX = new Node[newRightY.length];
                    for (int i = 0, leftI = 0, rightI = 0; i < xArr.length; i++) {
                        if (xArr[i] == first) {
                            xArr[i].ySplit = median.getY();
                        } else if (xArr[i].getY() < median.getY()) {
                            newLeftX[leftI++] = xArr[i];
                        } else {
                            newRightX[rightI++] = xArr[i];
                        }
                    }
                    /*
                    Set<Node<T>> leftSet = new HashSet<>();
                    List<Node<T>> newLeftXList = new ArrayList<>();
                    List<Node<T>> newRightXList = new ArrayList<>();
                    for (int i = 0, j = 0; i < xArr.length; i++, j++) {
                        if (xArr[i] == first) {
                            xArr[i].ySplit = median.getY();
                            
                        } else if (xArr[i].getY() < median.getY()) {
                            newLeftXList.add(xArr[i]);
                            leftSet.add(xArr[i]);
                            
                        } else {
                            newRightXList.add(xArr[i]);
                        }
                    }
                    
                    Node<T>[] newLeftY = new Node[newLeftXList.size()];
                    Node<T>[] newRightY = new Node[newRightXList.size()];
                    int leftI = 0;
                    int rightI = 0;
                    for (int i = 0; i < yArr.length; i++) {
                        Logger.write(leftSet.contains(yArr[i]));
                        if (leftSet.contains(yArr[i])) {
                            newLeftY[leftI++] = yArr[i];
                        } else {
                            newRightY[rightI++] = yArr[i];
                        }
                    }*/
                    
                    nextState.add(new Elem<>(
                            first,
                            //newLeftXList.toArray(new Node[newLeftXList.size()]),
                            newLeftX,
                            newLeftY,
                            //newRightXList.toArray(new Node[newRightXList.size()]),
                            newRightX,
                            newRightY
                    ));
                }
            }
            
            prevState = nextState;
            nextState = null;
        }
        
        
        /*
        List<Node<T>[]> curXData = List.<Node<T>[]>of(xData);
        List<Node<T>[]> nextXData = new ArrayList<>();
        
        //Node<T>[] curYData = yData;
        //Node<T>[] nextYData = null;
        List<Node<T>[]> curYData = List.<Node<T>[]>of(yData);
        List<Node<T>[]> nextYData = null;
        
        Node<T>[] prevNodes = new Node[1];
        Node<T>[] curNodes = null;
        int numMedians = 1;
        int numElems = xData.length;
        while (numElems > numMedians) { // TODO
            // Find and remove all nodes to extract in this iteration
            // from the x-dataset and set the hierarchy accordingly.
            curNodes = new Node[numMedians];
            for (int i = 0; i < curXData.size(); i++) {
                Node<T>[] arr = curXData.get(i);
                if (arr != null && arr.length > 0) {
                    curNodes[i] = arr[i];
                    if (i % 2 == 0) {
                        setLeft(prevNodes[i / 2], arr[i]);
                    } else {
                        setRight(prevNodes[i / 2], arr[i]);
                    }
                }
            }
            if (root == null) root = curNodes[0];
            
            // Remove all nodes to extract in this iteration from the y-dataset
            // and find the medians. Then set the median y-coordinates for these nodes.
            Node<T>[] medians = new Node[numMedians];
            nextYData = new ArrayList<Node<T>[]>(); ///[curYData.length - curNodes.length];
            //int[] medianIndices = getMedianIndices(numMedians, nextYData.length);
            //Set<Node<T>> toRemove = new HashSet<>(Arrays.asList(curNodes));
            
            for (int i = 0; i < curYData.size(); i++) {
                Node<T>[] arr = curYData.get(i);
                if (arr == null || arr.length == 0) continue;
                
            }
            
            /*
            int medianIndex = 0;
            int relIndex = 0;
            for (int i = 0; i < curYData.length; i++) {
                if (medianIndex < medianIndices.length && i == medianIndices[medianIndex]) {
                    medians[medianIndex] = curYData[i];
                    //curNodes[medianIndex++].ySplit = curYData[i].getY();
                }
                if (toRemove.contains(curYData[i])) {
                    relIndex++;
                } else {
                    nextYData[i - relIndex] = curYData[i];
                }
            }*//*
            
            // Split each array of the x-dataset into two using the medians.
            for (int i = 0; i < curXData.size(); i++) {
                Node<T>[] arr = curXData.get(i);
                if (arr == null || arr.length == 0) {
                    nextXData.add(null);
                    nextXData.add(null);
                }
                
                List<Node<T>> left = new ArrayList<>(arr.length / 2 + 1);
                List<Node<T>> right = new ArrayList<>(arr.length / 2 + 1);
                for (int j = 0; j < arr.length; j++) {
                    if (j == 0) arr[0].ySplit = medians[i].getY();
                    else if (arr[j].getY() < arr[0].ySplit) left.add(arr[j]);
                    else right.add(arr[j]);
                }
                nextXData.add(left.toArray(new Node[left.size()]));
                nextXData.add(right.toArray(new Node[right.size()]));
            }
            
            // Update the values for the next round.
            prevNodes = curNodes;
            curNodes = null;
            
            curXData = nextXData;
            nextXData = new ArrayList<>();
            
            curYData = nextYData;
            nextYData = null;
            
            numElems -= numMedians;
            numMedians *= 2;
        }
        
        Logger.write("HERE (0)");
        Logger.write(debug());
        Logger.write(ArrayTools.toDeepString(prevNodes));
        Logger.write("HERE (1)");
        for (int i = 0; i < prevNodes.length; i++) {
            if (prevNodes[i] == null) continue;
            Node<T>[] arr = curXData.get(2*i);
            Node<T> left = unwrap(arr, 0);
            if (left != null) {
                setLeft(prevNodes[i], left);
                left.ySplit = left.getY();
            }
            Node<T> right = unwrap(arr, 1);
            if (right != null) {
                setRight(prevNodes[i], right);
                right.ySplit = left.getY();
            }
        }*/
    }
    
    protected String debug() {
        StringBuilder sb = new StringBuilder();
        Stack<Pair<Node<T>, Boolean>> stack = new Stack<>();
        stack.push(new Pair<>(root, false));
        stack.push(new Pair<>(root, true));
        while (!stack.isEmpty()) {
            Pair<Node<T>, Boolean> item = stack.pop();
            Node<T> node = item.getFirst();
            if (item.getSecond()) {
                sb.append(node);
                sb.append(Var.LS);
            } else {
                if (node.left != null) {
                    stack.push(new Pair<>(node, false));
                    stack.push(new Pair<>(node, true));
                }
                if (node.right != null) {
                    stack.push(new Pair<>(node, false));
                    stack.push(new Pair<>(node, true));
                }
            }
        }
        
        return sb.toString();
    }
    
    private static <T> T unwrap(T[] arr, int i) {
        if (arr == null) return null;
        if (i >= arr.length) return null;
        return arr[i];
    }
    
    public T getRoot() {
        return (root == null ? null : root.data);
    }

    /**
     * @apiNote Runs in {@code O(amt)}.
     *
     * @param amt    The total number of medians.
     * @param length The length of the array.
     * 
     * @return An array containing the indices of the medians.
     */
    private static int[] getMedianIndices(int amt, int length) {
        int[] indices = new int[amt];
        for (int i = 0; i < amt; i++) {
            indices[i] = getMedianIndex(amt, length, i);
        }
        return indices;
    }
    
    /**
     * Finds the {@code i}'th median of the given sorted array.
     * 
     * @apiNote Runs in {@code O(1)}.
     *
     * @param amt    The total number of medians.
     * @param length The length of the array.
     * @param i      The median to get.
     *
     * @return The {@code i}'th median when selecting {@code amt} medians.
     */
    private static int getMedianIndex(int amt, int length, int i) {
        int index = (int) ((i + 0.5) * length / amt);
        return index;
    }
    
    /**
     * Sets the left edge between a node and it's parent.
     * 
     * @param parent The parent node of the edge.
     * @param left   The left node of the edge.
     */
    private void setLeft(Node parent, Node left) {
        if (parent != null) parent.left = left;
        if (left != null) left.parent = parent;
    }
    
    /**
     * Sets the right edge between a node and it's parent.
     *
     * @param parent The parent node of the edge.
     * @param right  The right node of the edge.
     */
    private void setRight(Node parent, Node right) {
        if (parent != null) parent.right = right;
        if (right != null) right.parent = parent;
    }
    
    public List<T> getRange(int xMin, int xMax, int yMin, int yMax) { // TODO
        return null;
    }
    
    public T get(Object obj) { // TODO
        return null;
    }
    
    public Iterator<T> iterator() { // TODO
        return null;
    }
    
    
    
    // TMP
    public static void main(String[] args) {
        Logger.setDefaultLogger(new StreamLogger(System.out));
        PriorTree<InputVertex> tree = new PriorTree<>(List.of(
                new InputVertex(0L, 1, 1),
                new InputVertex(0L, 2, 2),
                new InputVertex(0L, 3, 3),
                new InputVertex(0L, 4, 4),
                new InputVertex(0L, 5, 5),
                new InputVertex(0L, 6, 6),
                new InputVertex(0L, 7, 7)
        ));
        Logger.write("ENDED");
        Logger.write(tree.debug());
    }
    
    
}
