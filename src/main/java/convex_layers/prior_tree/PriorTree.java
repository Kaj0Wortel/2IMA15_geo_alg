package convex_layers.prior_tree;

import convex_layers.InputVertex;
import lombok.*;
import tools.Var;
import tools.iterators.FunctionIterator;
import tools.iterators.GeneratorIterator;
import tools.log.Logger;
import tools.log.StreamLogger;

import java.util.*;
import java.util.List;

public class PriorTree<T extends PriorTreeNode<T>>
            implements Collection<T> {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The root node of the tree. */
    private Node<T> root;
    /** The size of the tree. */
    protected int size = 0;
    
    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    /**
     * Data class for the nodes used in the priority tree.
     * 
     * @param <T> The data type of the data.
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
        private Node<T> parent;
        /** The left child of the node. */
        private Node<T> left;
        /** The right child of the node. */
        private Node<T> right;
        /** The y-coordinate this node splits on. */
        private double ySplit;
        /** The x-coordinate this node splits on if the y-split is equal. */
        private double xSplit;
        
        
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
        @SuppressWarnings("rawtypes")
        public boolean equals(Object obj) { // TODO
            if (!(obj instanceof Node)) return false;
            else return Objects.equals(data, ((Node) obj).data);
        }
        
        @Override
        public String toString() {
            return getClass().getCanonicalName() + "[" + Var.LS +
                    "    data  : " + (data == null ? "null" : data) + Var.LS +
                    "    split : (" + xSplit + "," + ySplit + ")" + Var.LS +
                    "    parent: " + (parent == null ? "null" : parent.data) + Var.LS +
                    "    left  : " + (left == null ? "null" : left.data) + Var.LS +
                    "    right : " + (right == null ? "null" : right.data) + Var.LS +
                    "]";
        }
        
        
    }
    
    
    /**
     * Data structure element for initializing a priority tree.
     * 
     * @param <T> The data type of the data of the nodes.
     */
    @AllArgsConstructor
    private static class Elem<T extends PriorTreeNode<T>> {
        /** The current node. */
        private Node<T> node;
        /** All left children of the current node, sorted on x. */
        private Node<T>[] leftX;
        /** All left children of the current node, sorted on y. */
        private Node<T>[] leftY;
        /** All right children of the current node, sorted on x. */
        private Node<T>[] rightX;
        /** All right children of the current node, sorted on y. */
        private Node<T>[] rightY;
        
        
    }
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates and initializes a new priority tree.
     * 
     * @param nodes The nodes to be added.
     */
    public PriorTree(Collection<T> nodes) {
        initTree(nodes);
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * Initializes the tree with the given elements.
     *
     * @apiNote Runs in {@code n*log(n)}.
     * 
     * @param nodes The nodes of the tree.
     */
    @SuppressWarnings("unchecked")
    private void initTree(Collection<T> nodes) {
        size = nodes.size();
        root = null;
        
        // Convert data to nodes.
        if (nodes.isEmpty()) return;
        if (nodes.size() == 1) {
            root = new Node<T>(nodes.iterator().next());
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
        // First sort on x, then copy, then sort on y.
        Arrays.sort(xData, Node::compareToX);
        Node<T>[] yData = Arrays.copyOf(xData, xData.length);
        Arrays.sort(yData, Node::compareToY);
        
        // Build the tree.
        List<Elem<T>> prevState = new ArrayList<>();
        List<Elem<T>> nextState = null;
        prevState.add(new Elem<T>(null, xData, yData, null, null));
        
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
                    first.ySplit = median.getY();
                    first.xSplit = median.getX();
//                    Logger.write("");
//                    Logger.write("first: " + first);
//                    Logger.write("median: " + median);
//                    Logger.write("");
                    
                    // Split the y-array into the left and right arrays.
                    // The first value of the x-array is removed here.
                    // The points on the median are considered to be in the left tree
                    // if, and only if the x-coordinate of the element is smaller or
                    // equal to the x-coordinate of the median.
                    List<Node<T>> newLeftYList = new ArrayList<>();
                    List<Node<T>> newRightYList = new ArrayList<>();
                    for (int i = 0; i < yArr.length; i++) {
                        if (yArr[i] == first) continue;
                        if (yArr[i].getY() < median.getY()) {
                            newLeftYList.add(yArr[i]);
                            
                        } else if (yArr[i].getY() > median.getY()) {
                            newRightYList.add(yArr[i]);
                            
                        } else {
                            if (yArr[i].getX() <= median.getX()) {
                                newLeftYList.add(yArr[i]);
                            } else {
                                newRightYList.add(yArr[i]);
                            }
                        }
                    }
                    Node<T>[] newLeftY = newLeftYList.toArray(new Node[newLeftYList.size()]);
                    Node<T>[] newRightY = newRightYList.toArray(new Node[newRightYList.size()]);
                    
//                    Logger.write(Var.LS + "left Y : " + Var.LS + Arrays.toString(newLeftY));
//                    Logger.write(Var.LS + "right Y: " + Var.LS + Arrays.toString(newRightY));
                    
                    Node<T>[] newLeftX = new Node[newLeftY.length];
                    Node<T>[] newRightX = new Node[newRightY.length];
                    for (int i = 1, leftI = 0, rightI = 0; i < xArr.length; i++) {
                        if (xArr[i].getY() < median.getY()) {
                            newLeftX[leftI++] = xArr[i];
                            
                        } else if (xArr[i].getY() > median.getY()) {
                            newRightX[rightI++] = xArr[i];
                            
                        } else {
                            if (xArr[i].getX() <= median.getX()) {
                                newLeftX[leftI++] = xArr[i];
                            } else {
                                newRightX[rightI++] = xArr[i];
                            }
                        }
                    }
                    
                    if (root == null) root = first;
                    nextState.add(new Elem<>(
                            first,
                            newLeftX,
                            newLeftY,
                            newRightX,
                            newRightY
                    ));
                }
            }
            
            prevState = nextState;
            nextState = null;
        }
    }
    
    protected String debug() {
        StringBuilder sb = new StringBuilder(getClass().getCanonicalName());
        sb.append("[");
        sb.append(Var.LS);
        sb.append("  ");
        boolean first = true;
        for (Iterator<Node<T>> it = nodeIterator(); it.hasNext(); ) {
            Node<T> node = it.next();
            if (first) first = false;
            else {
                sb.append(",");
                sb.append(Var.LS);
                sb.append("  ");
            }
            sb.append(node.toString().replaceAll(Var.LS + "]", Var.LS + "  ]"));
        }
        sb.append(Var.LS);
        sb.append("]");
        return sb.toString();
    }

    /**
     * @return The root of the tree.
     */
    public T getRoot() {
        return (root == null ? null : root.data);
    }
    
    /**
     * Sets the left edge between a node and it's parent.
     * 
     * @param parent The parent node of the edge.
     * @param left   The left node of the edge.
     */
    private void setLeft(Node<T> parent, Node<T> left) {
        if (parent != null) parent.left = left;
        if (left != null) left.parent = parent;
    }
    
    /**
     * Sets the right edge between a node and it's parent.
     *
     * @param parent The parent node of the edge.
     * @param right  The right node of the edge.
     */
    private void setRight(Node<T> parent, Node<T> right) {
        if (parent != null) parent.right = right;
        if (right != null) right.parent = parent;
    }
    
    /**
     * Reports all elements in the given range. <br>
     * All boundaries are inclusive.
     * 
     * @param xMax The maximum x-coordinate (incl.).
     * @param yMin The minimum y-coordinate (incl.).
     * @param yMax The maximum y-coordinate (incl.).
     * 
     * @return All elements within the given range.
     * 
     * @throws IllegalArgumentException If the described rectangle is empty.
     */
    public Collection<T> getUnboundedRange(double xMax, double yMin, double yMax) {
        if (yMin > yMax) {
            throw new IllegalArgumentException("Invalid range: (-inf, " + yMin + ") x (" +
                    xMax + ", " + yMax + ")");
        }
        Collection<T> toReport = new ArrayList<>();
        Node<T> node = root;
        if (node == null) return toReport;
        
        while (node != null) {
            if (node.getX() > xMax) return toReport;
            if (yMin <= node.getY() && node.getY() <= yMax) toReport.add(node.data);
            
            if (node.ySplit < yMin) node = node.right;
            else if (yMax < node.ySplit || (node.ySplit == yMax && node.xSplit >= xMax)) node = node.left;
            else break;
        }
        if (node == null) return toReport;
        
        reportHalf(toReport, node.left, xMax, yMin, yMax, true);
        reportHalf(toReport, node.right, xMax, yMin, yMax, false);
        
        return toReport;
    }

    /**
     * Reports the nodes in the tree branched to the left or right,
     * depending on {@code left}. Starts from {@code node} (excl).
     * 
     * @param toReport The set used to report the answers for.
     * @param node     The node to start reporting from.
     * @param xMax     The maximum x-coordinate.
     * @param yMin     The minimum y-coordinate.
     * @param yMax     The maximum y-coordinate.
     * @param left     Whether the tree was branched to the left or the right.
     */
    private void reportHalf(Collection<T> toReport, Node<T> node,
                            double xMax, double yMin, double yMax, boolean left) {
        Queue<Node<T>> toProcess = new LinkedList<>(List.of(node));
        while (!toProcess.isEmpty()) {
            Node<T> n = toProcess.remove();
            if (n.getX() > xMax) continue;
            
            if (yMin <= n.getY() && n.getY() <= yMax) {
                toReport.add(n.data);
            }
            
            if (n.ySplit < yMin) {
                assert(left);
                if (n.right != null) toProcess.add(n.right);
                
            } else if (n.ySplit > yMax || (node.ySplit == yMax && node.xSplit >= xMax)) {
                assert(!left);
                if (n.left != null) toProcess.add(n.left);
                
            } else {
                if (left) {
                    reportAll(toReport, n.right, xMax);
                    if (n.left != null) toProcess.add(n.left);
                } else {
                    if (n.right != null) toProcess.add(n.right);
                    reportAll(toReport, n.left, xMax);
                }
            }
        }
    }
    
    
    /**
     * Reports all elements which have the given root node in their path
     * from the root of the tree. This includes the given root node.
     * 
     * @param toReport The collection used to report the items.
     * @param rootNode The node to start reporting items from.
     * @param xMax     The maximum x-coordinate.
     */
    private void reportAll(Collection<T> toReport, Node<T> rootNode, double xMax) {
        if (rootNode == null) return;
        Deque<Node<T>> stack = new LinkedList<>(List.of(rootNode));
        while (!stack.isEmpty()) {
            Node<T> node = stack.removeLast();
            if (node.getX() > xMax) continue;
            toReport.add(node.data);
            if (node.right != null) stack.addLast(node.right);
            if (node.left != null) stack.addLast(node.left);
        }
    }

    /**
     * @param obj The key of the node to search for.
     * 
     * @return The node denoted by the given key, or {@code null} if no such node exists.
     */
    @SuppressWarnings("unchecked")
    protected Node<T> getNode(Object obj) {
        T elem;
        if (obj instanceof Node) {
            elem = ((Node<T>) obj).data;
            
        } else if (obj instanceof PriorTreeNode) {
            elem = (T) obj;
            
        } else return null;
        Node<T> node = root;
        while (node != null) {
            if (node.getX() > elem.getX()) return null;
            if (node.getX() == elem.getX() && node.getY() == elem.getY()) {
                if (Objects.equals(node.data, node)) return node;
            }
            if (node.getY() <= node.ySplit) {
                node = node.right;
            } else {
                node = node.left;
            }
        }
        return null;
    }
    
    public T get(Object obj) {
        Node<T> node = getNode(obj);
        return (node == null ? null : node.data);
    }
    
    @Override
    public boolean contains(Object obj) {
        return getNode(obj) != null;
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new FunctionIterator<>(nodeIterator(),
                (node) -> (node == null ? null : node.data));
    }
    
    @Override
    public Object[] toArray() {
        return toArray(new PriorTreeNode[size()]);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(T1[] arr) {
        int i = 0;
        for (T data : this) {
            arr[i++] = (T1) data;
            if (i > arr.length) break;
        }
        return arr;
    }
    
    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean containsAll(Collection<?> col) {
        for (Object obj : col) { // TODO: optimize this stuff with a range query or something.
            if (!contains(obj)) return false;
        }
        return true;
    }
    
    @Override
    public boolean addAll(Collection<? extends T> col) {
        boolean mod = false;
        for (T data : col) {
            if (add(data)) mod = true;
        }
        return mod;
    }
    
    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        size = 0;
        root = null;
    }
    
    /**
     * @return An iterator over the nodes in this tree. There is no
     *     guarantee in which order the nodes are returned.
     */
    protected Iterator<Node<T>> nodeIterator() {
        if (root == null) {
            return new GeneratorIterator<>() {
                @Override
                protected Node<T> generateNext() {
                    done();
                    return null;
                }
            };
        }
        return new GeneratorIterator<>() {
            Deque<Node<T>> stack = new LinkedList<>(List.of(root));
            
            @Override
            protected Node<T> generateNext() {
                if (stack.isEmpty()) {
                    done();
                    return null;
                }
                Node<T> node = stack.removeFirst();
                if (node.right != null) {
                    stack.addFirst(node.right);
                }
                if (node.left != null) {
                    stack.addFirst(node.left);
                }
                return node;
            }
        };
    }
    
    
    
    // TMP
    public static void main(String[] args) {
        Logger.setDefaultLogger(new StreamLogger(System.out));
        PriorTree<InputVertex> tree = new PriorTree<>(List.of(
                new InputVertex(0L, 1, 1),
                new InputVertex(0L, 2, 1),
                new InputVertex(0L, 3, 1),
                new InputVertex(0L, 1, 2),
                new InputVertex(0L, 2, 2),
                new InputVertex(0L, 3, 2),
                new InputVertex(0L, 1, 3),
                new InputVertex(0L, 2, 3),
                new InputVertex(0L, 3, 3)
        ));
        Logger.write("ENDED");
        //Logger.write(tree.debug());
        Logger.write("-------------------------");
        Logger.write(tree.getUnboundedRange(3, 2, 3).toString().replaceAll("], ", "]," + Var.LS));
    }
    
    
}
