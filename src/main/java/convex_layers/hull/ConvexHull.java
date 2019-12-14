package convex_layers.hull;

import convex_layers.VectorYEdge;
import convex_layers.InputVertex;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import convex_layers.visual.Visualizer;
import tools.Pair;
import tools.Var;
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
                        ? new Edge(last, secLast)
                        : new Edge(secLast, last));
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
     * @apiNote This function runs in {@code O(log(n))}.
     * 
     * @param e       The edge to get the intersection of.
     * @param hasLeft Whether the given edge has nodes of it's convex hull on the left of it.
     * 
     * @return A {@link NearIntersection} with the points.
     */
    public NearIntersection getPointsNearLine(Edge e, boolean hasLeft) {
        VectorYNode vyn1, vyn2, vyn3, vyn4;
        NearIntersection.Orientation orientation;
        
        // TODO: edge cases:
        //  - edge goes through vertex (done?).
        double relOriTop = e.relOri(top.getVec());
        double relOriBot = e.relOri(bottom.getVec());
        if (relOriTop == 0) {
            // Note: should not occur since no 3 points on one line.
            // TODO: edge case: edge goes through top.
            Logger.write("through top", Logger.Type.ERROR);
            throw new RuntimeException();
            
        } else if (relOriBot == 0) {
            // Note: should not occur since no 3 points on one line.
            // TODO: edge case: edge goes through bottom.
            Logger.write("through bottom", Logger.Type.ERROR);
            throw new RuntimeException();
            
        } else if (relOriTop * relOriBot < 0) {
            Logger.write("BOTH");
            // Line goes through both the left and right side.
            // Set direction of e to the right relative to the edge (bottom, top), if needed.
            Edge bt = new Edge(bottom.getVec(), top.getVec());
            if (bt.relOriRounded(e.v1()) * bt.distance(e.v1()) > bt.relOriRounded(e.v2()) * bt.distance(e.v2())) {
                e = new Edge(e.v2(), e.v1());
                hasLeft = !hasLeft;
            }
            Pair<VectorYNode, VectorYNode> pair = getNodeAboveBothSides(left, e);
            vyn1 = pair.getFirst();
            vyn2 = pair.getSecond();
            pair = getNodeAboveBothSides(right, e);
            vyn3 = pair.getFirst();
            vyn4 = pair.getSecond();
            if (hasLeft) {
                orientation = NearIntersection.Orientation.BOTTOM;
                
            } else {
                orientation = NearIntersection.Orientation.TOP;
            }
            
        } else {
            // The edge goes twice though either the left side or the right side.
            // Therefore must the two points defining the edge lie on the same side
            // of the line.
            Edge bottomTopEdge = new Edge(bottom.getVec(), top.getVec());
            if (bottomTopEdge.relOri(e.v1()) < 0) {
                Logger.write("LEFT");
                // Edge goes through on the left side.
                // Set direction of e to upwards, if needed.
                if (e.y1() > e.y2()) {
                    e = new Edge(e.v2(), e.v1());
                    hasLeft = !hasLeft;
                }
                Pair<VectorYNode, VectorYNode> pair = getNodeAboveOneSide(left, e, true);
                vyn1 = pair.getFirst();
                vyn2 = pair.getSecond();
                pair = getNodeAboveOneSide(left, e, false);
                vyn4 = pair.getFirst();
                vyn3 = pair.getSecond();

            } else {
                Logger.write("RIGHT");
                // Edge goes through on the right side.
                // Set direction of e to downwards, if needed.
                if (e.y1() < e.y2()) {
                    e = new Edge(e.v2(), e.v1());
                    hasLeft = !hasLeft;
                }
                Pair<VectorYNode, VectorYNode> pair = getNodeAboveOneSide(right, e, true);
                vyn1 = pair.getFirst();
                vyn2 = pair.getSecond();
                pair = getNodeAboveOneSide(right, e, false);
                vyn4 = pair.getFirst();
                vyn3 = pair.getSecond();
            }
            if (hasLeft) {
                orientation = NearIntersection.Orientation.LEFT;
            } else {
                orientation = NearIntersection.Orientation.RIGHT;
            }
        }
        
        return new NearIntersection(vyn1, vyn2, vyn3, vyn4, orientation);
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
     * @apiNote This function runs in {@code O(1)}.
     * 
     * @param node The node to get the next node of.
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
     * @apiNote This function runs in {@code O(1)}.
     * 
     * @param node The node to get the previous node of.
     *
     * @return The previous node in the chain.
     */
    public VectorYNode prev(VectorYNode node) {
        if (node.prev() != null) return node.prev();
        if (left.getMin() == node) return right.getMin();
        else return left.getMin();
    }

    /**
     * Throws an exception stating that the given node is not part of the hull.
     * 
     * @param node The node which caused the exception.
     * 
     * @throws IllegalArgumentException Always.
     */
    private static void throwNotPartOfThisHullException(VectorYNode node)
            throws IllegalArgumentException {
        throw new IllegalArgumentException("The given node is not part of this hull: " + node);
    }
    
    /**
     * Traverses the hull in clockwise order.
     * 
     * @param node The node to get the next node for.
     * 
     * @return The next node in clockwise order.
     * 
     * @throws IllegalArgumentException If the given node is not part of this hull.
     */
    public VectorYNode clockwise(VectorYNode node) {
        if (size() == 0) throwNotPartOfThisHullException(node);
        Edge e = new Edge(bottom.getVec(), top.getVec());
        double ori = e.relOri(node.getVec());
        if (ori < 0) return next(node);
        else if (ori > 0) return prev(node);
        else {
            VectorYNode rtn;
            if (node == top) {
                if (top == left.getMax()) rtn = next(node);
                else rtn = prev(node);
                
            } else if (node == bottom) {
                if (bottom == left.getMin()) rtn = next(node);
                else rtn = prev(node);
                
            } else {
                throwNotPartOfThisHullException(node);
                return null;
            }
            
            if (rtn != null) return rtn;
            if (left.size() == 0 ^ right.size() == 0) {
                if (node == top) return bottom;
                else return top;
                
            } else {
                throwNotPartOfThisHullException(node);
                return null;
            }
        }
    }
    
    /**
     * Traverses the hull in counter clockwise order.
     *
     * @param node The node to get the next node for.
     *
     * @return The next node in counter clockwise order.
     *
     * @throws IllegalArgumentException If the given node is not part of this hull.
     */
    public VectorYNode counterClockwise(VectorYNode node) {
        if (size() == 0) throwNotPartOfThisHullException(node);
        Edge e = new Edge(bottom.getVec(), top.getVec());
        double ori = e.relOri(node.getVec());
        if (ori < 0) return prev(node);
        else if (ori > 0) return next(node);
        else {
            VectorYNode rtn;
            if (node == top) {
                if (top == left.getMax()) rtn = prev(node);
                else rtn = next(node);

            } else if (node == bottom) {
                if (bottom == left.getMin()) rtn = prev(node);
                else rtn = next(node);

            } else {
                throwNotPartOfThisHullException(node);
                return null;
            }
            
            if (rtn != null) return rtn;
            if (left.size() == 0 ^ right.size() == 0) {
                if (node == top) return bottom;
                else return top;

            } else {
                throwNotPartOfThisHullException(node);
                return null;
            }
        }
    }

    /**
     * @return A random edge from the hull.
     */
    public VectorYEdge getRandomEdge() {
        int ran = Var.RAN.nextInt(size());
        VectorYNode node = getNode(ran);
        if (ran < left.size()) {
            return new VectorYEdge(node, next(node));
        } else {
            return new VectorYEdge(node, prev(node));
        }
    }

    /**
     * Returns the input vertex at the given index. The indices are order from the minimal
     * to the maximal vertex of the left tree, an then from the maximal to the minimal
     * vertex of the right tree.
     *
     * @apiNote This function runs in {@code O(log(n))}.
     *
     * @param i The input vertex to get.
     *
     * @return The input vertex at the given index.
     */
    public InputVertex get(int i) {
        return getNode(i).getIv();
    }

    /**
     * Returns the node at the given index. The indices are order from the minimal
     * to the maximal node of the left tree, an then from the maximal to the minimal
     * node of the right tree.
     *
     * @apiNote This function runs in {@code O(log(n))}.
     *
     * @param i The node to get.
     *
     * @return The node at the given index.
     */
    public VectorYNode getNode(int i) {
        if (i < 0 || i >= size()) throw new IndexOutOfBoundsException();
        if (i < left.size()) return left.get(i);
        else return right.get(right.size() - (i - left.size() + 1));
    }
    
    /**
     * Adds a vertex to the hull.
     *
     * @apiNote This function runs in {@code O(log(n))}.
     * 
     * @param iv The vertex to add.
     * 
     * @return {@code true} if the vertex was added. {@code false} otherwise.
     */
    @Override
    public boolean add(InputVertex iv) {
        return add(new VectorYNode(iv));
    }
    
    /**
     * Adds an input vertex and updates the hull accordingly by removing vertices from the hull. <br>
     * If the just added point was inside the hull, then it will be removed directly and returned in
     * the returned list. This is the only way for the given vertex to be removed. <br>
     * Moreover, if the given vertex is removed, then no other vertices will be removed.
     *
     * @apiNote This function runs in {@code O(log(n) + k)}, where {@code k} denotes the number of removed elements.
     * 
     * @param iv The input vertex to add.
     * 
     * @return A list containing all removed vertices.
     */
    public List<InputVertex> addAndUpdate(InputVertex iv) {
        VectorYNode vyn = new VectorYNode(iv);
        List<InputVertex> rem = new ArrayList<>();
        if (!add(vyn) || size() <= 3) return rem;
        {
            VectorYNode prev = counterClockwise(vyn);
            VectorYNode next = clockwise(vyn);
            Edge e = new Edge(prev.getVec(), next.getVec());
            if (e.relOri(vyn.getVec()) > 0) {
                remove(vyn);
                rem.add(vyn.getIv());
                return rem;
            }
        }

        {
            VectorYNode prev = vyn;
            while (size() > 3) {
                VectorYNode cur = clockwise(prev);
                VectorYNode next = clockwise(cur);
                Edge e = new Edge(prev.getVec(), next.getVec());
                if (e.relOri(cur.getVec()) <= 0) {
                    break;
                }
                remove(cur);
                rem.add(cur.getIv());
                prev = cur;
            }
        }

        {
            VectorYNode next = vyn;
            while (size() > 3) {
                VectorYNode cur = counterClockwise(next);
                VectorYNode prev = counterClockwise(cur);
                Edge e = new Edge(prev.getVec(), next.getVec());
                if (e.relOri(cur.getVec()) <= 0) {
                    break;
                }
                remove(cur);
                rem.add(cur.getIv());
                next = cur;
            }
        }
        
        return rem;
        /*
        
        
        
        boolean leftList = (new Edge(bottom.getVec(), top.getVec()).relOri(vyn.getVec()) < 0);
        
        // Check if the added vertex lies on the hull. If not, remove it.
        if (vyn != top && vyn != bottom) {
            double ori = new Edge(prev(vyn).getVec(), next(vyn).getVec()).relOri(vyn.getVec());
            if ((leftList && ori > 0) || (!leftList && ori < 0)) {
                remove(vyn);
                rem.add(vyn.getIv());
                return rem;
            }
        }
        
        // Either fix the hull or resolve to a case where {@code vyn == top}.
        while (vyn != top && size() > 3) {
            Logger.write("LOOPING 1");
            VectorYNode nextNode;
            VectorYNode node = vyn.next();
            if (node == null) break;
            nextNode = next(node);
            double ori = new Edge(vyn.getVec(), node.getVec()).relOri(nextNode.getVec());
            if (vyn == top) break;
            if ((leftList && ori < 0) || (!leftList && ori > 0)) {
                rem.add(node.getIv());
                remove(node);
            }
        }
        
        // Either fix the hull or resolve to a case where {@code vyn == bottom}.
        while (vyn != bottom && size() > 3) {
            Logger.write("LOOPING 2");
            VectorYNode prevNode;
            VectorYNode node = vyn.prev();
            if (node == null) break;
            prevNode = prev(node);
            double ori = new Edge(node.getVec(), vyn.getVec()).relOri(prevNode.getVec());
            if (vyn == bottom) break;
            if ((leftList && ori < 0) || (!leftList && ori > 0)) {
                rem.add(node.getIv());
                remove(node);
            }
        }
        
        if ((vyn == top || vyn == bottom) && size() > 3) {
            // Remove invalid nodes on the left side.
            while (size() > 3) {
                Logger.write("LOOPING 3");
                VectorYNode l1 = (left.getMax() == vyn || right.getMin() == vyn
                        ? prev(vyn)
                        : next(vyn)
                );
                VectorYNode l2 = (vyn == top
                        ? prev(l1)
                        : next(l1)
                );
                
                Edge el = (vyn == top
                        ? new Edge(l2.getVec(), vyn.getVec())
                        : new Edge(vyn.getVec(), l2.getVec())
                );
                
                if (el.relOri(l1.getVec()) <= 0) break;
                rem.add(l1.getIv());
                remove(l1);
            }
            
            // Remove invalid nodes on the right side.
            while (size() > 3) {
                Logger.write("LOOPING 4");
                VectorYNode r1 = (left.getMax() == vyn || right.getMin() == vyn
                        ? next(vyn)
                        : prev(vyn)
                );
                VectorYNode r2 = (vyn == top
                        ? prev(r1)
                        : next(r1)
                );
                
                Edge er = (vyn == top
                        ? new Edge(r2.getVec(), vyn.getVec())
                        : new Edge(vyn.getVec(), r2.getVec())
                );
                if (er.relOri(r1.getVec()) >= 0) break;
                rem.add(r1.getIv());
                remove(r1);
            }
        }
        
        return rem;*/
    }
    
    /**
     * Inserts a node in the chain.
     * 
     * @param vyn The node to add.
     * 
     * @return {@code true} if the node was added. {@code false} otherwise.
     */
    private boolean add(VectorYNode vyn) {
        if (size() == 0) {
            return left.add(top = bottom = vyn);
        }
        boolean newBound = false;
        if (vyn.getVec().y() > top.getVec().y()) {
            newBound = true;
            top = vyn;
            
        } else if (vyn.getVec().y() < bottom.getVec().y()) {
            newBound = true;
            bottom = vyn;
        }
        
        if (newBound) {
            if (left.size() > right.size()) return right.add(vyn);
            else return left.add(vyn);

        } else {
            Edge e = new Edge(bottom.getVec(), top.getVec());
            if (e.relOri(vyn.getVec()) < 0) return left.add(vyn);
            else return right.add(vyn);
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @apiNote This function runs in {@code O(log(n))}.
     */
    @Override
    public boolean remove(Object obj) {
        if (obj instanceof InputVertex) return remove((InputVertex) obj);
        if (obj instanceof VectorYNode) return remove((VectorYNode) obj);
        return false;
    }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote This function runs in {@code O(log(n) + k)}, where {@code k} denotes the size of the collection.
     */
    @Override
    public boolean containsAll(Collection<?> col) {
        for (Object obj : col) {
            if (!contains(obj)) return false;
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote This function runs in {@code O(log(n) + k)}, where {@code k} denotes the size of the collection.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends InputVertex> col) {
        return addAll((Iterable<InputVertex>) col);
    }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote This function runs in {@code O(log(n) + k)}, where {@code k} denotes the size of the collection.
     */
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
        if (isEmpty()) return false;
        Edge e = new Edge(bottom.getVec(), top.getVec());
        double ori = e.relOri(vyn.getVec());
        if (ori < 0) return left.remove(vyn);
        else if (ori > 0) return right.remove(vyn);
        else {
            if (!left.remove(vyn) && !right.remove(vyn)) return false;
            if (isEmpty()) {
                top = bottom = null;
                
            } else if (left.isEmpty()) {
                top = right.getMax();
                bottom = right.getMin();

            } else if (right.isEmpty()) {
                top = left.getMax();
                bottom = left.getMin();
                
            } else {
                top = (left.getMax().getIv().getY() > right.getMax().getIv().getY()
                        ? left.getMax()
                        : right.getMax()
                );
                bottom = (left.getMin().getIv().getY() < right.getMin().getIv().getY()
                        ? left.getMin()
                        : right.getMin()
                );
            }
            return true;
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

    /**
     * {@inheritDoc}
     *
     * @apiNote This function runs in {@code O(log(n))}.
     */
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
                return it.next().getIv();
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
    
    /**
     * @return An iterator over the left list.
     */
    public Iterator<InputVertex> leftIterator() {
        return convert(left.iterator());
    }
    
    /**
     * @return An iterator over the left list.
     */
    public Iterator<InputVertex> rightIterator() {
        return convert(right.iterator());
    }
    
    /**
     * Returns the right list. <br>
     * <b>WARNING</b> <br>
     * Modifying this list <b>will</b> modify the underlying data structure.
     * 
     * @return The right list.
     */
    public Iterable<VectorYNode> getLeft() {
        return left;
    }
    
    /**
     * Returns the left list. <br>
     * <b>WARNING</b> <br>
     * Modifying this list <b>will</b> modify the underlying data structure.
     *
     * @return The left list.
     */
    public Iterable<VectorYNode> getRight() {
        return right;
    }
    
    /**
     * @return An iterable of the input vertices of the left list.
     */
    public Iterable<InputVertex> getLeftInput() {
        return () -> convert(left.iterator());
    }
    
    /**
     * @return An iterable of the input vertices of the right list.
     */
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
            if (i >= arr.length) break;
            arr[i++] = (T) iv;
        }
        return arr;
    }

    /**
     * @return The top element, or {@code null} if the hull is empty.
     */
    public InputVertex getTop() {
        return (size() == 0
                ? null
                : top.getIv()
        );
    }

    /**
     * @return The bottom element, or {@code null} if the hull is empty.
     */
    public InputVertex getBottom() {
        return (size() == 0
                ? null
                : bottom.getIv()
        );
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
        
        
        left.add(new InputVertex(98, -5, 45));
        right.add(new InputVertex(99, 20, 10));
        
        List<InputVertex> combi = new ArrayList<>(left);
        combi.addAll(right);
        ConvexHull ch = ConvexHull.createConvexHull(combi);
        Visualizer vis = new Visualizer();
        
        vis.setData(List.of(left, right));
        vis.redraw();
        vis.addData(List.of(ch));
        vis.redraw();
        vis.addData(List.of(ch.getLeftInput(), ch.getRightInput()));
        vis.redraw();
        Edge e = new Edge(new Vector(1, 5), new Vector(-1, 30));
        vis.addPoint(List.of(e.v1(), e.v2()));
        vis.addEdge(List.of(e));
        
        NearIntersection ni = ch.getPointsNearLine(e, true);
        Logger.write(ni);
        vis.addEdge(List.of(
                new Edge(ni.v1.getVec(), ni.v2.getVec()),
                new Edge(ni.v2.getVec(), ni.v4.getVec()),
                new Edge(ni.v4.getVec(), ni.v3.getVec()),
                new Edge(ni.v3.getVec(), ni.v1.getVec())
        ));
        vis.redraw();
        
        vis.setData(List.of(ch));
        vis.redraw();
        vis.addData(List.of(ch.getLeftInput(), ch.getRightInput()));
        vis.redraw();
        Logger.write(ch.addAndUpdate(new InputVertex(50, -21, 60)));
        vis.redraw();
        vis.addData(List.of(ch));
        vis.redraw();
    }
    
    
}
