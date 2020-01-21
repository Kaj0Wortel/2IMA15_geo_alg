package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of an {@link AbstractQuadNode} representing a leaf
 * in the quadtree structure.
 * 
 * @param <T> The type of the data nodes.
 */
class QuadLeaf<T extends Node2D<T>>
        extends AbstractQuadNode<T> {
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    public QuadLeaf(double maxX, double maxY, double minX, double minY, int depth,
             AbstractQuadNode<T> parent, Collection<T> col) {
        super(maxX, maxY, minX, minY, depth);
        if (col.size() > 0) {
            points.add(col.iterator().next()); //get first and only element of the collection
        }
        this.parent = parent;
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    public Collection<T> getRange(double maxX, double maxY, double minX, double minY) {
        Set<T> result = new HashSet<T>();
        if (points.size() > 0) {
            if (minX > this.maxX || maxX <= this.minX || minY > this.maxY || maxY <= this.minY) {
            } else {
                T p = points.iterator().next();
                if (minX <= p.getX() && p.getX() <= maxX && minY <= p.getY() && p.getY() <= maxY) {
                    return points;
                }
            }
        }
        return result;
    }
    
    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public AbstractQuadNode<T> remove(Node2D<T> node) {
        if (!(points.contains(node))){
            throw new IllegalStateException("point not part of leaf");
        }
        points.remove(node);
        return this;
    }
    
    
}
