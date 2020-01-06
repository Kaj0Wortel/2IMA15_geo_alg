package convex_layers.hull;

import convex_layers.InputVertex;
import convex_layers.OutputEdge;
import convex_layers.BaseInputVertex;
import convex_layers.math.Edge;
import tools.Pair;
import tools.Var;
import tools.data.collection.rb_tree.LinkedRBTree;
import tools.iterators.FunctionIterator;
import tools.iterators.InverseListIterator;
import tools.iterators.MultiIterator;
import tools.log.Logger;

import java.util.*;


/**
 * This class represents the convex hull data structure.
 * This data structure can be used for storing and updating a convex hull,
 * and performing fast search queries on the hull. 
 */
public class ConvexHull<IV extends BaseInputVertex>
            implements Collection<IV> {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The search tree for the left part of the hull. */
    private final LinkedRBTree<VectorYNode<IV>> left;
    /** The search tree for the right part of the hull. */
    private final LinkedRBTree<VectorYNode<IV>> right;
    /** The element with the maximum y-coordinate. */
    private VectorYNode<IV> top;
    /** The element with the minimum y-coordinate. */
    private VectorYNode<IV> bottom;
    /** The element with the minimum x-coordinate. */
    private VectorYNode<IV> minX;
    /** The element with the maximum x-coordinate. */
    private VectorYNode<IV> maxX;
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates a new convex hull data structure from the given left and right half-hulls.
     *
     * @apiNote Runs in {@code O(n*log(n))} if the points are not sorted,
     *     or {@code O(n)} if the points are (almost) sorted.
     * 
     * @param lCol The left side of the convex hull.
     * @param rCol The right side of the convex hull.
     */
    public ConvexHull(Collection<IV> lCol, Collection<IV> rCol) {
        List<VectorYNode<IV>> lList = new ArrayList<>(lCol.size());
        List<VectorYNode<IV>> rList = new ArrayList<>(rCol.size());
        for (IV iv : lCol) {
            lList.add(new VectorYNode<>(iv, this, true));
        }
        for (IV iv : rCol) {
            rList.add(new VectorYNode<>(iv, this, false));
        }
        left = new LinkedRBTree<>(lList);
        right = new LinkedRBTree<>(rList);
        top = (left.getMax().compareTo(right.getMax()) <= 0
                ? right.getMax()
                : left.getMax());
        bottom = (left.getMin().compareTo(right.getMin()) > 0
                ? right.getMin()
                : left.getMin());
        updateMinMaxX();
    }
    
    
    /* ----------------------------------------------------------------------
     * Static initializers.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates a new convex hull fom the given data set.
     *
     * @apiNote Runs in {@code O(n*log(n))} if the points are not sorted,
     *     or {@code O(n)} if the points are (almost) sorted.
     * 
     * @param col The collection to create the hull from.
     * 
     * @return A convex hull from the collection.
     */
    public static <IV extends BaseInputVertex> ConvexHull<IV> createConvexHull(Collection<IV> col) {
        // Filter out edge cases.
        if (col.isEmpty()) return new ConvexHull<>(List.of(), List.of());
        if (col.size() == 1) return new ConvexHull<>(col, List.of());
        if (col.size() == 2) {
            Iterator<IV> it = col.iterator();
            return new ConvexHull<>(List.of(it.next()), List.of(it.next()));
        }
        
        // Determine the min and max x and y elements.
        IV minX = null;
        IV maxX = null;
        IV minY = null;
        IV maxY = null;
        for (IV iv : col) {
            if (minX == null) {
                minX = maxX = minY = maxY = iv;
                continue;
            }
            if (iv.getX() < minX.getX()) minX = iv;
            if (iv.getX() > maxX.getX()) maxX = iv;
            if (iv.getY() < minY.getY()) minY = iv;
            if (iv.getY() > maxY.getY()) maxY = iv;
        }
        
        // Split the dataset into left and right.
        List<IV> left = new LinkedList<>();
        List<IV> right = new LinkedList<>();
        Edge e = new Edge(minY.getV(), maxY.getV());
        for (IV iv : col) {
            double ori = e.relOri(iv.getV());
            if (ori < 0) left.add(iv);
            else if (ori > 0) right.add(iv);
            else {
                left.add(iv);
                right.add(iv);
            }
        }
        
        // Sort the datasets.
        left.sort(createCmp(minX, true));
        right.sort(createCmp(maxX, false));
        
        // Compute the hull of both sides.
        left = computeHalfRightHull(left, true);
        right = computeHalfRightHull(right, false);
        
        // Remove the minY and maxY values from the biggest list.
        {
            List<IV> big = (left.size() > right.size() ? left : right);
            List<IV> small = (left.size() <= right.size() ? left : right);
            if (big.contains(minY) && small.contains(minY)) big.remove(minY);
            if (big.contains(maxY) && small.contains(maxY)) big.remove(maxY);
        }
        
        // Initialize and return the convex hull.
        return new ConvexHull<>(left, right);
    }
    
    /**
     * Creates a comparator with the given split vertex, and whether it is meant for the
     * left of the right tree.
     * 
     * @param split The split vertex.
     * @param left  Whether it is intended for the left of the right tree.
     * 
     * @return A comparator to sort the left/right sub tree.
     */
    private static <IV extends BaseInputVertex> Comparator<IV> createCmp(final IV split, final boolean left) {
        return (iv1, iv2) -> {
            double yDiff = iv1.getY() - iv2.getY();
            if (yDiff < 0) return Math.min(-1, (int) yDiff);
            else if (yDiff > 0) return Math.max(1, (int) yDiff);
            else if (yDiff == 0) {
                double xDiff = iv1.getX() - iv2.getX();
                if (iv1.getX() < split.getX() == left) {
                    if (xDiff < 0) return Math.max(1, (int) xDiff);
                    else if (xDiff > 0) return Math.min(-1, (int) xDiff);
                    
                } else if (iv1.getX() > split.getX() == left) {
                    if (xDiff < 0) return Math.min(-1, (int) xDiff);
                    else if (xDiff > 0) return Math.max(1, (int) xDiff);
                }
            }
            return 0;
        };
    }

    /**
     * Computes the right half convex hull of the given points. Assume that the points are sorted
     * on y-coordinate, or computes the lower half convex hull if sorted on x-coordinate. <br>
     * To obtain the other half call with {@code inverse == true.}
     * 
     * @apiNote Runs in {@code O(n)}.
     * 
     * @param in      The input vertices.
     * @param inverse Whether the other side of the hull should be computed.
     * 
     * @return The vertices on the convex hull.
     */
    private static <IV extends BaseInputVertex> List<IV> computeHalfRightHull(List<IV> in, boolean inverse) {
        Stack<IV> out = new Stack<>();
        for (IV iv : in) {
            if (out.size() < 2) {
                out.add(iv);
                continue;
            }

            IV last;
            double ori;
            do {
                last = out.pop();
                IV secLast = out.peek();
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
     * @apiNote Runs in {@code O(log(n))}.
     * 
     * @param vye     The edge to get the intersection of.
     * @param hullOnLeftSide Whether the given edge has nodes of it's convex hull on the left of it.
     * 
     * @return A {@link NearIntersection} with the points.
     * 
     * @throws IllegalStateException If {@link #isEmpty()}{@code == true}.
     * @throws IllegalArgumentException If the line created from the given edge
     *     intersects the top/bottom of the hull.
     */
    public NearIntersection<IV> getPointsNearLine(VectorYEdge<IV> vye, boolean hullOnLeftSide) {
        if (isEmpty()) throw new IllegalStateException("This function cannot be called on an empty hull.");
        
        VectorYNode<IV> vyn1, vyn2, vyn3, vyn4;
        Pair<VectorYNode<IV>, VectorYNode<IV>> pair1;
        Pair<VectorYNode<IV>, VectorYNode<IV>> pair2;
        Edge e = vye.toEdge();
        boolean clockwise;
        boolean flipped = false;
        
        // TODO: edge cases:
        //  - edge goes through vertex (done?).
        double relOriTop = e.relOri(top.getVec());
        double relOriBot = e.relOri(bottom.getVec());
        if (relOriTop == 0) {
            // Top vertex lies on the line
            // Note: should not occur since no 3 points on one line.
            // TODO: edge case: edge goes through top.
            Logger.write("through top", Logger.Type.ERROR);
            throw new IllegalArgumentException();
            
        } else if (relOriBot == 0) {
            // Bottom vertex lies on the line
            // Note: should not occur since no 3 points on one line.
            // TODO: edge case: edge goes through bottom.
//            Logger.write("through bottom", Logger.Type.ERROR);
            throw new IllegalArgumentException();
            
        } else if (relOriTop * relOriBot < 0) {
//            Logger.write("BOTH");
            // Line goes through both the left and right side of the hull.
            // Set direction of e to the right relative to the edge (bottom, top), if needed.
            Edge bt = getBottomTopEdge();
            
            if (bt.relOriRounded(e.v1()) * bt.distance(e.v1()) > bt.relOriRounded(e.v2()) * bt.distance(e.v2())) {
                // When both points of line lie:
                // - in separate sides, ensure v1 is in the left part (line points to the 'right')
                // - in the 'left' side, ensure the line points towards the middle line (line points to the 'right')
                // - in the 'right' side, ensure the line points away from the middle line (line points to the 'right')
                e = new Edge(e.v2(), e.v1());
                hullOnLeftSide = !hullOnLeftSide;
//                Logger.write("FLIP");
                flipped = true;
            }
            pair1 = getNodeAboveBothSides(left, e);
            pair2 = getNodeAboveBothSides(right, e);

            if (hullOnLeftSide) {
                vyn1 = pair1.getFirst();
                vyn2 = pair1.getSecond();
                vyn3 = pair2.getSecond();
                vyn4 = pair2.getFirst();
                clockwise = false;
            } else {
                vyn1 = pair1.getSecond();
                vyn2 = pair1.getFirst();
                vyn3 = pair2.getFirst();
                vyn4 = pair2.getSecond();
                clockwise = true;
            }
            
        } else {
            // The edge goes twice though either the left side or the right side.
            // Therefore must the two points defining the edge lie on the same side
            // of the line.
            Edge bottomTopEdge = getBottomTopEdge();
            if (bottomTopEdge.relOri(e.v1()) < 0) {
//                Logger.write("LEFT");
                // Edge goes through on the left side.
                // Set direction of e to upwards, if needed.
                if (e.y1() > e.y2()) {
                    e = new Edge(e.v2(), e.v1());
                    hullOnLeftSide = !hullOnLeftSide;
//                    Logger.write("FLIP");
                } else flipped = true;
                pair1 = getNodeAboveOneSide(left, e, true);  // Up
                pair2 = getNodeAboveOneSide(left, e, false); // Down

                if (hullOnLeftSide) {
                    vyn1 = pair1.getSecond();
                    vyn2 = pair1.getFirst();
                    vyn3 = pair2.getSecond();
                    vyn4 = pair2.getFirst();
                    clockwise = true;
                } else {
                    vyn1 = pair1.getFirst();
                    vyn2 = pair1.getSecond();
                    vyn3 = pair2.getFirst();
                    vyn4 = pair2.getSecond();
                    clockwise = false;
                }

            } else {
//                Logger.write("RIGHT");
                // Edge goes through on the right side.
                // Set direction of e to downwards, if needed.
                if (e.y1() < e.y2()) {
                    e = new Edge(e.v2(), e.v1());
                    hullOnLeftSide = !hullOnLeftSide;
//                    Logger.write("FLIP");
                    flipped = true;
                }
                
                pair1 = getNodeAboveOneSide(right, e, true);  // Up
                pair2 = getNodeAboveOneSide(right, e, false); // Down
                
                if (hullOnLeftSide) {
                    vyn1 = pair1.getSecond();
                    vyn2 = pair1.getFirst();
                    vyn3 = pair2.getSecond();
                    vyn4 = pair2.getFirst();
                    clockwise = false;
                } else {
                    vyn1 = pair1.getFirst();
                    vyn2 = pair1.getSecond();
                    vyn3 = pair2.getFirst();
                    vyn4 = pair2.getSecond();
                    clockwise = true;
                }
            }
        }
        
//        Logger.write("VYEdge   : " + vye);
//        Logger.write("hasLeft  : " + hullOnLeftSide);
//        Logger.write("clockwise: " + clockwise);
//        Logger.write(new Object[] {
//                "  vyn1: " + vyn1,
//                "  vyn2: " + vyn2,
//                "  vyn3: " + vyn3,
//                "  vyn4: " + vyn4
//        });
        VectorYNode<IV> v1, v2;
        if (!flipped) {
            v1 = vye.getIv1();
            v2 = vye.getIv2();
        } else {
            v1 = vye.getIv2();
            v2 = vye.getIv1();
        }
        return new NearIntersection<>(vyn1, vyn2, vyn3, vyn4, clockwise, v1, v2, hullOnLeftSide);
    }
    
    /**
     * Determines the two points on the hull which are the closest to the given line. <br>
     * The following is here assumed:
     * <ul>
     *     <li>The line goes through both the left and the right side.</li>
     *     <li>The line is directed towards the right relative to the line through
     *         the bottom and top point..</li>
     * </ul>
     *
     * @apiNote Runs in {@code O(log(n))}.
     * 
     * @param tree The tree to determine the intersections with.
     * @param e    The line to calculate the intersections for.
     * 
     * @return The two points near the intersection, where the first point has the largest y-coordinate.
     */
    private Pair<VectorYNode<IV>, VectorYNode<IV>> getNodeAboveBothSides(
            LinkedRBTree<VectorYNode<IV>> tree, Edge e) {
        VectorYNode<IV> node = tree.getRoot();
        while (true) {
            double ori = e.relOri(node.getVec());
            if (ori < 0) {
                VectorYNode<IV> prev = prev(node);
                if (!node.hasLeft()) return new Pair<>(node, prev);
                if (e.relOri(prev.getVec()) >= 0) return new Pair<>(node, prev);
                node = node.left();
                
            } else if (ori > 0) {
                VectorYNode<IV> next = next(node);
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
     * @apiNote Runs in {@code O(log(n))}.
     * 
     * @param tree The tree to determine the intersections with.
     * @param e    The line to calculate the intersections for.
     * @param up   Whether the upper or lower intersection should be calculated.
     *
     * @return The two points near the intersection, where the first point has the largest y-coordinate.
     */
    private Pair<VectorYNode<IV>, VectorYNode<IV>> getNodeAboveOneSide(
            LinkedRBTree<VectorYNode<IV>> tree, Edge e, boolean up) {
        VectorYNode<IV> node = tree.getRoot();
        double target = (up
                ? Math.max(e.v1().y(), e.v2().y())
                : Math.min(e.v1().y(), e.v2().y()));
        while (true) {
            if (up) {
                if (node.getVec().y() < target) {
                    if (node.hasRight()) node = node.right();
                    else return new Pair<>(next(node), node);
                    continue;
                }
            } else {
                if (node.getVec().y() > target) {
                    if (node.hasLeft()) node = node.left();
                    else return new Pair<>(node, prev(node));
                    continue;
                }
            }
            
            double ori = e.relOri(node.getVec());
            if ((up && ori < 0) || (!up && ori > 0)) {
                VectorYNode<IV> next = next(node);
                if (!node.hasRight()) return new Pair<>(next, node);
                if (e.relOri(next.getVec()) * ori < 0) return new Pair<>(next, node);
                if (node.hasRight()) node = node.right();
                
            } else if ((up && ori > 0) || (!up && ori < 0)) {
                VectorYNode<IV> prev = prev(node);
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
     * @apiNote Runs in {@code O(log(n))}.
     * 
     * Initialises the {@link #minX} and {@link #maxX} fields.
     */
    private void updateMinMaxX() {
        minX = maxX = null;
        if (isEmpty()) return;
        
        // Update minX.
        if (top.getVec().x() < bottom.getVec().x()) minX = top;
        else minX = bottom;
        if (!left.isEmpty()) {
            VectorYNode<IV> node = left.getRoot();
            VectorYNode<IV> last;
            do {
                last = node;
                VectorYNode<IV> next = next(node);
                VectorYNode<IV> prev = prev(node);
                double dNext = node.getVec().x() - next.getVec().x();
                double dPrev = node.getVec().x() - prev.getVec().x();
                if ((dNext < 0 && dPrev < 0) || (dNext == 0 || dPrev == 0)) break;
                else if (dNext < 0) node = node.left();
                else if (dPrev < 0) node = node.right();
                else throw new IllegalStateException();
            } while (node != null);
            minX = (minX.getVec().x() < last.getVec().x()
                    ? minX
                    : last
            );
        }
        
        // Update maxX.
        if (top.getVec().x() > bottom.getVec().x()) maxX = top;
        else maxX = bottom;
        if (!right.isEmpty()) {
            VectorYNode<IV> node = right.getRoot();
            VectorYNode<IV> last;
            do {
                last = node;
                VectorYNode<IV> next = next(node);
                VectorYNode<IV> prev = prev(node);
                double dNext = node.getVec().x() - next.getVec().x();
                double dPrev = node.getVec().x() - prev.getVec().x();
                if ((dNext > 0 && dPrev > 0) || (dNext == 0 || dPrev == 0)) break;
                else if (dNext > 0) node = node.left();
                else if (dPrev > 0) node = node.right();
                else throw new IllegalStateException();
            } while (node != null);
            maxX = (maxX.getVec().x() > last.getVec().x()
                    ? maxX
                    : last
            );
        }
    }
    
    /**
     * Gets the next node of the chain. If no next node exists, takes the maximum node of the
     * other chain. If this node also doesn't exist, then the same node is returned.
     *
     * @apiNote Runs in {@code O(1)}.
     * 
     * @param node The node to get the next node of.
     * 
     * @return The next node in the chain.
     */
    public VectorYNode<IV> next(VectorYNode<IV> node) {
        if (node.next() != null) return node.next();
        else if (left.getMax() == node) {
            if (right.isEmpty()) return node;
            else return right.getMax();
            
        } else{
            if (left.isEmpty()) return node;
            else return left.getMax();
        }
    }
    
    /**
     * Gets the previous node of the chain. If no previous node exists, takes the minimum node
     * of the other chain. If this node also doesn't exist, then the same node is returned.
     *
     * @apiNote Runs in {@code O(1)}.
     * 
     * @param node The node to get the previous node of.
     *
     * @return The previous node in the chain.
     */
    public VectorYNode<IV> prev(VectorYNode<IV> node) {
        if (node.prev() != null) return node.prev();
        else if (left.getMin() == node) {
            if (right.isEmpty()) return node;
            else return right.getMin();
            
        } else {
            if (left.isEmpty()) return node;
            else return left.getMin();
        }
    }
    
    /**
     * Throws an exception stating that the given node is not part of the hull.
     * 
     * @param node The node which caused the exception.
     * 
     * @throws IllegalArgumentException Always.
     */
    private static <IV extends BaseInputVertex> void throwNotPartOfThisHullException(VectorYNode<IV> node)
            throws IllegalArgumentException {
        throw new IllegalArgumentException("The given node is not part of this hull: " + node);
    }
    
    /**
     * Traverses the hull in clockwise order.
     *
     * @apiNote Runs in {@code O(1)}.
     * 
     * @param node The node to get the next node for.
     * 
     * @return The next node in clockwise order.
     * 
     * @throws IllegalArgumentException If the given node is not part of this hull.
     */
    public VectorYNode<IV> clockwise(VectorYNode<IV> node) {
        if (size() == 0) throwNotPartOfThisHullException(node);
        
        if (node.isLeft()) {
            if (node.next() != null) return node.next();
            else if (right.isEmpty()) return left.getMin();
            else return right.getMax();
            
        } else {
            if (node.prev() != null) return node.prev();
            else if (left.isEmpty()) return right.getMax();
            else return left.getMin();
        }
        /*
        Edge e = getBottomTopEdge();
        double ori = e.relOri(node.getVec());
        if (ori < 0) {
            if (node.next() != null) return node.next();
            else if (right.isEmpty()) return left.getMin();
            else return right.getMax();
            
        } else if (ori > 0) {
            if (node.prev() != null) return node.prev();
            else if (left.isEmpty()) return right.getMax();
            else return left.getMin();
            
        } else {
            VectorYNode<IV> rtn;
            if (node == top) {
                if (top == right.getMax()) {
                    if (right.size() == 1) rtn = left.getMin();
                    else rtn = node.prev();
                } else rtn = right.getMax();
                
            } else if (node == bottom) {
                if (bottom == left.getMin()) {
                    if (left.size() == 1) rtn = right.getMax();
                    else rtn = node.next();
                } else rtn = left.getMin();
                
            } else {
                throwNotPartOfThisHullException(node);
                return null;
            }
            
            if (rtn != null) return rtn;
            // Handle cases where either of the two sides is empty.
            if (left.size() == 0 ^ right.size() == 0) {
                if (node == top) return bottom;
                else return top;
                
            } else {
                throwNotPartOfThisHullException(node);
                return null;
            }
        }*/
    }
    
    /**
     * Traverses the hull in counter clockwise order.
     * 
     * @apiNote Runs in {@code O(1)}.
     *
     * @param node The node to get the next node for.
     *
     * @return The next node in counter clockwise order.
     *
     * @throws IllegalArgumentException If the given node is not part of this hull.
     */
    public VectorYNode<IV> counterClockwise(VectorYNode<IV> node) {
        if (size() == 0) throwNotPartOfThisHullException(node);
        
        if (node.isLeft()) {
            if (node.prev() != null) return node.prev();
            else if (right.isEmpty()) return left.getMax();
            else return right.getMin();
            
        } else {
            if (node.next() != null) return node.next();
            else if (left.isEmpty()) return right.getMin();
            else return left.getMax();
        }
        
        
        /*
        Edge e = getBottomTopEdge();
        double ori = e.relOri(node.getVec());
        if (ori < 0) {
            if (node.prev() != null) return node.prev();
            else if (right.isEmpty()) return left.getMax();
            else return right.getMin();
            
        } else if (ori > 0) {
            if (node.next() != null) return node.next();
            else if (left.isEmpty()) return right.getMin();
            else return left.getMax();
            
        } else {
            VectorYNode<IV> rtn;
            if (node == top) {
                if (top == left.getMax()) {
                    if (left.size() == 1) rtn = right.getMin();
                    else rtn = node.prev();
                } else rtn = left.getMax();
                
            } else if (node == bottom) {
                if (bottom == right.getMin()) {
                    if (right.size() == 1) rtn = left.getMax();
                    else rtn = node.next();
                } else rtn = right.getMin();
                
            } else {
                throwNotPartOfThisHullException(node);
                return null;
            }
            
            if (rtn != null) return rtn;
            // Handle cases where either of the two sides is empty.
            if (left.size() == 0 ^ right.size() == 0) {
                if (node == top) return bottom;
                else return top;
                
            } else {
                throwNotPartOfThisHullException(node);
                return null;
            }
        }*/
    }
    
    /**
     * @apiNote Runs in {@code O(log(n))}.
     * 
     * @return A random edge from the hull.
     */
    public VectorYEdge<IV> getRandomEdge() {
        VectorYNode<IV> node = getNode(Var.RAN.nextInt(size()));
        return new VectorYEdge<IV>(node, clockwise(node));
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
    public IV get(int i) {
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
    public VectorYNode<IV> getNode(int i) {
        if (i < 0 || i >= size()) throw new IndexOutOfBoundsException(i);
        if (i < left.size()) return left.get(i);
        else return right.get(right.size() - (i - left.size() + 1));
    }
    
    /**
     * Adds a vertex to the hull.
     *
     * @apiNote Runs in {@code O(log(n))}.
     * 
     * @param iv The vertex to add.
     * 
     * @return {@code true} if the vertex was added. {@code false} otherwise.
     */
    @Override
    public boolean add(IV iv) {
        return add(new VectorYNode<>(iv, this, true));
    }
    
    /**
     * Adds an input vertex and updates the hull accordingly by removing vertices from the hull. <br>
     * If the just added point was inside the hull, then it will be removed directly and returned in
     * the returned list. This is the only way for the given vertex to be removed. <br>
     * Moreover, if the given vertex is removed, then no other vertices will be removed.
     *
     * @apiNote Runs in {@code O(log(n) + k)}, where {@code k} denotes the number of removed elements.
     * 
     * @param iv The input vertex to add.
     * 
     * @return A list containing all removed vertices.
     */
    public List<IV> addAndUpdate(IV iv) {
        VectorYNode<IV> vyn = new VectorYNode<>(iv, this, true);
        List<IV> rem = new ArrayList<>();
        if (!add(vyn)) {
            rem.add(iv);
            return rem;
        }
        if (size() <= 3) return rem;
        {
            VectorYNode<IV> prev = counterClockwise(vyn);
            VectorYNode<IV> next = clockwise(vyn);
            Edge e = new Edge(prev.getVec(), next.getVec());
            if (e.relOri(vyn.getVec()) > 0) {
                remove(vyn);
                rem.add(vyn.getIv());
                return rem;
            }
        }

        {
            VectorYNode<IV> prev = vyn;
            while (size() > 3) {
                VectorYNode<IV> cur = clockwise(prev);
                VectorYNode<IV> next = clockwise(cur);
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
            VectorYNode<IV> next = vyn;
            while (size() > 3) {
                VectorYNode<IV> cur = counterClockwise(next);
                VectorYNode<IV> prev = counterClockwise(cur);
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
    }
    
    /**
     * Inserts a node in the chain.
     *
     * @apiNote Runs in {@code O(log(n))}.
     * 
     * @param vyn The node to add.
     * 
     * @return {@code true} if the node was added. {@code false} otherwise.
     */
    private boolean add(VectorYNode<IV> vyn) {
        vyn.setHull(this);
        if (size() == 0) {
            vyn.setLeft(true);
            return left.add(top = bottom = maxX = minX = vyn);
        }
        
        boolean newVerticalBound = false;
        if (vyn.getVec().y() > top.getVec().y()) {
            if (top == left.getMax()) {
                Edge e = new Edge(bottom.getVec(), vyn.getVec());
                if (e.relOri(top.getVec()) > 0) {
                    left.remove(top);
                    right.add(top);
                    top.setLeft(false);
                }
                
            } else {
                Edge e = new Edge(bottom.getVec(), vyn.getVec());
                if (e.relOri(top.getVec()) < 0) {
                    right.remove(top);
                    left.add(top);
                    top.setLeft(true);
                }
            }
            newVerticalBound = true;
            top = vyn;
            
        } else if (vyn.getVec().y() < bottom.getVec().y()) {
            if (bottom == left.getMin()) {
                Edge e = new Edge(vyn.getVec(), top.getVec());
                if (e.relOri(bottom.getVec()) > 0) {
                    left.remove(bottom);
                    right.add(bottom);
                    bottom.setLeft(false);
                }
                
            } else {
                Edge e = new Edge(vyn.getVec(), top.getVec());
                if (e.relOri(bottom.getVec()) < 0) {
                    right.remove(bottom);
                    left.add(bottom);
                    bottom.setLeft(true);
                }
            }
            newVerticalBound = true;
            bottom = vyn;
        }
        
        if (vyn.getVec().x() > maxX.getVec().x()) {
            maxX = vyn;
            
        } else if (vyn.getVec().x() < minX.getVec().x()) {
            minX = vyn;
        }
        
        if (newVerticalBound) {
            if (left.size() > right.size()) {
                vyn.setLeft(false);
                return right.add(vyn);
                
            } else {
                vyn.setLeft(true);
                return left.add(vyn);
            }
            
        } else {
            Edge e = getBottomTopEdge();
            if (e.relOri(vyn.getVec()) < 0) {
                vyn.setLeft(true);
                return left.add(vyn);
                
            } else {
                vyn.setLeft(false);
                return right.add(vyn);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @apiNote Runs in {@code O(log(n))}.
     * 
     * @apiNote This function runs in {@code O(log(n))}.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object obj) {
        if (obj instanceof BaseInputVertex) return remove((IV) obj);
        if (obj instanceof VectorYNode) return remove((VectorYNode<IV>) obj);
        return false;
    }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote Runs in {@code O(k*log(n))}.
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
     * @apiNote Runs in {@code O(k*log(n))}.
     *
     * @apiNote This function runs in {@code O(log(n) + k)}, where {@code k} denotes the size of the collection.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends IV> col) {
        return addAll((Iterable<IV>) col);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @apiNote Runs in {@code O(k*log(n))}.
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

    /**
     * {@inheritDoc}
     * 
     * @apiNote Runs in {@code O(k*log(n))}, where {@code k} denotes the minimum of:
     *     <ul>
     *         <li>The number of items in {@code col} which also occur in the hull.</li>
     *         <li>The number of items in the hull which do not occur in {@code col}.</li>
     *     </ul>
     */
    @Override
    public boolean retainAll(Collection<?> col) {
        boolean mod = left.retainAll(col);
        if (right.retainAll(col)) mod = true;
        if (!mod) return false;
        
        // Edge case.
        if (left.isEmpty() && right.isEmpty()) {
            top = bottom = minX = maxX = null;
        }
        
        // Update top/bottom.
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
        
        // Update min/max x.
        // For a small size, simply iterate over all elements in the hull to avoid special cases.
        if (size() < 10) {
            minX = maxX = null;
            for (VectorYNode<IV> vyn : left) {
                if (minX == null) minX = maxX = vyn;
                else if (vyn.getVec().x() < minX.getVec().x()) minX = vyn;
                else if (vyn.getVec().x() > maxX.getVec().x()) maxX = vyn;
            }
            for (VectorYNode<IV> vyn : right) {
                if (minX == null) minX = maxX = vyn;
                else if (vyn.getVec().x() < minX.getVec().x()) minX = vyn;
                else if (vyn.getVec().x() > maxX.getVec().x()) maxX = vyn;
            }
            return true;
        }
        updateMinMaxX();
        
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
     * @apiNote Runs in {@code O(k*log(n))}.
     * 
     * @param ivs The vertices to add.
     * 
     * @return {@code true} if the data structure was modified. {@code false} otherwise.
     */
    public boolean addAll(Iterable<IV> ivs) {
        boolean mod = false;
        for (IV iv : ivs) {
            if (add(iv)) mod = true;
        }
        return mod;
    }
    
    /**
     * Removes the given vertex.
     *
     * @apiNote Runs in {@code O(log(n))}.
     * 
     * @param iv The vertex to be removed.
     * @return {@code true} if the vertex was removed. {@code false} otherwise.
     */
    public boolean remove(IV iv) {
        return remove(new VectorYNode<>(iv, this, true));
    }

    /**
     * Removes the given node.
     *
     * @apiNote Runs in {@code O(log(n))}.
     *
     * @param vyn The node to be removed.
     * @return {@code true} if the vertex was removed. {@code false} otherwise.
     */
    public boolean remove(VectorYNode<IV> vyn) {
        if (isEmpty()) return false;
        if (vyn.getHull() != this) throw new IllegalStateException();
        //Edge e = getBottomTopEdge();
        //double ori = e.relOri(vyn.getVec());
        
        boolean rem;
        if (vyn.isLeft()) {
            rem = left.remove(vyn);
            if (!rem) rem = right.remove(vyn);
            
        } else {
            rem = right.remove(vyn);
            if (!rem) rem = left.remove(vyn);
        }
        
        // Exit if not removed.
        if (!rem) return false;
        
        // Update top and bottom.
        if (isEmpty()) {
            // All nodes were removed, so safe to set all to {@code null} and exit.
            top = bottom = minX = maxX = null;
            vyn.setHull(null);
            return true;
            
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
        
        // Update minX and maxX
        if (vyn == minX) {
            VectorYNode<IV> next = next(vyn);
            VectorYNode<IV> prev = prev(vyn);
            minX = (next.getVec().x() < prev.getVec().x()
                    ? next
                    : prev
            );
        }
        if (vyn == maxX) {
            VectorYNode<IV> next = next(vyn);
            VectorYNode<IV> prev = prev(vyn);
            maxX = (next.getVec().x() > prev.getVec().x()
                    ? next
                    : prev
            );
        }
        
        vyn.setHull(null);
        return true;
        
        /*
        else {
            if (!left.remove(vyn) && !right.remove(vyn)) return false;
            // Update top and bottom.
            if (isEmpty()) {
                // All nodes were removed, so safe to set all to {@code null} and exit.
                top = bottom = minX = maxX = null;
                return true;
                
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
        }
        
        // Update minX and maxX
        if (vyn == minX) {
            VectorYNode<IV> next = next(vyn);
            VectorYNode<IV> prev = prev(vyn);
            minX = (next.getVec().x() < prev.getVec().x()
                    ? next
                    : prev
            );
        }
        if (vyn == maxX) {
            VectorYNode<IV> next = next(vyn);
            VectorYNode<IV> prev = prev(vyn);
            maxX = (next.getVec().x() > prev.getVec().x()
                    ? next
                    : prev
            );
        }
        return true;*/
    }
    
    /**
     * Removes all vertices from the list.
     * 
     * @apiNote Runs in {@code O(k*log(n))}.
     *
     * @param ivs The vertices to remove.
     *
     * @return {@code true} if the data structure was modified. {@code false} otherwise.
     */
    public boolean removeAll(Iterable<IV> ivs) {
        boolean mod = false;
        for (IV iv : ivs) {
            if (remove(iv)) mod = true;
        }
        return mod;
    }
    
    /**
     * Removes all nodes from the list.
     *
     * @apiNote Runs in {@code O(k*log(n))}.
     *
     * @param vyns The nodes to remove.
     *
     * @return {@code true} if the data structure was modified. {@code false} otherwise.
     */
    public boolean removeAllNodes(Iterable<IV> vyns) {
        boolean mod = false;
        for (IV vyn : vyns) {
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
     * @apiNote Runs in {@code O(log(n))}.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object obj) {
        if (isEmpty()) return false;
        VectorYNode<IV> node;
        boolean created = false;
        if (obj instanceof BaseInputVertex) {
            node = new VectorYNode<>((IV) obj, this, true);
            created = true;
        } else if (obj instanceof VectorYNode) node = (VectorYNode<IV>) obj;
        else return false;
        
        Edge e = getBottomTopEdge();
        double ori = e.relOri(node.getVec());
        if (ori <= 0) {
            if (created) node.setLeft(true);
            if (left.contains(node)) return true;
        }
        if (ori >= 0) {
            if (created) node.setLeft(false);
            return right.contains(node);
        }
        return false;
    }
    
    /**
     * Converts the given {@link VectorYNode} Iterator to an {@link BaseInputVertex} iterator.
     * 
     * @param it The iterator to convert.
     * 
     * @return A converted iterator using the given iterator as underlying stream.
     */
    public static <IV extends BaseInputVertex> Iterator<IV> convert(Iterator<VectorYNode<IV>> it) {
        return new FunctionIterator<>(it, VectorYNode::getIv);
    }
    
    /**
     * @return An iterator over all nodes in the hull. The nodes are returned in order. 
     */
    @SuppressWarnings("unchecked")
    public Iterator<VectorYNode<IV>> nodeIterator() {
        return new MultiIterator<>(
                left.iterator(),
                new InverseListIterator<>(right.listIterator(false))
        );
    }
    
    @Override
    public Iterator<IV> iterator() {
        return convert(nodeIterator());
    }
    
    /**
     * @return An iterator over the left list.
     */
    public Iterator<IV> leftIterator() {
        return convert(left.iterator());
    }
    
    /**
     * @return An iterator over the left list.
     */
    public Iterator<IV> rightIterator() {
        return convert(right.iterator());
    }
    
    /**
     * Returns the right list. <br>
     * <b>WARNING</b> <br>
     * Modifying this list <b>will</b> modify the underlying data structure.
     * 
     * @return The right list.
     */
    public Iterable<VectorYNode<IV>> getLeft() {
        return left;
    }
    
    /**
     * Returns the left list. <br>
     * <b>WARNING</b> <br>
     * Modifying this list <b>will</b> modify the underlying data structure.
     *
     * @return The left list.
     */
    public Iterable<VectorYNode<IV>> getRight() {
        return right;
    }
    
    /**
     * @return An iterable of the input vertices of the left list.
     */
    public Iterable<IV> getLeftInput() {
        return () -> convert(left.iterator());
    }
    
    /**
     * @return An iterable of the input vertices of the right list.
     */
    public Iterable<IV> getRightInput() {
        return () -> convert(right.iterator());
    }
    
    @Override
    public Object[] toArray() {
        return toArray(new BaseInputVertex[size()]);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] arr) {
        int i = 0;
        for (IV iv : this) {
            if (i >= arr.length) break;
            arr[i++] = (T) iv;
        }
        return arr;
    }

    /**
     * @apiNote Runs in {@code O(1)}.
     * 
     * @return The top element, or {@code null} if the hull is empty.
     */
    public IV getTop() {
        return (top == null ? null : top.getIv());
    }

    /**
     * @apiNote Runs in {@code O(1)}.
     * 
     * @return The bottom element, or {@code null} if the hull is empty.
     */
    public IV getBottom() {
        return (bottom == null ? null : bottom.getIv());
    }
    
    /**
     * @apiNote Runs in {@code O(1)}.
     * 
     * @return The left most element, {@code null} if the hull is empty.
     */
    public IV getMinX() {
        return (minX == null ? null : minX.getIv());
    }
    
    /**
     * @apiNote Runs in {@code O(1)}.
     * 
     * @return The right most element, {@code null} if the hull is empty.
     */
    public IV getMaxX() {
        return (maxX == null ? null : maxX.getIv());
    }

    /**
     * @apiNote Runs in {@code O(1)}.
     * 
     * @return An edge from the bottom to the top of the convex hull.
     */
    public Edge getBottomTopEdge() {
        return new Edge(bottom.getVec(), top.getVec());
    }

    /**
     * Determines the two/three edges needed to connect a single node inside this hull
     * such that angles are convex. <br>
     * The given vertex should be strictly inside this hull, i.e. it should not lie on the hull.
     * 
     * @apiNote Runs in {@code O(n} time. Expected running time is {@code O(size())}.
     *     This is a 2-approximation algorithm.
     * 
     * @param center The center vertex to find the connecting edges for.
     * 
     * @return A collection of all edges  
     */
    public Collection<OutputEdge> getInnerPointConnections(IV center) {
        if (isEmpty()) {
            throw new IllegalStateException();
        }
        
        Collection<OutputEdge> out = new HashSet<>();
        
        Iterator<IV> it = iterator();
        IV lastIv = get(size() - 1);
        out.add(new OutputEdge(center, lastIv));
        Edge e = new Edge(center.getV(), lastIv.getV());
        
        IV prev = null;
        while (it.hasNext()) {
            IV iv = it.next();
            double ori = e.relOri(iv.getV());
            if (ori <= 0) {
                if (prev == null) {
                    throw new IllegalStateException("Point lies outside the hull!");
                }
                out.add(new OutputEdge(center, prev));
                e = new Edge(center.getV(), prev.getV());
            }
            prev = iv;
        }
        
        double ori = e.relOri(lastIv.getV());
        if (ori <= 0) {
            if (prev == null) {
                throw new IllegalStateException("Point lies outside the hull!");
            }
            out.add(new OutputEdge(center, prev));
        }
        
        return out;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getCanonicalName());
        sb.append("[");
        sb.append(Var.LS);
        boolean first = true;
        for (BaseInputVertex iv : this) {
            if (first) first = false;
            else sb.append(",");
            sb.append("    ");
            sb.append(iv.toString());
            sb.append(Var.LS);
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Testing purposes
     * 
     * @param args
     */
    public static void main(String[] args) {/*
        Logger.setDefaultLogger(new StreamLogger(System.out));
        List<BaseInputVertex> left = new ArrayList<>();
        List<BaseInputVertex> right = new ArrayList<>();
        int amt = 20;
        left.add(new BaseInputVertex(0, 0, 0));
        for (int i = 1; i < amt; i++) {
            double a = 0.5*amt;
            double x = 4.0/amt*(-Math.pow(i - a, 2) + a*a);
            left.add(new BaseInputVertex(i, -x, 2*i));
            right.add(new BaseInputVertex(i + amt - 1, x, 2*i));
        }
        right.add(new BaseInputVertex(2*amt - 1, 0, 2*amt));
        
        
        left.add(new BaseInputVertex(98, -5, 45));
        right.add(new BaseInputVertex(99, 20, 10));
        
        List<BaseInputVertex> combi = new ArrayList<>(left);
        combi.addAll(right);
        ConvexHull<BaseInputVertex> ch = ConvexHull.createConvexHull(combi);
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
        
        NearIntersection<BaseInputVertex> ni = ch.getPointsNearLine(e, true);
        Logger.write(ni);
        vis.addEdge(List.of(
                new Edge(ni.n1.getVec(), ni.n2.getVec()),
                new Edge(ni.n2.getVec(), ni.n4.getVec()),
                new Edge(ni.n4.getVec(), ni.n3.getVec()),
                new Edge(ni.n3.getVec(), ni.n1.getVec())
        ));
        vis.redraw();
        
        vis.setData(List.of(ch));
        vis.redraw();
        vis.addData(List.of(ch.getLeftInput(), ch.getRightInput()));
        vis.redraw();
        Logger.write(ch.addAndUpdate(new BaseInputVertex(50, -21, 60)));
        vis.redraw();
        vis.addData(List.of(ch));
        vis.redraw();*/
    }
    
    
}
