package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;
import java.util.Collection;

class QuadNode<T extends Node2D<T>> extends AbstractQuadNode<T>{

    private AbstractQuadNode<T> NE;
    private AbstractQuadNode<T> NW;
    private AbstractQuadNode<T> SE;
    private AbstractQuadNode<T> SW;

    QuadNode(double maxX, double maxY, double minX, double minY) {
        super(maxX, maxY, minX, minY);
    }

    QuadNode(Collection<T> col){
        super();
        for(T p : col){
            if(p.getX()>this.maxX){
                this.maxX = p.getX();
            }
            if(p.getY()> this.maxY){
                this.maxY = p.getX();
            }
            if(p.getX() < this.minX){
                this.minX = p.getX();
            }
            if(p.getY() < this.minY){
                this.minY = p.getY();
            }
        }
    }
}
