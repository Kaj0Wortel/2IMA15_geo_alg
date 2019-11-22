
package convex_layers;

import lombok.Data;

/**
 * Linked list for items of a generic type {@code T}.
 * @param <T> Generic type used for th linked list
 */
@Data
public class DoublyLinkedList<T> {
    DLLItem<T> first;
    DLLItem<T> last;
}
