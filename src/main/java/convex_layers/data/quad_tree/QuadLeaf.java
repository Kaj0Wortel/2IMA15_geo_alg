package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;

import java.util.Collection;

class QuadLeaf<T extends Node2D<T>> extends AbstractQuadNode{

    QuadLeaf(Collection<T> col){
        if(col.size()>0){
            points.add(col.iterator().next());
        } else {
            points = null;
        }
    }
}
