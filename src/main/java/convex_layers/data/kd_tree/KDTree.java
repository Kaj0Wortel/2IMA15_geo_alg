package convex_layers.data.kd_tree;

import convex_layers.BaseInputVertex;
import convex_layers.data.Base2DTree;
import convex_layers.data.Node2D;
import convex_layers.data.Range2DSearch;
import convex_layers.data.prior_tree.PriorTree;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tools.Var;
import tools.log.Logger;
import tools.log.StreamLogger;

import java.util.*;

/**
 * KD tree data structure for 2D.
 *
 * @param <T> The type of the elements.
 */
public class KDTree<T extends Node2D<T>>
        implements Range2DSearch<T> {// TODO: Change to Base2DTree if the range search cannot be performed directly.
    
    /* ----------------------------------------------------------------------
     * Variables
     * ----------------------------------------------------------------------
     */
    /** The size of the tree. */
    protected int size = 0;
    /** The root node of the tree. */
    private Node<T> root;

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
    protected static class Node<T extends Node2D<T>>
            implements Node2D<Node<T>> {
        
        /** The data of the node. */
        private T data;
        /** The parent of this node. */
        private Node<T> parent;
        /** The left child of the node. */
        private Node<T> left;
        /** The right child of the node. */
        private Node<T> right;
        /** The dimension this node splits on. */
        private boolean xSplit;
        /** The x-value to search for. */
        private final double x;
        /** The y-value to search for. */
        private final double y;


        /**
         * Creates a new node.
         * 
         * @param data   The initial data of the node.
         * @param parent The initial parent of the node.
         * @param left   The initial left child of the node.
         * @param right  The initial right child of the node.
         * @param xSplit The split value of the x of this node.
         */
        public Node(T data, Node<T> parent, Node<T> left, Node<T> right, boolean xSplit) {
            this.data = data;
            this.parent = parent;
            this.left = left;
            this.right = right;
            this.xSplit = xSplit;
            this.x = data.getX();
            this.y = data.getY();
        }
        

        @Override
        public int hashCode() {
            return (data == null ? 0 : data.hashCode());
        }

        @Override
        @SuppressWarnings("rawtypes")
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) return false;
            return ((Node) obj).data.equals(data); 
        }

        @Override
        public String toString() {
            return getClass().getCanonicalName() + "[" + Var.LS +
                    "    data  : " + (data == null ? "null" : data) + Var.LS +
                    "    xSplit : " + xSplit + Var.LS +
                    "    parent: " + (parent == null ? "null" : parent.data) + Var.LS +
                    "    left  : " + (left == null ? "null" : left.data) + Var.LS +
                    "    right : " + (right == null ? "null" : right.data) + Var.LS +
                    "]";
        }
        
        
    }
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates an empty KD tree.
     */
    public KDTree() {
        size = 0;
    }

    /**
     * Creates a new KD tree containing the given elements.
     *
     * @param col The initial elements of the quad tree.
     */
    public KDTree(Collection<T> col) {
        //clear();
        init(col);
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    public void init(Collection<T> col) {
        clear();
        size = col.size();

        List<T> colX = new ArrayList<>(col);
        Comparator<T> xCompare = Comparator.comparingDouble(Node2D::getX);
        colX.sort(xCompare);

        List<T> colY = new ArrayList<>(col);
        Comparator<T> yCompare = Comparator.comparingDouble(Node2D::getY);
        colY.sort(yCompare);


        root = buildKDTree(colX, colY ,0);

    }

    private Node<T> buildKDTree(List<T> colX, List<T> colY, int depth) {
        if (colX.size() == 0) {
            return null;
        }
        if (colX.size() == 1) {
            // return tree of only one leaf
            T leaf = colX.get(0);
            return new Node<T>(leaf, null, null , null, depth % 2 == 0);
        }

        List<T> P1x = new ArrayList<>();
        List<T> P1y = new ArrayList<>();
        List<T> P2x = new ArrayList<>();
        List<T> P2y = new ArrayList<>();
        int medianLoc = colX.size() / 2;
        Node<T> medianNode;
        T median;

        if (depth % 2 == 0) {
            median = colX.get(medianLoc);
            medianNode = new Node<>(median, null, null, null, true);

            colX.forEach(p -> { if (p.getX() <= median.getX()) P1x.add(p); else P2x.add(p);});
            colY.forEach(p -> { if (p.getX() <= median.getX()) P1y.add(p); else P2y.add(p);});

        } else {
            median = colY.get(medianLoc);
            medianNode = new Node<>(median, null, null, null, false);

            colY.forEach(p -> { if (p.getY() <= median.getY()) P1y.add(p); else P2y.add(p);});
            colX.forEach(p -> { if (p.getY() <= median.getY()) P1x.add(p); else P2x.add(p);});
        }
        P1x.remove(median);
        P1y.remove(median);
        Node<T> left = buildKDTree(P1x, P1y, depth + 1);
        Node<T> right = buildKDTree(P2x, P2y, depth + 1);
        medianNode.setLeft(left);
        medianNode.setRight(right);
        if (left != null) {
            left.setParent(medianNode);
        }
        if (right != null) {
            right.setParent(medianNode);
        }
        return medianNode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(Object obj) {
        if (!(obj instanceof Node2D)) return null;
        Node2D<T> node = (Node2D<T>) obj;

        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public boolean contains(Object obj) {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object obj) {
        if (!(obj instanceof Node2D)) return false;
        T t = (T) obj;
        boolean remove1 = remove(root, t);
        int size1 = size;
        boolean remove2 = remove(root, t);
        if (!remove1 || remove2) {
            System.out.println(t.toString() + " " + remove1 + " " + remove2 + " " + size1 + " " + size);
        }
        return remove1;
    }
    
    private boolean remove(Node<T> current, T t) {
        while (current != null) {
            if (current.getData().getX() == t.getX() && current.getData().getY() == t.getY()) {
                // last node
                if (size == 1) {
                    root = null;
                    size--;
                    return true;
                }
                // remove leaf
                if (current.getLeft() == null && current.getRight() == null) {
                    if (current.getParent().getLeft() != null && current.getParent().getLeft().getData().equals(current.getData())) {
                        current.getParent().setLeft(null);
                    } else {
                        current.getParent().setRight(null);
                    }
                    size--;
                    return true;
                // recursively remove from right subtree
                } else if (current.getLeft() == null) {
                    T toRemove = findLeaf(current.getRight());
                    current.setData(toRemove);
                    return remove(current.getRight(), toRemove);
                // recursively remove from left subtree
                } else {
                    T toRemove = findLeaf(current.getLeft());
                    current.setData(toRemove);
                    return remove(current.getLeft(), toRemove);
                }
            } else {
                if (current.isXSplit()) {
                    if (t.getX() <= current.getX()) {
                        current = current.getLeft();
                    } else {
                        current = current.getRight();
                    }
                } else {
                    if (t.getY() <= current.getY()) {
                        current = current.getLeft();
                    } else {
                        current = current.getRight();
                    }
                }
            }
        }
        return false;
    }

    private T findLeaf(Node<T> current) {
        while (current != null) {
            if (current.getLeft() == null && current.getRight() == null) {
                return current.getData();
            } else if (current.getLeft() == null) {
                current = current.getRight();
            } else {
                current = current.getLeft();
            }
        }
        return null;
    }
    
    
    @Override
    public void clear() {
        size = 0;
        root = null;
    }
    
    @Override
    public int size() {
        return size;
    }

    private Collection<T> range(double xMin, double xMax, double yMin, double yMax) {
        return traverseTree(root, xMin, xMax, yMin, yMax, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private Collection<T> traverseTree(Node<T> node, double xMin, double xMax, double yMin, double yMax, double curMinX, double curMaxX, double curMinY, double curMaxY) {
        List<T> found = new ArrayList<>();

        if (node == null) {
            return found;
        }
        // we know that all nodes in the subtree are in the range
        if (xMin <= curMinX && curMaxX <= xMax && yMin <= curMinY && curMaxY <= yMax) {
            Stack<Node<T>> stack = new Stack<>();
            stack.add(node);
            found.add(node.getData());
            while (!stack.empty()) {
                Node<T> n = stack.pop();
                found.add(n.getData());

                if (n.getLeft() != null) {
                    stack.push(n.getLeft());
                }
                if (n.getRight() != null) {
                    stack.push(n.getRight());
                }
            }
            return found;

        }
        // check if current node is in the range
        if (xMin <= node.getData().getX() && node.getData().getX() <= xMax && yMin <= node.getData().getY() && node.getData().getY() <= yMax) {
            found.add(node.getData());
        }

        if (node.isXSplit()) {
            if (xMin <= node.getX() && xMax <= node.getX()) {
                found.addAll(traverseTree(node.getLeft(), xMin, xMax, yMin, yMax, curMinX, node.getX(), curMinY, curMaxY));
            } else if (xMin > node.getX() && xMax > node.getX()) {
                found.addAll(traverseTree(node.getRight(), xMin, xMax, yMin, yMax, node.getX(), curMaxX, curMinY, curMaxY));
            } else if (xMin <= node.getX() && xMax > node.getX()) {
                found.addAll(traverseTree(node.getLeft(), xMin, xMax, yMin, yMax, curMinX, node.getX(), curMinY, curMaxY));
                found.addAll(traverseTree(node.getRight(), xMin, xMax, yMin, yMax, node.getX(), curMaxX, curMinY, curMaxY));
            }
        } else {
            if (yMin <= node.getY() && yMax <= node.getY()) {
                found.addAll(traverseTree(node.getLeft(), xMin, xMax, yMin, yMax, curMinX, curMaxX, curMinY, node.getY()));
            } else if (yMin > node.getY() && yMax > node.getY()) {
                found.addAll(traverseTree(node.getRight(), xMin, xMax, yMin, yMax, curMinX, curMaxX, node.getY(), curMaxY));
            } else if (yMin <= node.getY() && yMax > node.getY()) {
                found.addAll(traverseTree(node.getLeft(), xMin, xMax, yMin, yMax, curMinX, curMaxX, curMinY, node.getY()));
                found.addAll(traverseTree(node.getRight(), xMin, xMax, yMin, yMax, curMinX, curMaxX, node.getY(), curMaxY));
            }
        }
        return found;
    }

    @Override
    public Collection<T> getRangeUpRight(double xMin, double xMax, double yMin, double yMax) {
        return range(xMin, xMax, yMin, yMax);
    }
    
    @Override
    public Collection<T> getRangeDownRight(double xMin, double xMax, double yMin, double yMax) {
        return range(xMin, xMax, yMin, yMax);
    }
    
    @Override
    public Collection<T> getRangeUpLeft(double xMin, double xMax, double yMin, double yMax) {
        return range(xMin, xMax, yMin, yMax);
    }
    
    @Override
    public Collection<T> getRangeDownLeft(double xMin, double xMax, double yMin, double yMax) {
        return range(xMin, xMax, yMin, yMax);
    }

    public static void main(String[] args) {
        Logger.setDefaultLogger(new StreamLogger(System.out));
        //KDTree<BaseInputVertex> t = new KDTree<>();
        //Logger.write(t.iterator().hasNext());
        KDTree<BaseInputVertex> tree = new KDTree<>(List.of(
                new BaseInputVertex(0L, 1, 1),
                new BaseInputVertex(0L, 2, 1),
                new BaseInputVertex(0L, 3, 1),
                new BaseInputVertex(0L, 1, 2),
                new BaseInputVertex(0L, 2, 2),
                new BaseInputVertex(0L, 3, 2),
                new BaseInputVertex(0L, 1, 3),
                new BaseInputVertex(0L, 2, 3),
                new BaseInputVertex(0L, 3, 3)
        ));
        Logger.write("ENDED");
        tree.print(tree.root);
        Collection test = tree.getRange(1, 9, 2, 2, true, true);
        Logger.write(test.toString());
        tree.remove(new BaseInputVertex(0L, 2, 2));
        tree.print(tree.root);
        Collection test2 = tree.getRange(1, 9, 2, 2, true, true);
        Logger.write(test2.toString());
        Logger.write("-------------------------");
        //Logger.write(tree.getUnboundedRange(3, 2, 3).toString().replaceAll("], ", "]," + Var.LS));
    }

    private void print(Node t) {
        System.out.println(t.toString());
        if (t.getLeft() != null) {
            print(t.getLeft());
        }
        if (t.getRight() != null) {
            print(t.getRight());
        }

    }
    
}
