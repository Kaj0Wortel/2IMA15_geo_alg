package convex_layers.data;

import java.util.Collection;

/**
 * Interface used for search queries.
 * 
 * @param <T> The data type of the search query.
 */
public interface Range2DSearch<T extends Node2D<T>>
        extends Base2DTree<T> {
    
    /**
     * Returns all elements in the given range. The maximum x and maximum y
     * values are unbounded. Points which lie in the unbounded part might or
     * might not be returned.
     * 
     * @param xMin The minimum x-coordinate of the range.
     * @param xMax The maximum x-coordinate of the range (can be ignored).
     * @param yMin The minimum y-coordinate of the range.
     * @param yMax The maximum x-coordinate of the range (can be ignored).
     */
    Collection<T> getRangeUpRight(double xMin, double xMax, double yMin, double yMax);
    
    /**
     * Returns all elements in the given range. The maximum x and minimum y
     * values are unbounded. Points which lie in the unbounded part might or
     * might not be returned.
     *
     * @param xMin The minimum x-coordinate of the range.
     * @param xMax The maximum x-coordinate of the range (can be ignored).
     * @param yMin The minimum y-coordinate of the range (can be ignored).
     * @param yMax The maximum x-coordinate of the range.
     */
    Collection<T> getRangeDownRight(double xMin, double xMax, double yMin, double yMax);

    /**
     * Returns all elements in the given range. The minimum x and maximum y
     * values are unbounded. Points which lie in the unbounded part might or
     * might not be returned.
     *
     * @param xMin The minimum x-coordinate of the range (can be ignored).
     * @param xMax The maximum x-coordinate of the range.
     * @param yMin The minimum y-coordinate of the range.
     * @param yMax The maximum x-coordinate of the range (can be ignored).
     */
    Collection<T> getRangeUpLeft(double xMin, double xMax, double yMin, double yMax);

    /**
     * Returns all elements in the given range. The minimum x and minimum y
     * values are unbounded. Points which lie in the unbounded part might or
     * might not be returned.
     * 
     * @param xMin The minimum x-coordinate of the range (can be ignored).
     * @param xMax The maximum x-coordinate of the range.
     * @param yMin The minimum y-coordinate of the range (can be ignored).
     * @param yMax The maximum x-coordinate of the range.
     */
    Collection<T> getRangeDownLeft(double xMin, double xMax, double yMin, double yMax);

    /**
     * Returns all elements in the given range. The values of {@code unboundedLeft}
     * and {@code unboundedRight} denote which two sides are unbounded.
     *
     * @param xMin The minimum x-coordinate of the range (can be ignored).
     * @param xMax The maximum x-coordinate of the range.
     * @param yMin The minimum y-coordinate of the range (can be ignored).
     * @param yMax The maximum x-coordinate of the range.
     * @param unboundedLeft   Whether the left or the right side of the range is unbounded.
     * @param unboundedBottom Whether the bottom or the top side of the range is unbounded.
     * 
     * @return A collection containing all points in the given range. Elements in the
     *     unbounded area might or might not be reported.
     */
    default Collection<T> getRange(double xMin, double xMax, double yMin, double yMax,
                                   boolean unboundedLeft, boolean unboundedBottom) {
        if (unboundedLeft) {
            if (unboundedBottom) {
                return getRangeDownLeft(xMin, xMax, yMin, yMax);
            } else {
                return getRangeUpLeft(xMin, xMax, yMin, yMax);
            }
            
        } else {
            if (unboundedBottom) {
                return getRangeDownRight(xMin, xMax, yMin, yMax);
            } else {
                return getRangeUpRight(xMin, xMax, yMin, yMax);
            }
        }
    }
    
    
}
