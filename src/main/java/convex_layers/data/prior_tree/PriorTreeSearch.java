package convex_layers.data.prior_tree;

import convex_layers.data.Node2D;
import convex_layers.data.Range2DSearch;
import lombok.RequiredArgsConstructor;
import tools.data.Function;
import tools.data.collection.FunctionCollection;

import java.util.Collection;
import java.util.Iterator;

/**
 * Wrapper class of the {@link PriorTree} class which adds the full range search functionality.
 * 
 * @param <T> The type of the node.
 */
public class PriorTreeSearch<T extends Node2D<T>>
        implements Range2DSearch<T> {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The function used to invert the x-axis of a node. */
    private final Function<T, InvertedXNode2D<T>> invertFunction = InvertedXNode2D::new;
    /** The function used to revert the x-axis of a node. */
    private final Function<InvertedXNode2D<T>, T> revertFunction = (inv) -> inv.src;
    
    /** The left priority tree which has the left side unbounded. */
    private PriorTree<T> left;
    /** The right priority tree which has the right side unbounded. */
    private PriorTree<InvertedXNode2D<T>> right;
    
    
    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    /**
     * Wrapper class which inverts all operations related to the x-axis.
     * 
     * @param <S> The source type to wrap.
     */
    @RequiredArgsConstructor
    private static class InvertedXNode2D<S extends Node2D<S>>
            implements Node2D<InvertedXNode2D<S>> {
        /** The source node used to get the information from. */
        private final S src;
        
        @Override
        public double getX() {
            return -src.getX();
        }
        
        @Override
        public double getY() {
            return src.getY();
        }
        
        @Override
        public int compareToX(InvertedXNode2D<S> node) {
            return -src.compareToX(node.src);
        }
        
        @Override
        public int compareToY(InvertedXNode2D<S> node) {
            return src.compareToY(node.src);
        }
        
        @Override
        public int compareToXThenY(InvertedXNode2D<S> node) {
            return src.compareToXThenY(node.src);
        }
        
        @Override
        public int compareToYThenX(InvertedXNode2D<S> node) {
            return src.compareToYThenX(node.src);
        }
        
        
    }
    
    
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates an empty priority tree search tree.
     */
    public PriorTreeSearch() {
        left = new PriorTree<>();
        right = new PriorTree<>();
    }
    
    /**
     * Creates a new priority tree search with the given elements.
     *
     * @param col The initial elements of the quad tree.
     */
    public PriorTreeSearch(Collection<T> col) {
        left = new PriorTree<>(col);
        right = new PriorTree<>(new FunctionCollection<>(col, revertFunction, invertFunction));
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    public T get(Object obj) {
        return left.get(obj);
    }
    
    @Override
    public void init(Collection<T> col) {
        left.init(col);
        right.init(new FunctionCollection<>(col, revertFunction, invertFunction));
    }
    
    @Override
    public int size() {
        return left.size();
    }
    
    @Override
    public boolean contains(Object obj) {
        return left.contains(obj);
    }
    
    @Override
    public Iterator<T> iterator() {
        return left.iterator();
    }
    
    @Override
    public boolean add(T data) {
        boolean lAdd = left.add(data);
        boolean rAdd = right.add(invertFunction.run(data));
        if (lAdd ^ rAdd) throw new IllegalStateException();
        return lAdd;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object obj) {
        boolean lRem = left.remove(obj);
        boolean rRem = right.remove(invertFunction.run((T) obj));
        if (lRem ^ rRem) throw new IllegalStateException();
        return lRem;
    }
    
    @Override
    public void clear() {
        left.clear();
        right.clear();
    }
    
    @Override
    public Collection<T> getRangeUpRight(double xMin, double xMax, double yMin, double yMax) {
        return new FunctionCollection<>(right.getUnboundedRange(-xMax, yMin, yMax), invertFunction, revertFunction);
    }
    
    @Override
    public Collection<T> getRangeDownRight(double xMin, double xMax, double yMin, double yMax) {
        return new FunctionCollection<>(right.getUnboundedRange(-xMax, yMin, yMax), invertFunction, revertFunction);
    }
    
    @Override
    public Collection<T> getRangeUpLeft(double xMin, double xMax, double yMin, double yMax) {
        return left.getUnboundedRange(xMax, yMin, yMax);
    }
    
    @Override
    public Collection<T> getRangeDownLeft(double xMin, double xMax, double yMin, double yMax) {
        return left.getUnboundedRange(xMax, yMin, yMax);
    }
    
    
}
