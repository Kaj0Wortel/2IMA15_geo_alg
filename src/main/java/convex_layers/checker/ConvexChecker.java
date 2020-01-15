package convex_layers.checker;

import convex_layers.BaseInputVertex;
import convex_layers.OutputEdge;
import convex_layers.Problem2;
import convex_layers.hull.ConvexHull;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Checker implementation which checks whether the edges and points are convex. <br>
 * Runtime:
 * <table border="1">
 *     <tr><th>Case</th><th>Runtime</th></tr>
 *     <tr><td align="right">Worst case</td>{@code |V|log|V| + |E|log|E|}</tr>
 *     <tr><td align="right">Average case</td>{@code |V|log|V| + |E|}</tr>
 * </table>
 * The worst case is obtained if (almost) all edges have a single point in common,
 * which is highly unlikely due to the way the edges are generated.
 */
public class ConvexChecker
        implements Checker {
    
    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    /**
     * Node class for representing the edges to take into account when checking
     * whether the point is convex..
     */
    @RequiredArgsConstructor
    private static class Node
            implements Iterable<OutputEdge> {
        
        /** The vertex the edges connect to. */
        @NonNull
        @Getter
        private final BaseInputVertex biv;
        /** The edges containing to {@link #biv} */
        @NonNull
        private final List<OutputEdge> edges = new ArrayList<>();
        
        
        /**
         * Adds the given edge to this node.
         * 
         * @param e The edge to add.
         */
        public void add(OutputEdge e) {
            edges.add(e);
        }
        
        @Override
        public Iterator<OutputEdge> iterator() {
            Edge vertEdge = new Edge(biv.getV(), new Vector(biv.getX(), biv.getY() + 100));        
            edges.sort((e1, e2) -> {
                BaseInputVertex other1 = (e1.getV1().equals(biv) ? e1.getV2() : e1.getV1());
                BaseInputVertex other2 = (e2.getV1().equals(biv) ? e2.getV2() : e2.getV1());
                if (other1.getV().equals(other2.getV())) return 0;
                
                double ori1 = vertEdge.relOri(other1.getV());
                double ori2 = vertEdge.relOri(other2.getV());
                if (ori1 < 0 && ori2 >= 0) return 1;
                else if (ori2 < 0 && ori1 >= 0) return -1;
                else if (ori1 == 0 && ori2 == 0) {
                    double diff = other1.getY() - other2.getY();
                    if (diff < 0) return -1;
                    else if (diff > 0) return 1;
                    else return 0;
                } else {
                    Edge e = new Edge(biv.getV(), other2.getV());
                    int ori = e.relOriRounded(other1.getV());
                    return ori;
                }
            });
            return edges.iterator();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) return false;
            return ((Node) obj).biv.equals(biv);
        }
        
        @Override
        public int hashCode() {
            return biv.hashCode();
        }
        
        
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public CheckerError check(Problem2 problem, Collection<OutputEdge> sol) {
        CheckerError err = new CheckerError();
        
        // Separate the vertices into the outer hull and the inner vertices.
        Collection<BaseInputVertex> inner = new HashSet<>(problem.getVertices());
        Collection<BaseInputVertex> hull = ConvexHull.createConvexHull(problem.getVertices());
        inner.removeAll(hull);
        
        // Create nodes in the map for all vertices.
        Map<BaseInputVertex, Node> nodes = new HashMap<>();
        for (BaseInputVertex biv : problem) {
            nodes.put(biv, new Node(biv));
        }
        
        // Add the edges to the corresponding nodes.
        // Note that only edges from the outer hull to an inner node are ignored here,
        // assuming bi-directional edges.
        for (OutputEdge e : sol) {
            boolean in1 = inner.contains(e.getV1());
            boolean in2 = inner.contains(e.getV2());
            if (in1 == in2) {
                nodes.get(e.getV1()).add(e);
                nodes.get(e.getV2()).add(e);

            } else if (in1) nodes.get(e.getV1()).add(e);
            else nodes.get(e.getV2()).add(e);
        }
        
        // First check the inner nodes.
        for (BaseInputVertex biv : inner) {
            Node node = nodes.get(biv);
            OutputEdge first = null;
            OutputEdge prev = null;
            for (OutputEdge e : node) {
                if (first == null) {
                    prev = first = e;
                    continue;
                }
                checkEdges(prev, e, biv, err);
                prev = e;
            }
            if (first == prev) {
                err.addPoint(node.getBiv());
                if (first != null) err.addEdge(first);
                
            } else {
                checkEdges(prev, first, biv, err);
            }
        }
        
        // Then check the outer nodes.
        if (hull.size() > 3) {
            Node first = null, second = null;
            Node prev = null, cur = null;
            for (BaseInputVertex biv : hull) {
                if (first == null) {
                    first = prev = nodes.get(biv);
                    continue;
                } else if (second == null) {
                    second = cur = nodes.get(biv);
                    continue;
                }
                
                Node next = nodes.get(biv);
                checkHull(prev, cur, next, err);
                prev = cur;
                cur = next;
            }
            checkHull(prev, cur, first, err);
            checkHull(cur, first, second, err);
        }
        
        // Return the errors.
        return err;
    }

    /**
     * Checks the given nodes on the hull. <br>
     * The nodes must have at 
     * 
     * @param prev The previous node on the hull.
     * @param cur  The current node on the hull.
     * @param next The next node on the hull.
     * @param err  The checker error to update.
     */
    private void checkHull(Node prev, Node cur, Node next, CheckerError err) {
        Iterator<OutputEdge> it = cur.iterator();
        if (!it.hasNext()) err.addPoint(cur.getBiv());
        OutputEdge out1 = it.next();
        if (!it.hasNext()) err.addPoint(cur.getBiv());
        OutputEdge out2 = it.next();
        if (it.hasNext()) {
            err.addPoint(cur.getBiv());
            for (OutputEdge e : cur) {
                err.addEdge(e);
            }
        }
        OutputEdge e1 = match(out1, out2, prev.getBiv());
        OutputEdge e2 = match(out1, out2, next.getBiv());

        checkEdges(e2, e1, cur.getBiv(), err);
    }

    /**
     * Matches the output edge with the given target vertex.
     * It is assumed that at least one of the two edges contains the target.
     * If both edges contain the target, then the first edge is returned.
     * 
     * @param e1     The first edge to compare.
     * @param e2     The second edge to compare.
     * @param target The target to find.
     * 
     * @return The edge which has the given target.
     */
    private OutputEdge match(OutputEdge e1, OutputEdge e2, BaseInputVertex target) {
        if (e1.getV1().equals(target) || e1.getV2().equals(target)) return e1;
        else return e2;
    }

    /**
     * Checks whether the angle between the two edges is less or equal to 180 degrees,
     * assuming clockwise rotation.
     * 
     * @param e1     The first edge to check.
     * @param e2     The second edge to check.
     * @param target The target vertex both edges have in common.
     * @param err    The checker error to update.
     */
    private void checkEdges(OutputEdge e1, OutputEdge e2, BaseInputVertex target, CheckerError err) {
        BaseInputVertex other1 = (e1.getV1().equals(target) ? e1.getV2() : e1.getV1());
        BaseInputVertex other2 = (e2.getV1().equals(target) ? e2.getV2() : e2.getV1());
        if (other1.equals(other2)) {
            err.addEdges(List.of(e1, e2));
            err.addPoint(target);
            return;
        }
        Edge e = new Edge(target.getV(), other1.getV());
        double ori = e.relOri(other2.getV());
        if (ori < 0) {
            err.addEdges(List.of(e1, e2));
            err.addPoint(target);
        }
    }
    
    
}
