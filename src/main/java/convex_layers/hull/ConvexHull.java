package convex_layers.hull;

import convex_layers.InputVertex;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import tools.data.collection.rb_tree.LinkedRBTree;

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
    
    
    public NearIntersection getPointsNearLine(Vector v1, Vector v2) {
        if (v1.x() > v2.x()) {
            Vector tmp = v1;
            v1 = v2;
            v2 = v1;
        }
        // v1 lies to the left of v2.
        Edge e = new Edge(v1, v2);
        VectorYNode vyn1 = null, vyn2 = null, vyn3 = null, vyn4 = null;
        
        // TODO: edge cases:
        //  - edge goes between 3 top vertices.
        //  - edge goes between 3 bottom vertices.
        //  - edge goes through vertex.
        double relOriTop = e.relOri(top.getVec());
        double relOriBot = e.relOri(bottom.getVec());
        if (relOriTop * relOriBot < 0) {
            // Line goes through left and right side.
            vyn2 = getNodeAboveBothSides(left, e);
            vyn1 = vyn2.next();
            vyn3 = getNodeAboveBothSides(right, e);
            vyn4 = vyn3.prev();
            
        } else if (relOriTop == 0) {
            // TODO: edge case: edge goes through top.
            
        } else {
            // Line lies on the left/right side.
            LinkedRBTree<VectorYNode> tree = (relOriTop > 0
                    ? left
                    : right);
            vyn2 = getNodeAboveOneSide(tree, e, true);
            vyn1 = vyn2.next();
            vyn3 = getNodeAboveOneSide(tree, e, false);
            vyn4 = vyn3.prev();
        }
        
        return new NearIntersection(vyn1, vyn2, vyn3, vyn4);
    }
    
    private VectorYNode getNodeAboveBothSides(LinkedRBTree<VectorYNode> tree, Edge e) {
        VectorYNode node = left.getRoot();
        while (true) {
            double ori = e.relOri(node.getVec());
            if (ori < 0) {
                if (node.hasRight()) node = node.left();
                else return node;
                
            } else if (ori > 0) {
                if (node.hasLeft()) node = node.right();
                else return node.prev();
                
            } else {
                return node;
            }
        }
    }
    
    private VectorYNode getNodeAboveOneSide(LinkedRBTree<VectorYNode> tree, Edge e, boolean invert) {
        VectorYNode node = left.getRoot();
        double target = (invert
                ? Math.min(e.v1().y(), e.v2().y())
                : Math.max(e.v1().y(), e.v2().y()));
        while (true) {
            if (node.getVec().y() < target != invert) {
                if (invert) {
                    if (node.hasRight()) node = node.right();
                    else return node.next();
                    
                } else {
                    if (node.hasLeft()) node = node.left();
                    else return node.prev();
                }
                continue;
            }
            
            double ori = e.relOri(node.getVec());
            if (ori < 0) {
                if (node.hasRight()) node = (invert ? node.left() : node.right());
                else return (invert ? node : node.next());

            } else if (ori > 0) {
                if (node.hasLeft()) node = (invert ? node.right() : node.left());
                else return (invert ? node : node.prev());
                
            } else {
                return node;
            }
        }
    }
    
    
}
