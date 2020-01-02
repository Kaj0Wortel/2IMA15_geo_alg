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
    T get(Node2D obj) {
        return null;
    }

    @Override
    Collection<T> getRange(double maxX, double maxY, double minX, double minY) {
        Set<T> result = new HashSet<T>();
        if (minX > this.maxX || maxX <= this.minX || minY > this.maxY || maxY <= this.minY) {
            System.out.println("looool");
            System.out.println(points);
        } else {
            return points;

        }
        System.out.println("results: " + result.toString());
        return result;
    }
}
