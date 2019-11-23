
package convex_layers.linked_list;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * Item of a linked list that keeps track of its previous and next neighbours.
 * @param <T> Generic type of the item.
 */
@Getter
@AllArgsConstructor
public class DLLItem<T> {
    /** The item in the linked list. */
    private T item;
    /** The next item in the linked list. */
    private DLLItem<T> prev;
    /** The previous item in the linked list. */
    private DLLItem<T> next;

    /**
     * @param item The new contents of the item.
     */
    protected void setItem(T item) {
        this.item = item;
    }
    
    /**
     * @param prev The previous item in the list.
     */
    protected void setPrev(DLLItem<T> prev) {
        this.prev = prev;
    }

    /**
     * @param next The next item in the list.
     */
    protected void setNext(DLLItem<T> next) {
        this.next = next;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == item) return true;
        if (obj == null || item == null) return false;
        if (obj instanceof DLLItem) {
            T item = ((DLLItem<T>) obj).getItem();
            return Objects.deepEquals(item, this.item);
            
        } else {
            return Objects.deepEquals(obj, item);
        }
    }
    
    @Override
    public int hashCode() {
        return (item == null ? 0 : item.hashCode());
    }
    
    @Override
    public String toString() {
        return (item == null ? "null" : item.toString());
    }
    
    
    
}
