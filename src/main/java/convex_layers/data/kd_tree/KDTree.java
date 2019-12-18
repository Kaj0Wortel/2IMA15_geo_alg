package convex_layers.data.kd_tree;

import convex_layers.data.Base2DTree;
import convex_layers.data.Node2D;
import convex_layers.data.Range2DSearch;

import java.util.Collection;
import java.util.Iterator;

/**
 * KD tree data structure for 2D.
 *
 * @param <T> The type of the elements.
 */
public class KDTree<T extends Node2D<T>>
        implements Range2DSearch<T> {// TODO: Change to Base2DTree if the range search cannot be performed directly.
    
    /* ----------------------------------------------------------------------
     * Variables
     * ----------------------------------------------------------------------
     */
    private int size; // TODO
    
    
    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates an empty KD tree.
     */
    public KDTree() {
        size = 0;
    }

    /**
     * Creates a new KD tree containing the given elements.
     *
     * @param col The initial elements of the quad tree.
     */
    public KDTree(Collection<T> col) {
        clear();
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
