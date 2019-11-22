@Data
package convex_layers;

/**
 * Item of a linked list that keeps track of its previous and next neighbours.
 * @param <T> Generic type of the item.
 */
public class DLLItem<T> {
    T item;
    DLLItem<T> prev;
    DLLItem<T> next;
}
