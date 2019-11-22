
package convex_layers;

import lombok.Data;

/**
 * Item of a linked list that keeps track of its previous and next neighbours.
 * @param <T> Generic type of the item.
 */
@Data
public class DLLItem<T> {
    
    T item;
    DLLItem<T> prev;
    DLLItem<T> next;
    
    
}
