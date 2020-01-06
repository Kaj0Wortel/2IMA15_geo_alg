package convex_layers.data.quad_tree;

import convex_layers.BaseInputVertex;
import convex_layers.data.Node2D;
import convex_layers.data.Range2DSearch;
import convex_layers.data.prior_tree.PriorTreeSearch;
import tools.log.Logger;
import tools.log.StreamLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Quad tree data structure.
 * 
 * @param <T> The type of the elements.
 */
public class QuadTree<T extends Node2D<T>>
        implements Range2DSearch<T> { // TODO: Change to Base2DTree if the range search cannot be performed directly.
    
    /* ----------------------------------------------------------------------
     * Variables
     * ----------------------------------------------------------------------
     */
    private int size;

    //The quads defined by the current quad
    private AbstractQuadNode<T> root;

    
    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates an empty quad tree.
     */
    public QuadTree() {
        size = 0;
    }
    
    /**
     * Creates a new quad tree containing the given elements.
     * 
     * @param col The initial elements of the quad tree.
     */
    public QuadTree(Collection<T> col) {
        init(col);
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    public void init(Collection<T> col) {
        clear();
        size = col.size();
        if (col.size() > 1) {
            root = new QuadNode(0,null, col);
        } else {
            root = new QuadLeaf(0,0,0,0,0,null, col);//TODO get rid of zeroes
        }
    }
    
    @Override
    public T get(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object obj) {
        return root.getPoints().contains(obj);
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException(); // TODO
    }

    //TODO finish method
    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Node2D)){
            throw new IllegalArgumentException("Object provided should be of type Node2D");
        } else {
            size -= 1;
            root.remove((Node2D) o);
            return true;
        }
    }

    @Override
    public void clear() {
        size = 0;
        root = null;
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public Collection<T> getRangeUpRight(double xMin, double xMax, double yMin, double yMax) {
        return getRange(xMin, xMax, yMin, yMax);
    }
    
    @Override
    public Collection<T> getRangeDownRight(double xMin, double xMax, double yMin, double yMax) {
        return getRange(xMin, xMax, yMin, yMax);
    }
    
    @Override
    public Collection<T> getRangeUpLeft(double xMin, double xMax, double yMin, double yMax) {
        return getRange(xMin, xMax, yMin, yMax);
    }
    
    @Override
    public Collection<T> getRangeDownLeft(double xMin, double xMax, double yMin, double yMax) {
        return getRange(xMin, xMax, yMin, yMax);
    }

    //TODO
    public Collection<T> getRange(double xMin, double xMax, double yMin, double yMax){
        return root.getRange(xMax, yMax, xMin, yMin);
    }

    @SuppressWarnings("Duplicates")
    public static void main(String args[]){
        Logger.setDefaultLogger(new StreamLogger(System.out));
        ArrayList<BaseInputVertex> pts = new ArrayList<>(List.of(
                new BaseInputVertex(0, 1, 1),
                new BaseInputVertex(0, 2, 2),
                new BaseInputVertex(0, 3, 3),
                new BaseInputVertex(0, 4, 4),
                new BaseInputVertex(0, 5, 5),
                new BaseInputVertex(0, 6, 6),
                new BaseInputVertex(0, 7, 7),
                new BaseInputVertex(0, 8, 8),
                new BaseInputVertex(0, 9, 9)
        ));
        QuadTree<BaseInputVertex> quad = new QuadTree<>(pts);
    }
    
}
