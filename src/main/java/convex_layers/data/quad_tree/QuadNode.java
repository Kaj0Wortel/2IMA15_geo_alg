package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class QuadNode<T extends Node2D<T>> extends AbstractQuadNode<T>{

    @Getter private AbstractQuadNode<T> NE;
    @Getter private AbstractQuadNode<T> NW;
    @Getter private AbstractQuadNode<T> SE;
    @Getter private AbstractQuadNode<T> SW;



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
        System.out.println("maxes " + this.depth + " :" + this.maxX + " " + this.maxY + " " + this.minX + " "+  this.minY);
        buildTree(col);
    }


    QuadNode(int depth, AbstractQuadNode<T> parent, Collection<T> col){
        super();
//        System.out.println("maxes:" + this.maxX + " " + this.maxY + " " + this.minX + " "+  this.minY);
        for(T p : col){
            System.out.println(p.toString());
            if(p.getX()>this.maxX){
                this.maxX = p.getX();
            }
            if(p.getY()> this.maxY){
                this.maxY = p.getY();
            }
            if(p.getX() < this.minX){
                this.minX = p.getX();
            }
            if(p.getY() < this.minY){
                this.minY = p.getY();
            }
            points.add(p);
            System.out.println("maxes:" + this.maxX + " " + this.maxY + " " + this.minX + " "+  this.minY);
        }
        middleX = (maxX-minX)/2.0 + minX;
        middleY = (maxY-minY)/2.0 + minY;
        this.depth = depth;
        this.parent = parent;
        buildTree(col);
    }

    @SuppressWarnings("Duplicates")
    void buildTree(Collection<T> col){
        //Sets used for construction of the leaves of this QuadNode
        Set<T> northEast = new HashSet<>();
        Set<T> northWest = new HashSet<>();
        Set<T> southEast = new HashSet<>();
        Set<T> southWest = new HashSet<>();

        //divide points amongst the sets defined above
        for(T p : col){
            if(p.getX()>(minX+(maxX-minX)/2.0)){
                if(p.getY() >(minY+(maxY-minY)/2.0)){
                    northEast.add(p);
                } else {
                    southEast.add(p);
                }
            }else{
                if(p.getY() >(minY+(maxY-minY)/2.0)){
                    northWest.add(p);
                } else {
                    southWest.add(p);
                }
            }
        }
        //construct leaves of this QuadNode
        if(northEast.size() > 1){
            NE = new QuadNode<T>(maxX, maxY, middleX, middleY, depth+1,this, northEast);
        } else {
            NE = new QuadLeaf<T>(maxX, maxY, middleX, middleY, depth+1,this, northEast);
        }

        if(northWest.size() > 1){
            NW = new QuadNode<T>(middleX, maxY, minX, middleY, depth+1,this,northWest);
        } else {
            NW = new QuadLeaf<T>(middleX, maxY, minX, middleY, depth+1,this,northWest);
        }

        if(southEast.size() > 1){
            SE = new QuadNode<T>(maxX, middleY, middleX, minY, depth+1,this,southEast);
        } else {
            SE = new QuadLeaf<T>(maxX, middleY, middleX, minY, depth+1,this,southEast);
        }

        if(southWest.size() > 1){
            SW = new QuadNode<T>(middleX, middleY, minX, minY, depth+1,this,southWest);
        } else {
            SW = new QuadLeaf<T>(middleX, middleY, minX, minY, depth+1,this,southWest);
        }
    }

    public Set<T> GetPoints(){
        return points;
    }

    @Override
    T get(Node2D p) {
        T result;
        if(p.getX() > middleX){
            if(p.getY() > middleY){
                result = NE.get(p);
            } else {
                result = SE.get(p);
            }
        } else {
            if(p.getY() > middleY){
                result = NW.get(p);
            } else {
                result = SW.get(p);
            }
        }
        return result;
    }

    @Override
    Collection<T> getRange(double maxX, double maxY, double minX, double minY) {
        Set<T> result = new HashSet<T>();
        if(maxX >= this.maxX && maxY>=this.maxY && minX <= this.minX && minY <= this.minY){
            System.out.println("this maxX: " + this.maxX  + " maxX: " + maxX);
            System.out.println("this minX: " + this.minX  + " minX: " + minX);
            System.out.println("this maxY: " + this.maxY  + " maxY: " + maxY);
            System.out.println("this minY: " + this.minY  + " minY: " + minY);
            System.out.println("depth: " + depth);
            System.out.println("points: " + points);
            result.addAll(points);
        } else if(minX > this.maxX || maxX < this.minX || minY > this.maxY || maxY < this.minY){
            System.out.println("looool");
            System.out.println(points);
        } else {
            System.out.println("did recusions");
            System.out.println("NE: " + NE.minX + " " + NE.maxX + " " + NE.minX + " " + NE.minY);
            result.addAll(NE.getRange(maxX, maxY, minX, minY));
            System.out.println("NW: " + NW.minX + " " + NW.maxX + " " + NW.minX + " " + NW.minY);
            result.addAll(NW.getRange(maxX, maxY, minX, minY));
            System.out.println("SE: " + SE.minX + " " + SE.maxX + " " + SE.minX + " " + SE.minY);
            result.addAll(SE.getRange(maxX, maxY, minX, minY));
            System.out.println("SW: " + SW.minX + " " + SW.maxX + " " + SW.minX + " " + SW.minY);
            result.addAll(SW.getRange(maxX, maxY, minX, minY));

        }
        System.out.println("results: " + result.toString());
        return result;
    }
}
