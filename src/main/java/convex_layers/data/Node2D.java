package convex_layers.data;

/**
 * Interface describing 2D nodes used in the search structures.
 * 
 * @param <T> The type of the node.
 */
public interface Node2D<T extends Node2D<T>> {

    /**
     * @return The x-coordinate of the node.
     */
    double getX();

    /**
     * @return The y-coordinate of the node.
     */
    double getY();

    /**
     * Compares the x-coordinate of nodes.
     * 
     * @param node The node to compare with.
     * 
     * @return The difference between x-coordinate {@code this} and the given node.
     */
    default int compareToX(T node) {
        double diff = getX() - node.getX();
        if (diff < 0) return Math.min(-1, (int) diff);
        else if (diff > 0) return Math.max(1, (int) diff);
        else return 0;
    }
    
    /**
     * Compares the y-coordinate of the nodes
     *
     * @param node The node to compare with.
     *
     * @return The difference between y-coordinate {@code this} and the given node.
     */
    default int compareToY(T node) {
        double diff = getY() - node.getY();
        if (diff < 0) return Math.min(-1, (int) diff);
        else if (diff > 0) return Math.max(1, (int) diff);
        else return 0;
    }
    
    /**
     * Compares the x-coordinate of the nodes. If they are equal, then compare the y-coordinate.
     *
     * @param node The node to compare with.
     *
     * @return The difference between coordinates {@code this} and the given node,
     *     with priority for the x-coordinate.
     */
    default int compareToXThenY(T node) {
        int cmp = compareToY(node);
        if (cmp == 0) cmp = compareToX(node);
        return cmp;
    }
    
    /**
     * Compares the y-coordinate of the nodes. If they are equal, then compare the x-coordinate.
     *
     * @param node The node to compare with.
     *
     * @return The difference between coordinates {@code this} and the given node,
     *     with priority for the y-coordinate.
     */
    default int compareToYThenX(T node) {
        int cmp = compareToX(node);
        if (cmp == 0) cmp = compareToY(node);
        return cmp;
    }
    
    
}
