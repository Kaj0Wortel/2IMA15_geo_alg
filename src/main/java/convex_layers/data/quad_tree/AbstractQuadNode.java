package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * abstract class of nodes in a quadTree
 * @param <T> type used for the nodes in the quad tree
 */
@NoArgsConstructor
@Data
abstract class AbstractQuadNode<T extends Node2D<T>> {
    //variables used to maintain QuadTree structure

    //doubles that keep track of the maxima and minima of this QuadNode
    protected double maxX = Double.NEGATIVE_INFINITY;
    protected double maxY = Double.NEGATIVE_INFINITY;
    protected double minX = Double.POSITIVE_INFINITY;
    protected double minY = Double.POSITIVE_INFINITY;


    //parent of this QuadNode
    protected AbstractQuadNode<T> parent;
    //points which are contained within the range defined by the maxima and minima
    protected Set<T> points = new HashSet<>();
    //depth of this QuadNode
    protected int depth;
    //middle values of the X and Y maxima and minima
    protected double middleX;
    protected double middleY;

    AbstractQuadNode(double maxX, double maxY, double minX, double minY, int depth){
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
        this.depth = depth;
        middleX = ((maxX-minX)/2.0)+minX;
        middleY = ((maxY-minY)/2.0)+minY;
    }

    Set<T> getPoints(){
        return points;
    }

    AbstractQuadNode<T> getParent(){
        return parent;
    }

    /**
     * @return depth of this Node
     */
    int getDepth(){
        return depth;
    }

    abstract Collection<T> getRange(double maxX, double maxY, double minX, double minY);

    /**
     *
     * @param node item to be removed from the tree
     * @return a QuadNode with the {@code node} removed from its structure
     */
    abstract AbstractQuadNode remove(Node2D node);
}
