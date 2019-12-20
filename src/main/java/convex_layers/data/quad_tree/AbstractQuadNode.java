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
    protected Set<T> points = new HashSet<T>();

    AbstractQuadNode(double maxX, double maxY, double minX, double minY){
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
    }

    Set<T> getPoints(){
        return points;
    }
}
