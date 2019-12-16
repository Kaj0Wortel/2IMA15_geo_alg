package convex_layers.prior_tree;

public interface PriorTreeNode<T extends PriorTreeNode<T>> {
    
    int compareToX(T node);
    
    int compareToY(T node);
    
    
}
