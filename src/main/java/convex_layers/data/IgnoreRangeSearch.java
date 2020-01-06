package convex_layers.data;

import convex_layers.BaseInputVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * Implementation of the {@link Range2DSearch} interface which doesn't respect the given contract.
 * Instead of returning the values in the given range, it will return every value in the collection.
 * 
 * @param <T> The type of the nodes.
 */
public class IgnoreRangeSearch<T extends Node2D<T>>
        implements Base2DTree<T>, Range2DSearch<T> {
    
    private Collection<T> col;
    
    @Override
    public void init(Collection<T> col) {
        this.col = new ArrayList<>(col);
    }
    
    @Override
    public T get(Object obj) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int size() {
        return col.size();
    }
    
    @Override
    public boolean contains(Object obj) {
        return col.contains(obj);
    }
    
    @Override
    public Iterator<T> iterator() {
        return col.iterator();
    }
    
    @Override
    public boolean add(T v) {
        return col.add(v);
    }
    
    @Override
    public boolean remove(Object obj) {
        return col.remove(obj);
    }
    
    @Override
    public void clear() {
        col.clear();
    }
    
    @Override
    public Collection<T> getRangeUpRight(double xMin, double xMax, double yMin, double yMax) {
        return col;
    }
    
    @Override
    public Collection<T> getRangeDownRight(double xMin, double xMax, double yMin, double yMax) {
        return col;
    }
    
    @Override
    public Collection<T> getRangeUpLeft(double xMin, double xMax, double yMin, double yMax) {
        return col;
    }
    
    @Override
    public Collection<T> getRangeDownLeft(double xMin, double xMax, double yMin, double yMax) {
        return col;
    }
    
    
}
