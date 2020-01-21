package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Abstract class of nodes in a quadTree
 * @param <T> Type used for the nodes in the quad tree
 */
@Data
@NoArgsConstructor
abstract class AbstractQuadNode<T extends Node2D<T>> {

    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    // Values that keep track of the maxima and minima of this QuadNode
    protected double maxX = Double.NEGATIVE_INFINITY;
    protected double maxY = Double.NEGATIVE_INFINITY;
    protected double minX = Double.POSITIVE_INFINITY;
    protected double minY = Double.POSITIVE_INFINITY;
    
    
    // Parent of this QuadNode
    protected AbstractQuadNode<T> parent;
    // Points which are contained within the range defined by the maxima and minima
    protected Set<T> points = new HashSet<>();
    // Depth of this QuadNode
    protected int depth;
    // Middle values of the X and Y maxima and minima
    protected double middleX;
    protected double middleY;
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    public AbstractQuadNode(double maxX, double maxY, double minX, double minY, int depth){
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
        this.depth = depth;
        middleX = ((maxX-minX)/2.0)+minX;
        middleY = ((maxY-minY)/2.0)+minY;
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * @return The which lie in this node.
     */
    protected Set<T> getPoints() {
        return points;
    }
    
    /**
     * @return The parent of this node.
     */
    protected AbstractQuadNode<T> getParent() {
        return parent;
    }
    
    /**
     * @return The depth of this node.
     */
    protected int getDepth() {
        return depth;
    }

    /**
     * The points of this node which lie inside the given range.
     * 
     * @param maxX The maximum x-coordinate of the range.
     * @param maxY The maximum y-coordinate of the range.
     * @param minX The minimum x-coordinate of the range.
     * @param minY The minimum y-coordinate of the range.
     * 
     * @return The points in the given range inside this node.
     */
    public abstract Collection<T> getRange(double maxX, double maxY, double minX, double minY);

    /**
     *
     * @param node item to be removed from the tree
     * @return a QuadNode with the {@code node} removed from its structure
     */
    public abstract AbstractQuadNode<T> remove(Node2D<T> node);
    
    
}
