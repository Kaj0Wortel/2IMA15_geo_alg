package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of an {@link AbstractQuadNode} representing an internal node
 * in the quadtree structure.
 *
 * @param <T> The type of the data nodes.
 */
class QuadNode<T extends Node2D<T>>
        extends AbstractQuadNode<T>{

    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The north-eastern part of the node. */
    @Getter
    private AbstractQuadNode<T> NE;
    /** The north-western part of the node. */
    @Getter
    private AbstractQuadNode<T> NW;
    /** The south-eastern part of the node. */
    @Getter
    private AbstractQuadNode<T> SE;
    /** The south-western part of the node. */
    @Getter
    private AbstractQuadNode<T> SW;
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    public QuadNode(double maxX, double maxY, double minX, double minY, int depth, AbstractQuadNode<T> parent,Collection<T> col) {
        super(maxX, maxY, minX, minY, depth);
        if(col instanceof HashSet){
            points = ((Set<T>) col);
        } else {
            points.addAll(col);
        }

        this.parent = parent;
        buildTree(col);
    }


    public QuadNode(int depth, AbstractQuadNode<T> parent, Collection<T> col){
        for(T p : col){
            if (p.getX() > this.maxX) {
                this.maxX = p.getX();
            }
            if (p.getY()> this.maxY) {
                this.maxY = p.getY();
            }
            if (p.getX() < this.minX) {
                this.minX = p.getX();
            }
            if (p.getY() < this.minY) {
                this.minY = p.getY();
            }
            points.add(p);
        }
        middleX = (maxX-minX) / 2.0 + minX;
        middleY = (maxY-minY) / 2.0 + minY;
        this.depth = depth;
        this.parent = parent;
        buildTree(col);
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @SuppressWarnings("DuplicatedCode")
    private void buildTree(Collection<T> col){
        // Sets used for construction of the leaves of this QuadNode
        Set<T> northEast = new HashSet<>();
        Set<T> northWest = new HashSet<>();
        Set<T> southEast = new HashSet<>();
        Set<T> southWest = new HashSet<>();

        // Divide points amongst the sets defined above
        for (T p : col){
            if (p.getX()>(minX+(maxX-minX)/2.0)) {
                if (p.getY() > (minY + (maxY - minY) / 2.0)) {
                    northEast.add(p);
                } else {
                    southEast.add(p);
                }
            } else {
                if(p.getY() > (minY + (maxY - minY) / 2.0)) {
                    northWest.add(p);
                } else {
                    southWest.add(p);
                }
            }
        }
        
        // Construct leaves of this QuadNode. If a node would contain more than 1 element,
        // make a QuadNode, otherwise make a QuadLeaf.
        if(northEast.size() > 1){
            NE = new QuadNode<>(maxX, maxY, middleX, middleY, depth+1,this, northEast);
        } else {
            NE = new QuadLeaf<>(maxX, maxY, middleX, middleY, depth+1,this, northEast);
        }

        if(northWest.size() > 1){
            NW = new QuadNode<>(middleX, maxY, minX, middleY, depth+1,this,northWest);
        } else {
            NW = new QuadLeaf<>(middleX, maxY, minX, middleY, depth+1,this,northWest);
        }

        if(southEast.size() > 1){
            SE = new QuadNode<>(maxX, middleY, middleX, minY, depth+1,this,southEast);
        } else {
            SE = new QuadLeaf<>(maxX, middleY, middleX, minY, depth+1,this,southEast);
        }

        if(southWest.size() > 1){
            SW = new QuadNode<>(middleX, middleY, minX, minY, depth+1,this,southWest);
        } else {
            SW = new QuadLeaf<>(middleX, middleY, minX, minY, depth+1,this,southWest);
        }
    }

    @Override
    public Collection<T> getRange(double maxX, double maxY, double minX, double minY) {
        Set<T> result = new HashSet<T>();
        if(maxX >= this.maxX && maxY>=this.maxY && minX <= this.minX && minY <= this.minY){
            result.addAll(points);
        } else if (minX > this.maxX || maxX < this.minX || minY > this.maxY || maxY < this.minY) {
        } else {
            result.addAll(NE.getRange(maxX, maxY, minX, minY));
            result.addAll(NW.getRange(maxX, maxY, minX, minY));
            result.addAll(SE.getRange(maxX, maxY, minX, minY));
            result.addAll(SW.getRange(maxX, maxY, minX, minY));
        }
        return result;
    }
    
    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public AbstractQuadNode<T> remove(Node2D<T> node) {
        points.remove(node);
        if (points.size() <= 1) {
            return new QuadLeaf<T>(maxX, maxY, minX, minY, depth, parent, points);
        }
        if (NE.points.contains(node)) {
            NE = NE.remove(node);
        } else if(NW.points.contains(node)) {
            NW = NW.remove(node);
        } else if(SE.points.contains(node)) {
            SE = SE.remove(node);
        } else {
            SW = SW.remove(node);
        }
        return this;
    }
    
    
}
