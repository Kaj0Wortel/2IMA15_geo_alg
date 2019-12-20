package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;

import java.util.Collection;
import java.util.Set;

class QuadLeaf<T extends Node2D<T>> extends AbstractQuadNode{

    QuadLeaf(int depth, AbstractQuadNode<T> parent,Collection<T> col){
        if(col.size()>0){
            points.add(col.iterator().next());
        } else {
            points = null;
        }
        this.parent = parent;
        this.depth = depth;
    }

    Set<T> getPoints(){
        return points;
    }
}
