package convex_layers.hull;

import convex_layers.InputVertex;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import convex_layers.visual.Visualizer;
import tools.Pair;
import tools.data.collection.rb_tree.LinkedRBTree;
import tools.iterators.InverseListIterator;
import tools.iterators.MultiIterator;
import tools.log.Logger;
import tools.log.StreamLogger;

import java.util.*;


/**
 * This class represents the convex hull data structure.
 * This data structure should be used for search queries on the hull.
 */
public class ConvexHull
            implements Collection<InputVertex> {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The search tree for the left part of the hull. */
    private final LinkedRBTree<VectorYNode> left;
    /** The search tree for the right part of the hull. */
    private final LinkedRBTree<VectorYNode> right;
    /** The element with the maximum y-coordinate. */
    private VectorYNode top;
    /** The element with the lowest y-coordinate. */
    private VectorYNode bottom;
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates a new convex hull data structure from the given left and right hald-hulls. <br>
     * The initialisation takes {@code O(n log n)} time if the collections are unsorted, or
     * {@code O(n)} time if the collections are (nearly) sorted from low y-index to high y-index.
     * 
     * @param lCol The left side of the convex hull.
     * @param rCol The right side of the convex hull.
     */
    public ConvexHull(Collection<InputVertex> lCol, Collection<InputVertex> rCol) {
        List<VectorYNode> lList = new ArrayList<>(lCol.size());
        List<VectorYNode> rList = new ArrayList<>(rCol.size());
        for (InputVertex iv : lCol) {
            lList.add(new VectorYNode(iv));
        }
        for (InputVertex iv : rCol) {
            rList.add(new VectorYNode(iv));
        }
        left = new LinkedRBTree<>(lList);
        right = new LinkedRBTree<>(rList);
        top = (left.getMax().compareTo(right.getMax()) <= 0
                ? right.getMax()
                : left.getMax());
        bottom = (left.getMin().compareTo(right.getMin()) > 0
                ? right.getMin()
                : left.getMin());
    }
    
    
    /* ----------------------------------------------------------------------
     * Static initializers.
     * ----------------------------------------------------------------------
     */
    public static ConvexHull createConvexHull(Collection<InputVertex> col) {
        if (col.isEmpty()) return new ConvexHull(List.of(), List.of());
        if (col.size() == 1) return new ConvexHull(col, List.of());
        if (col.size() == 2) {
            Iterator<InputVertex> it = col.iterator();
            return new ConvexHull(List.of(it.next()), List.of(it.next()));
        }
        InputVertex[] data = col.toArray(new InputVertex[col.size()]);
        Arrays.sort(data, (iv1, iv2) -> {
            double diff = iv1.getV().y() - iv2.getV().y();
            if (diff == 0) return 0;
            else if (diff < 0) return Math.min(-1, (int) diff);
            else if (diff > 0) return Math.max(1, (int) diff);
            else throw new IllegalArgumentException(
                    "Expected non-special type double, but found: " + diff);
        });
        InputVertex min = data[0];
        InputVertex max = data[data.length - 1];
        List<InputVertex> left = new LinkedList<>();
        List<InputVertex> right = new LinkedList<>();
        Edge e = new Edge(min.getV(), max.getV());
        left.add(min);
        right.add(min);
        for (int i = 1; i < data.length - 1; i++) {
            if (e.relOri(data[i].getV()) < 0) left.add(data[i]);
            else right.add(data[i]);
        }
        left.add(max);
        right.add(max);
        
        left = computeHalfRightHull(left, true);
        right = computeHalfRightHull(right, false);
        
        {
            List<InputVertex> big = (left.size() > right.size() ? left : right);
            big.remove(0);
            big.remove(big.size() - 1);
        }
        
        return new ConvexHull(left, right);
    }

    /**
     * Computes the right half convex hull of the given points. Assume that the points are sorted
     * on y-coordinate, or computes the lower half convex hull if sorted on x-coordinate. <br>
     * To obtain the other half call with {@code inverse == true.}
     * 
     * @param in      The input vertices.
     * @param inverse Whether the other side of the hull should be computed.
     * 
     * @return The vertices on the convex hull.
     */
    private static List<InputVertex> computeHalfRightHull(List<InputVertex> in, boolean inverse) {
        Stack<InputVertex> out = new Stack<>();
        for (InputVertex iv : in) {
            if (out.size() < 2) {
                out.add(iv);
                continue;
            }

            InputVertex last;
            double ori;
            do {
                last = out.pop();
                InputVertex secLast = out.peek();
                Edge e = (inverse
                        ? new Edge(last.v(), secLast.v())
                        : new Edge(secLast.v(), last.v()));
                ori = e.relOri(iv.v());

            } while (ori > 0 && out.size() >= 2);
            if (ori <= 0) out.add(last);
            out.add(iv);
        }
        return out;
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * Determines the four points near the intersection of the extended edge with the hull.
     * 
     * @param e The edge to get the intersection of.
     * 
     * @return A {@link NearIntersection} with the points.
     */
    public NearIntersection getPointsNearLine(Edge e) {
        VectorYNode vyn1 = null, vyn2 = null, vyn3 = null, vyn4 = null;
        NearIntersection.Tree t1 = null, t2 = null, t3 = null, t4 = null;
        
        // TODO: edge cases:
        //  - edge goes through vertex (done?).
        double relOriTop = e.relOri(top.getVec());
        double relOriBot = e.relOri(bottom.getVec());
        if (relOriTop == 0) {
            Logger.write("through top", Logger.Type.ERROR);
            // TODO: edge case: edge goes through top.
            // Note: should not occur since no 3 points on one line.
            throw new RuntimeException();
            
        } else if (relOriBot == 0) {
            Logger.write("through bottom", Logger.Type.ERROR);
            // TODO: edge case: edge goes through bottom.
            // Note: should not occur since no 3 points on one line.
            throw new RuntimeException();

        } else if (relOriTop * relOriBot < 0) {
            // Line goes through both the left and right side.
            // Set direction of e to the right, if needed.
            if (e.x1() > e.x2()) {
                e = new Edge(e.v2(), e.v1());
            }
            Pair<VectorYNode, VectorYNode> pair = getNodeAboveBothSides(left, e);
            vyn1 = pair.getFirst();
            vyn2 = pair.getSecond();
            pair = getNodeAboveBothSides(right, e);
            vyn3 = pair.getFirst();
            vyn4 = pair.getSecond();
            t1 = NearIntersection.Tree.LEFT;
            t2 = NearIntersection.Tree.LEFT;
            t3 = NearIntersection.Tree.RIGHT;
            t4 = NearIntersection.Tree.RIGHT;
            
        } else if (relOriTop > 0) {
            // Line lies on the left side.
            // Set direction of e to upwards, if needed.
            if (e.y1() > e.y2()) {
                e = new Edge(e.v2(), e.v1());
            }
            Pair<VectorYNode, VectorYNode> pair = getNodeAboveOneSide(left, e, true);
            vyn1 = pair.getFirst();
            vyn2 = pair.getSecond();
            pair = getNodeAboveOneSide(left, e, false);
            vyn4 = pair.getFirst();
            vyn3 = pair.getSecond();
            t1 = NearIntersection.Tree.LEFT;
            t2 = NearIntersection.Tree.LEFT;
            t3 = NearIntersection.Tree.LEFT;
            t4 = NearIntersection.Tree.LEFT;
            
        } else {
            // Line lies on the right side.
            // Set direction of e to downwards, if needed.
            if (e.y1() < e.y2()) {
                e = new Edge(e.v2(), e.v1());
            }
            Pair<VectorYNode, VectorYNode> pair = getNodeAboveOneSide(right, e, true);
            vyn1 = pair.getFirst();
            vyn2 = pair.getSecond();
            pair = getNodeAboveOneSide(right, e, false);
            vyn4 = pair.getFirst();
            vyn3 = pair.getSecond();
            t1 = NearIntersection.Tree.RIGHT;
            t2 = NearIntersection.Tree.RIGHT;
            t3 = NearIntersection.Tree.RIGHT;
            t4 = NearIntersection.Tree.RIGHT;
        }
        
        return new NearIntersection(vyn1, vyn2, vyn3, vyn4, t1, t2, t3, t4, top, bottom);
    }

    /**
     * Determines the two points on the hull which are the closest to the given line. <br>
     * The following is here assumed:
     * <ul>
     *     <li>The line goes through both the left and the right side.</li>
     *     <li>The line is directed towards the right.</li>
     * </ul>
     * 
     * @param tree The tree to determine the intersections with.
     * @param e    The line to calculate the intersections for.
     * 
     * @return The two points near the intersection, where the first point has the largest y-coordinate.
     */
    private Pair<VectorYNode, VectorYNode> getNodeAboveBothSides(LinkedRBTree<VectorYNode> tree, Edge e) {
        VectorYNode node = tree.getRoot();
        while (true) {
            double ori = e.relOri(node.getVec());
            if (ori < 0) {
                VectorYNode prev = prev(node);
                if (!node.hasLeft()) return new Pair<>(node, prev);
                if (e.relOri(prev.getVec()) >= 0) return new Pair<>(node, prev);
                node = node.left();
                
            } else if (ori > 0) {
                VectorYNode next = next(node);
                if (!node.hasRight()) return new Pair<>(next, node);
                if (e.relOri(next.getVec()) <= 0) return new Pair<>(next, node);
                if (node.hasRight()) node = node.right();
                
            } else {
                return new Pair<>(next(node), node);
            }
        }
    }

    /**
     * Determines the two points on the hull which are the closest to the given line. <br>
     * The following is here assumed:
     * <ul>
     *     <li>The line goes through either the left of the right side, but not both.</li>
     *     <li>The line is directed upwards if the left tree is searched, and directed
     *         downwards if the right tree is searched.</li>
     * </ul>
     * If {@code up} is {@code true}, then the points around the upper intersection are returned.
     * Otherwise are the points around the lower intersection returned.
     * 
     * @param tree The tree to determine the intersections with.
     * @param e    The line to calculate the intersections for.
     * @param up   Whether the upper or lower intersection should be calculated.
     *
     * @return The two points near the intersection, where the first point has the largest y-coordinate.
     */
    private Pair<VectorYNode, VectorYNode> getNodeAboveOneSide(LinkedRBTree<VectorYNode> tree, Edge e, boolean up) {
        VectorYNode node = tree.getRoot();
        double target = (up
                ? Math.max(e.v1().y(), e.v2().y())
                : Math.min(e.v1().y(), e.v2().y()));
        while (true) {
            if (up) {
                if (node.getVec().y() < target) {
                    if (node.hasRight()) node = node.right();
                    else return new Pair<>(next(node), node);
                }
            } else {
                if (node.getVec().y() > target) {
                    if (node.hasLeft()) node = node.left();
                    else return new Pair<>(node, prev(node));
                }
            }
            
            double ori = e.relOri(node.getVec());
            if ((up && ori < 0) || (!up && ori > 0)) {
                VectorYNode next = next(node);
                if (!node.hasRight()) return new Pair<>(next, node);
                if (e.relOri(next.getVec()) * ori < 0) return new Pair<>(next, node);
                if (node.hasRight()) node = node.right();

            } else if ((up && ori > 0) || (!up && ori < 0)) {
                VectorYNode prev = prev(node);
                if (!node.hasLeft()) return new Pair<>(node, prev);
                if (e.relOri(prev.getVec()) * ori <= 0) return new Pair<>(node, prev);
                node = node.left();
                
            } else {
                if (up) return new Pair<>(next(node), node);
                else return new Pair<>(node, prev(node));
            }
        }
    }

    /**
     * Gets the next node of the chain. If no next node exists, takes the maximum node of the
     * other chain.
     * 
     * @param node     The node to get the next node of.
     * 
     * @return The next node in the chain.
     */
    public VectorYNode next(VectorYNode node) {
        if (node.next() != null) return node.next();
        if (left.getMax() == node) return right.getMax();
        else return left.getMax();
    }

    /**
     * Gets the prevsiou node of the chain. If no previous node exists,
     * takes the minimum node of the other chain.
     *
     * @param node     The node to get the previous node of.
     *
     * @return The previous node in the chain.
     */
    public VectorYNode prev(VectorYNode node) {
        if (node.prev() != null) return node.prev();
        if (left.getMin() == node) return right.getMin();
        else return left.getMin();
    }

    /**
     * Adds a vertex to the hull.
     * 
     * @param iv The vertex to add.
     * 
     * @return {@code true} if the vertex was added. {@code false} otherwise.
     */
    @Override
    public boolean add(InputVertex iv) {
        VectorYNode vyn = new VectorYNode(iv);
        if (size() == 0) {
            left.add(top = bottom = vyn);
        }
        
        boolean newBound = false;
        if (iv.getY() > top.getVec().y()) {
            newBound = true;
            top = vyn;
        }
        if (iv.getY() < bottom.getVec().y()) {
            newBound = true;
            bottom = vyn;
        }
        
        if (newBound) {
            if (left.size() > right.size()) return right.add(vyn);
            else return left.add(vyn);
            
        } else {
            Edge e = new Edge(bottom.getVec(), top.getVec());
            if (e.relOri(iv.getV()) < 0) return left.add(vyn);
            else return right.add(vyn);
        }
    }

    @Override
    public boolean remove(Object obj) {
        if (obj instanceof InputVertex) return remove((InputVertex) obj);
        if (obj instanceof VectorYNode) return remove((VectorYNode) obj);
        return false;
    }
    
    @Override
    public boolean containsAll(Collection<?> col) {
        for (Object obj : col) {
            if (!contains(obj)) return false;
        }
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends InputVertex> col) {
        return addAll((Iterable<InputVertex>) col);
    }
    
    @Override
    public boolean removeAll(Collection<?> col) {
        boolean mod = false;
        for (Object obj : col) {
            if (remove(obj)) mod = true;
        }
        return mod;
    }
    
    @Override
    public boolean retainAll(Collection<?> col) {
        boolean mod = left.retainAll(col);
        if (right.retainAll(col)) mod = true;
        if (!mod) return false;
        
        if (left.isEmpty()) {
            top = right.getMax();
            bottom = right.getMin();
            
        } else if (right.isEmpty()) {
            top = right.getMax();
            bottom = right.getMin();
            
        } else {
            top = (left.getMax().getVec().y() < right.getMax().getVec().y()
                    ? right.getMax()
                    : left.getMax()
            );
            bottom = (left.getMin().getVec().y() > right.getMin().getVec().y()
                    ? right.getMin()
                    : left.getMin()
            );
        }
        return true;
    }
    
    @Override
    public void clear() {
        left.clear();
        right.clear();
        top = null;
        bottom = null;
    }
    
    /**
     * Adds all vertices from the list.
     * 
     * @param ivs The vertices to add.
     * 
     * @return {@code true} if the data structure was modified. {@code false} otherwise.
     */
    public boolean addAll(Iterable<InputVertex> ivs) {
        boolean mod = false;
        for (InputVertex iv : ivs) {
            if (add(iv)) mod = true;
        }
        return mod;
    }
    
    /**
     * Removes the given vertex.
     * 
     * @param iv The vertex to be removed.
     * @return {@code true} if the vertex was removed. {@code false} otherwise.
     */
    public boolean remove(InputVertex iv) {
        return remove(new VectorYNode(iv));
    }

    /**
     * Removes the given node.
     *
     * @param vyn The node to be removed.
     * @return {@code true} if the vertex was removed. {@code false} otherwise.
     */
    public boolean remove(VectorYNode vyn) {
        Edge e = new Edge(bottom.getVec(), top.getVec());
        double ori = e.relOri(vyn.getVec());
        if (ori < 0) return left.remove(vyn);
        else if (ori > 0) return right.remove(vyn);
        else {
            if (vyn.equals(top)) top = prev(top);
            if (vyn.equals(bottom)) bottom = next(bottom);
            if (left.remove(vyn)) return true;
            return right.remove(vyn);
        }
    }
    
    /**
     * Removes all vertices from the list.
     *
     * @param ivs The vertices to remove.
     *
     * @return {@code true} if the data structure was modified. {@code false} otherwise.
     */
    public boolean removeAll(Iterable<InputVertex> ivs) {
        boolean mod = false;
        for (InputVertex iv : ivs) {
            if (remove(iv)) mod = true;
        }
        return mod;
    }
    
    /**
     * Removes all nodes from the list.
     *
     * @param vyns The nodes to remove.
     *
     * @return {@code true} if the data structure was modified. {@code false} otherwise.
     */
    public boolean removeAllNodes(Iterable<VectorYNode> vyns) {
        boolean mod = false;
        for (VectorYNode vyn : vyns) {
            if (remove(vyn)) mod = true;
        }
        return mod;
    }
    
    @Override
    public int size() {
        return left.size() + right.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object obj) {
        VectorYNode node;
        if (obj instanceof InputVertex) node = new VectorYNode((InputVertex) obj);
        else if (obj instanceof VectorYNode) node = (VectorYNode) obj;
        else return false;
        if (isEmpty()) return false;
        Edge e = new Edge(bottom.getVec(), top.getVec());
        if (e.relOri(node.getVec()) < 0) return left.contains(node);
        else return right.contains(node);
    }
    
    /**
     * Converts the given {@link VectorYNode} Iterator to an {@link InputVertex} iterator.
     * 
     * @param it The iterator to convert.
     * 
     * @return A converted iterator using the given iterator as underlying stream.
     */
    public static Iterator<InputVertex> convert(Iterator<VectorYNode> it) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public InputVertex next() {
                return it.next().getIV();
            }
        };
    }

    /**
     * @return An iterator over all nodes in the hull. The nodes are returned in order. 
     */
    @SuppressWarnings("unchecked")
    public Iterator<VectorYNode> nodeIterator() {
        return new MultiIterator<>(
                left.iterator(),
                new InverseListIterator<>(right.listIterator(false))
        );
    }

    @Override
    public Iterator<InputVertex> iterator() {
        return convert(nodeIterator());
    }
    
    public Iterator<InputVertex> leftIterator() {
        return convert(left.iterator());
    }
    
    public Iterator<InputVertex> rightIterator() {
        return convert(right.iterator());
    }
    
    public Iterable<VectorYNode> getLeft() {
        return left;
    }
    
    public Iterable<VectorYNode> getRight() {
        return right;
    }
    
    public Iterable<InputVertex> getLeftInput() {
        return () -> convert(left.iterator());
    }
    
    public Iterable<InputVertex> getRightInput() {
        return () -> convert(right.iterator());
    }

    @Override
    public Object[] toArray() {
        return toArray(new InputVertex[size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] arr) {
        int i = 0;
        for (InputVertex iv : this) {
            //if (i >= arr.length) break;
            arr[i++] = (T) iv;
        }
        return arr;
    }
    
    /**
     * Testing purposes
     * 
     * @param args
     */
    public static void main(String[] args) {
        Logger.setDefaultLogger(new StreamLogger(System.out));
        List<InputVertex> left = new ArrayList<>();
        List<InputVertex> right = new ArrayList<>();
        int amt = 20;
        left.add(new InputVertex(0, 0, 0));
        for (int i = 1; i < amt; i++) {
            double a = 0.5*amt;
            double x = 4.0/amt*(-Math.pow(i - a, 2) + a*a);
            left.add(new InputVertex(i, -x, 2*i));
            right.add(new InputVertex(i + amt - 1, x, 2*i));
        }
        right.add(new InputVertex(2*amt - 1, 0, 2*amt));
        List<Edge> lEdges = new ArrayList<>();
        for (int i = 0; i < left.size() - 1; i++) {
            lEdges.add(new Edge(left.get(i).getV(), left.get(i+1).getV()));
        }
        List<Edge> rEdges = new ArrayList<>();
        for (int i = 0; i < right.size() - 1; i++) {
            rEdges.add(new Edge(right.get(i).getV(), right.get(i+1).getV()));
        }
        left.add(new InputVertex(98, -5, 45));
        right.add(new InputVertex(99, 20, 10));
        
        //ConvexHull ch = new ConvexHull(left, right);
        List<InputVertex> combi = new ArrayList<>(left);
        combi.addAll(right);
        ConvexHull ch = ConvexHull.createConvexHull(combi);
        Visualizer viz = new Visualizer();
        
        viz.setData(List.of(left, right));
        viz.redraw();
        viz.addData(List.of(ch));
        viz.redraw();
        viz.addData(List.of(ch.getLeftInput(), ch.getRightInput()));
        viz.redraw();
        Edge e = new Edge(new Vector(1, 5), new Vector(-1, 30));
        viz.addPoint(List.of(e.v1(), e.v2()));
        viz.addEdge(List.of(e));
        
        NearIntersection ni = ch.getPointsNearLine(e);
        Logger.write(ni);
        viz.addEdge(List.of(
                new Edge(ni.v1.getVec(), ni.v2.getVec()),
                new Edge(ni.v2.getVec(), ni.v4.getVec()),
                new Edge(ni.v4.getVec(), ni.v3.getVec()),
                new Edge(ni.v3.getVec(), ni.v1.getVec())
        ));
        viz.redraw();
    }
    
    
}
