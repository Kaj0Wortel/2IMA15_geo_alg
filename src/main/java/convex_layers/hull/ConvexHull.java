package convex_layers.hull;

import convex_layers.InputVertex;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import convex_layers.visual.Visualizer;
import tools.MultiTool;
import tools.Pair;
import tools.data.collection.rb_tree.LinkedRBTree;
import tools.log.Logger;
import tools.log.StreamLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConvexHull {
    private final LinkedRBTree<VectorYNode> left;
    private final LinkedRBTree<VectorYNode> right;
    private VectorYNode top;
    private VectorYNode bottom;
    
    
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
    
    
    public NearIntersection getPointsNearLine(Edge e) {
        VectorYNode vyn1 = null, vyn2 = null, vyn3 = null, vyn4 = null;
        
        // TODO: edge cases:
        //  - edge goes between 3 top vertices.
        //  - edge goes between 3 bottom vertices.
        //  - edge goes through vertex.
        double relOriTop = e.relOri(top.getVec());
        double relOriBot = e.relOri(bottom.getVec());
        if (relOriTop == 0) {
            Logger.write("through top");
            // TODO: edge case: edge goes through top.
            // Note: should not occur since no 3 points on one line.
            throw new RuntimeException();
            
        } else if (relOriBot == 0) {
            Logger.write("through bottom");
            // TODO: edge case: edge goes through bottom.
            // Note: should not occur since no 3 points on one line.
            throw new RuntimeException();

        } else if (relOriTop * relOriBot < 0) {
            // Line goes through both the left and right side.
            Logger.write("both sides");
            // Set direction of e to the right, if needed.
            if (e.x1() > e.x2()) {
                e = new Edge(e.v2(), e.v1());
            }
            System.out.println(left.getRoot());
            Logger.write("left side;");
            Pair<VectorYNode, VectorYNode> pair = getNodeAboveBothSides(left, e);
            vyn1 = pair.getFirst();
            vyn2 = pair.getSecond();
            Logger.write("right side:");
            pair = getNodeAboveBothSides(right, e);
            vyn3 = pair.getFirst();
            vyn4 = pair.getSecond();
            
        } else if (relOriTop > 0) {
            // Line lies on the left side.
            // Set direction of e to upwards, if needed.
            if (e.y1() > e.y2()) {
                e = new Edge(e.v2(), e.v1());
            }
            Logger.write("left side;");
            Pair<VectorYNode, VectorYNode> pair = getNodeAboveOneSide(left, e, true);
            vyn1 = pair.getFirst();
            vyn2 = pair.getSecond();
            Logger.write("right side:");
            pair = getNodeAboveOneSide(left, e, false);
            vyn3 = pair.getFirst();
            vyn4 = pair.getSecond();
            
        } else {
            // Line lies on the right side.
            // Set direction of e to downwards, if needed.
            if (e.y1() < e.y2()) {
                e = new Edge(e.v2(), e.v1());
            }
            Logger.write("right side");
            Pair<VectorYNode, VectorYNode> pair = getNodeAboveOneSide(right, e, true);
            vyn1 = pair.getFirst();
            vyn2 = pair.getSecond();
            pair = getNodeAboveOneSide(right, e, false);
            vyn3 = pair.getFirst();
            vyn4 = pair.getSecond();
        }
        
        return new NearIntersection(vyn1, vyn2, vyn3, vyn4);
    }

    /**
     * TODO
     * @param tree
     * @param e
     * @return
     */
    private Pair<VectorYNode, VectorYNode> getNodeAboveBothSides(LinkedRBTree<VectorYNode> tree, Edge e) {
        VectorYNode node = tree.getRoot();
        while (true) {
            double ori = e.relOri(node.getVec());
            Logger.write(ori);
            if (ori < 0) {
                VectorYNode next = next(node, tree == left);
                if (!node.hasRight()) return new Pair<>(next, node);
                if (e.relOri(next.getVec()) <= 0) return new Pair<>(next, node);
                if (node.hasRight()) node = node.right();
                
            } else if (ori > 0) {
                VectorYNode prev = prev(node, tree == left);
                if (!node.hasLeft()) return new Pair<>(node, prev);
                if (e.relOri(prev.getVec()) >= 0) return new Pair<>(node, prev);
                node = node.left();
                
            } else {
                return new Pair<>(next(node, tree == left), node);
            }
        }
    }

    /**
     * Determines the two points on the hull which are the closest to the given line.
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
                    else return new Pair<>(next(node, tree == left), node);
                }
            } else {
                if (node.getVec().y() > target) {
                    if (node.hasLeft()) node = node.left();
                    else return new Pair<>(node, prev(node, tree == left));
                }
            }
            
            double ori = e.relOri(node.getVec());
            if ((up && ori < 0) || (!up && ori > 0)) {
                VectorYNode next = next(node, tree == left);
                if (!node.hasRight()) return new Pair<>(next, node);
                if (e.relOri(next.getVec()) <= 0) return new Pair<>(next, node);
                if (node.hasRight()) node = node.right();

            } else if ((up && ori > 0) || (!up && ori < 0)) {
                VectorYNode prev = prev(node, tree == left);
                if (!node.hasLeft()) return new Pair<>(node, prev);
                if (e.relOri(prev.getVec()) >= 0) return new Pair<>(node, prev);
                node = node.left();
                
            } else {
                if (up) return new Pair<>(next(node, tree == left), node);
                else return new Pair<>(node, prev(node, tree == left));
            }
        }
    }

    /**
     * Gets the next node of the chain. If no next node exists, takes the maximum node of the
     * other chain.
     * 
     * @param node     The node to get the next node of.
     * @param fromLeft Whether the given node is from the left or the right tree.
     * 
     * @return The next node in the chain.
     */
    public VectorYNode next(VectorYNode node, boolean fromLeft) {
        if (node.next() != null) return node.next();
        if (fromLeft) return right.getMax();
        else return left.getMax();
    }

    /**
     * Gets the prevsiou node of the chain. If no previous node exists,
     * takes the minimum node of the other chain.
     *
     * @param node     The node to get the previous node of.
     * @param fromLeft Whether the given node is from the left or the right tree.
     *
     * @return The previous node in the chain.
     */
    public VectorYNode prev(VectorYNode node, boolean fromLeft) {
        if (node.prev() != null) return node.prev();
        if (fromLeft) return right.getMin();
        else return left.getMin();
    }
    
    
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
        Visualizer viz = new Visualizer();
        viz.setPoints(List.of(Visualizer.toVec(left), Visualizer.toVec(right)));
        viz.setEdges(List.of(lEdges, rEdges));
        viz.setLabels(List.of(Visualizer.toLabel(left), Visualizer.toLabel(right)));
        viz.redraw();
        
        Edge e = new Edge(new Vector(-5, 5.5), new Vector(5, 5.5));
        viz.addPoint(List.of(e.v1(), e.v2()));
        viz.addEdge(List.of(e));
        viz.redraw();
        
        ConvexHull ch = new ConvexHull(left, right);
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
