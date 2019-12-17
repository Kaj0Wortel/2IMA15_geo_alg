package convex_layers.prior_tree;

public interface PriorTreeNode<T extends PriorTreeNode<T>> {
    
    double getX();
    
    double getY();
    
    default int compareToX(T node) {
        double diff = getX() - node.getX();
        if (diff < 0) return Math.min(-1, (int) diff);
        else if (diff > 0) return Math.max(1, (int) diff);
        else return 0;
    }

    default int compareToY(T node) {
        double diff = getY() - node.getY();
        if (diff < 0) return Math.min(-1, (int) diff);
        else if (diff > 0) return Math.max(1, (int) diff);
        else return 0;
    }
    
    
}
