package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class QuadNode<T extends Node2D<T>> extends AbstractQuadNode<T>{
    private AbstractQuadNode<T> NE;
    private AbstractQuadNode<T> NW;
    private AbstractQuadNode<T> SE;
    private AbstractQuadNode<T> SW;



    QuadNode(double maxX, double maxY, double minX, double minY, int depth, AbstractQuadNode<T> parent,Collection<T> col) {
        super(maxX, maxY, minX, minY, depth);
        if(col instanceof HashSet){
            points = ((Set<T>) col);
        } else {
            for(T p : col){
                points.add(p);
            }
        }
        this.parent = parent;

        System.out.println("Some node: " + points.toString());
        System.out.println(depth);
        buildTree(col);
    }


    QuadNode(int depth,AbstractQuadNode<T> parent, Collection<T> col){
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
            points.add(p);
        }
        this.depth = depth;
        System.out.println("Some node: " + points.toString());
        System.out.println(depth);
        buildTree(col);
    }

    @SuppressWarnings("Duplicates")
    void buildTree(Collection<T> col){
        Set<T> northEast = new HashSet<>();
        Set<T> northWest = new HashSet<>();
        Set<T> southEast = new HashSet<>();
        Set<T> southWest = new HashSet<>();

        for(T p : col){
            if(p.getX()>((maxX-minX)/2)){
                if(p.getY() >((maxY-minY)/2)){
                    northEast.add(p);
                } else {
                    southEast.add(p);
                }
            }else{
                if(p.getY() >((maxY-minY)/2)){
                    northWest.add(p);
                } else {
                    southWest.add(p);
                }
            }
        }


        //construct leaves of this QuadNode
        if(northEast.size() > 1){
            NE = new QuadNode<T>(maxX, maxY, minX/2, minY/2, depth+1,this, northEast);
        } else {
            NE = new QuadLeaf<T>(depth+1,this, northEast);
        }

        if(northWest.size() > 1){
            NW = new QuadNode<T>(maxX/2, maxY, minX, minY/2, depth+1,this,northWest);
        } else {
            NW = new QuadLeaf<T>(depth+1,this,northWest);
        }

        if(southEast.size() > 1){
            SE = new QuadNode<T>(maxX, maxY/2, minX/2, minY, depth+1,this,southEast);
        } else {
            SE = new QuadLeaf<T>(depth+1,this,southEast);
        }

        if(southWest.size() > 1){
            SW = new QuadNode<T>(maxX/2, maxY/2, minX, minY, depth+1,this,southWest);
        } else {
            SW = new QuadLeaf<T>(depth+1,this,southWest);
        }
    }

    public Set<T> GetPoints(){
        return points;
    }
}
