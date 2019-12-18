package convex_layers.data;

import java.util.Collection;

/**
 * Abstract base class for the search trees.
 *
 * @param <T> The type of the elements.
 */
public interface Base2DTree<T extends Node2D<T>>
        extends Collection<T> {
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    default boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    default Object[] toArray() {
        return toArray(new Node2D[size()]);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    default <T1> T1[] toArray(T1[] arr) {
        int i = 0;
        for (T data : this) {
            arr[i++] = (T1) data;
        }
        return arr;
    }
    
    @Override
    default boolean containsAll(Collection<?> col) {
        for (Object obj : col) {
            if (!contains(obj)) return false;
        }
        return true;
    }
    
    @Override
    default boolean addAll(Collection<? extends T> col) {
        boolean mod = false;
        for (T data : col) {
            if (add(data)) mod = true;
        }
        return mod;
    }
    
    @Override
    default boolean removeAll(Collection<?> col) {
        boolean mod = false;
        for (Object obj : col) {
            if (remove(obj)) mod = true;
        }
        return mod;
    }
    
    @Override
    default boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Used to initialize the search structure.
     *
     * @param col The collection containing the elements.
     */
    void init(Collection<T> col);
    
    /**
     * @param obj The search key.
     * 
     * @return The value in the tree which corresponds with the given key.
     */
    T get(Object obj);
    
    
}
