package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class QuadLeaf<T extends Node2D<T>> extends AbstractQuadNode {

    QuadLeaf(double maxX, double maxY, double minX, double minY, int depth, AbstractQuadNode<T> parent, Collection<T> col) {
        super(maxX, maxY, minX, minY, depth);
        if (col.size() > 0) {
            points.add(col.iterator().next()); //get first and only element of the collection
        }
        this.parent = parent;
    }

    Set<T> getPoints() {
        return points;
    }

    @Override
    Collection<T> getRange(double maxX, double maxY, double minX, double minY) {
        Set<T> result = new HashSet<T>();
        if (points.size() > 0) {
            if (minX > this.maxX || maxX <= this.minX || minY > this.maxY || maxY <= this.minY) {
            } else {
                Node2D p = (Node2D) points.iterator().next();
                if (minX <= p.getX() && p.getX() <= maxX && minY <= p.getY() && p.getY() <= maxY) {
                    return points;
                }
            }
        }
        return result;
    }

    @Override
    AbstractQuadNode remove(Node2D node) {
        if (!(points.contains(node))){
            throw new IllegalStateException("point not part of leaf");
        }
        points.remove(node);
        return this;
    }
}
