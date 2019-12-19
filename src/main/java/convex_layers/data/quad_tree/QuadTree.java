package convex_layers.data.quad_tree;

import convex_layers.data.Node2D;
import convex_layers.data.Range2DSearch;

import java.util.Collection;
import java.util.Iterator;

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
    private QuadNode<T> root;

    
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


        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public T get(Object obj) {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public boolean contains(Object obj) {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void clear() {
        size = 0;
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public Collection<T> getRangeUpRight(double xMin, double xMax, double yMin, double yMax) {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public Collection<T> getRangeDownRight(double xMin, double xMax, double yMin, double yMax) {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public Collection<T> getRangeUpLeft(double xMin, double xMax, double yMin, double yMax) {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public Collection<T> getRangeDownLeft(double xMin, double xMax, double yMin, double yMax) {
        throw new UnsupportedOperationException(); // TODO
    }
    
    
}
