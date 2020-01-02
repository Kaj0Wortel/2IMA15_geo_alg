package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Data
abstract class AbstractQuadNode<T extends Node2D<T>> {
    protected double maxX = Double.NEGATIVE_INFINITY;
    protected double maxY = Double.NEGATIVE_INFINITY;
    protected double minX = Double.POSITIVE_INFINITY;
    protected double minY = Double.POSITIVE_INFINITY;
    protected AbstractQuadNode<T> parent;
    protected Set<T> points = new HashSet<>();
    protected int depth;
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

    int getDepth(){
        return depth;
    }

    abstract T get(Node2D obj);

    abstract Collection<T> getRange(double maxX, double maxY, double minX, double minY);
}
