
package convex_layers.data.linked_list;

import tools.PublicCloneable;

import java.util.*;

/**
 * Linked list for items of a generic type {@code T}. <br>
 * This class is not thread safe.
 * 
 * @param <T> Generic type used for th linked list
 */
public class DoublyLinkedList<T>
        implements PublicCloneable, List<T>, Deque<T>, Iterable<T>, Collection<T>, Queue<T> {
    
    
    /* --------------------------------------------------------------------------------
     * Variables
     * --------------------------------------------------------------------------------
     */
    /** The first element in the list. */
    private DLLItem<T> first;
    /** The last element in the list. */
    private DLLItem<T> last;
    /** Denotes the size of the list. */
    private int size = 0;
    /** The number of modifications done to the list. */
    private long mod = 0;
    /** The cached item. */
    private DLLItem<T> cached = null;
    /** The index of the cached item. */
    private int cachedIndex = -1;
    /** The mod counter of the cached item. */
    private long cachedMod = 0;

    
    /* --------------------------------------------------------------------------------
     * Inner classes
     * --------------------------------------------------------------------------------
     */
    /**
     * ListIterator implementation for DDLItem elements. <br>
     * It uses a fail-fast mechanism to detect changes in the underlying linked list.
     */
    private class DoubleListElementIterator
            implements ListIterator<DLLItem<T>> {
        
        /** The next element. */
        private DLLItem<T> nextElem;
        /** The previous element. */
        private DLLItem<T> prevElem;
        /** The last returned element. */
        private DLLItem<T> returned = null;
        /** The current index. */
        private int i;
        /** The mod counter of the iterator. */
        private long itMod = mod;

        /**
         * Constructor.
         * Initializes the iterator at the given item with the given index.
         * 
         * @param elem The element to start at.
         * @param i    The index of the given element.
         */
        DoubleListElementIterator(DLLItem<T> elem, int i) {
            this.nextElem = elem;
            this.prevElem = (elem == null ? null : elem.getPrev());
            this.i = i;
        }
        
        @Override
        public boolean hasNext() {
            return nextElem != null;
        }
        
        @Override
        public DLLItem<T> next() {
            if (!hasNext()) throw new NoSuchElementException();
            if (mod != itMod) throw new ConcurrentModificationException();
            nextElem = (prevElem = nextElem).getNext();
            i++;
            return returned = prevElem;
        }
        
        /**
         * @return {@code true} if the iterator has a previous item.
         */
        public boolean hasPrevious() {
            return prevElem != null;
        }

        /**
         * @return The previous item.
         * 
         * @throws NoSuchElementException If {@link #hasPrevious()} returned {@code false}.
         */
        public DLLItem<T> previous() {
            if (!hasPrevious()) throw new NoSuchElementException();
            if (mod != itMod) throw new ConcurrentModificationException();
            prevElem = (nextElem = prevElem).getPrev();
            i--;
            return returned = nextElem;
        }
        
        @Override
        public int nextIndex() {
            return i;
        }
        
        @Override
        public int previousIndex() {
            return i - 1;
        }

        @Override
        public void remove() {
            if (mod != itMod) throw new ConcurrentModificationException();
            if (!deleteElem(returned)) throw new IllegalStateException();
            itMod++;
        }
        
        @Override
        public void set(DLLItem<T> elem) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(DLLItem<T> elem) {
            throw new UnsupportedOperationException();
        }
        
        
    }
    
    
    /**
     * ListIterator implementation for the items in the list. <br>
     * It uses the fail-fast mechanism of the {@link DoubleListElementIterator} class to detect
     * changes in the underlying linked list.
     */
    private class DoubleListIterator
            implements ListIterator<T> {
        
        /** The underlying element list iterator used to redirect the functions. */
        private final DoubleListElementIterator it;

        /**
         * Creates a new List iterator, starting at the given element with the given index.
         * 
         * @param elem The element to start at.
         * @param i    The index of the given element.
         */
        DoubleListIterator(DLLItem<T> elem, int i) {
            it = new DoubleListElementIterator(elem, i);
        }
        
        
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public T next() {
            return it.next().getItem();
        }

        @Override
        public boolean hasPrevious() {
            return it.hasPrevious();
        }

        @Override
        public T previous() {
            return it.previous().getItem();
        }

        @Override
        public int nextIndex() {
            return it.nextIndex();
        }

        @Override
        public int previousIndex() {
            return it.previousIndex();
        }

        @Override
        public void remove() {
            it.remove();
        }

        @Override
        public void set(T t) {
            it.set(new DLLItem<T>(t, null, null));
        }

        @Override
        public void add(T t) {
            it.add(new DLLItem<T>(t, null, null));
        }
        
        
    }


    /* --------------------------------------------------------------------------------
     * Constructors
     * --------------------------------------------------------------------------------
     */
    /**
     * Initializes an empty doubly linked list.
     */
    public DoublyLinkedList() {
    }

    /**
     * Initializes a doubly linked list containing the elements of the given collection.
     * 
     * @param col The elements to add to the list.
     */
    public DoublyLinkedList(Collection<T> col) {
        addAll(col);
    }


    /* --------------------------------------------------------------------------------
     * Functions
     * --------------------------------------------------------------------------------
     */
    @Override
    public void addFirst(T item) {
        DLLItem<T> newLink = new DLLItem<T>(item, null, first);
        if (size++ == 0) first = last = newLink;
        else {
            first.setPrev(newLink);
            first = newLink;
        }
        mod++;
    }
    
    @Override
    public void addLast(T item) {
        DLLItem<T> newLink = new DLLItem<T>(item, last, null);
        if (size++ == 0) first = last = newLink;
        else {
            last.setNext(newLink);
            last = newLink;
        }
        mod++;
    }
    
    @Override
    public boolean offerFirst(T item) {
        addFirst(item);
        return true;
    }
    
    @Override
    public boolean offerLast(T item) {
        addLast(item);
        return true;
    }
    
    @Override
    public T removeFirst() {
        if (first == null) return null; 
        T item = first.getItem();
        if ((first = first.getNext()) != null) {
            first.setPrev(null);
        }
        size--;
        mod++;
        return item;
    }
    
    @Override
    public T removeLast() {
        if (last == null) return null;
        T item = last.getItem();
        if ((last = last.getPrev()) != null) {
            last.setNext(null);
        }
        size--;
        mod++;
        return item;
    }
    
    @Override
    public T pollFirst() {
        return removeFirst();
    }
    
    @Override
    public T pollLast() {
        return removeLast();
    }
    
    @Override
    public T getFirst() {
        return (first == null ? null : first.getItem());
    }
    
    @Override
    public T getLast() {
        return (last == null ? null : last.getItem());
    }
    
    @Override
    public T peekFirst() {
        return getFirst();
    }
    
    @Override
    public T peekLast() {
        return getLast();
    }
    
    @Override
    public boolean removeFirstOccurrence(Object obj) {
        DoubleListElementIterator it = new DoubleListElementIterator(first, 0);
        while (it.hasNext()) {
            DLLItem<T> elem = it.next();
            if (Objects.equals(obj, elem.getItem())) {
                deleteElem(elem);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean removeLastOccurrence(Object obj) {
        DoubleListElementIterator it = new DoubleListElementIterator(last, size - 1);
        while (it.hasPrevious()) {
            DLLItem<T> elem = it.previous();
            if (Objects.equals(obj, elem.getItem())) {
                deleteElem(elem);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean offer(T item) {
        addLast(item);
        return true;
    }
    
    @Override
    public T remove() {
        return removeFirst();
    }
    
    @Override
    public T poll() {
        return pollFirst();
    }
    
    @Override
    public T element() {
        return getFirst();
    }
    
    @Override
    public T peek() {
        return peekFirst();
    }
    
    @Override
    public void push(T item) {
        addFirst(item);
    }
    
    @Override
    public T pop() {
        return removeFirst();
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    @Override
    public boolean contains(Object obj) {
        for (T item : this) {
            if (Objects.equals(obj, item)) return true;
        }
        return false;
    }

    @Override
    public Iterator<T> descendingIterator() {
        DoubleListIterator it = new DoubleListIterator(last, size - 1);
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return it.hasPrevious();
            }

            @Override
            public T next() {
                return it.previous();
            }
        };
    }
    
    @Override
    public Iterator<T> iterator() {
        return new DoubleListIterator(first, 0);
    }
    
    @Override
    public Object[] toArray() {
        return toArray(new Object[size]);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T2> T2[] toArray(T2[] arr) {
        int i = 0;
        for (T item : this) {
            arr[i++] = (T2) item;
        }
        return arr;
    }
    
    @Override
    public boolean add(T item) {
        addLast(item);
        return true;
    }
    
    @Override
    public boolean remove(Object obj) {
        return removeFirst() == null;
    }
    
    @Override
    public boolean containsAll(Collection<?> collection) {
        if (collection.isEmpty()) return true;
        Set<?> set = new HashSet<>(collection);
        if (size < set.size()) return false;
        DoubleListElementIterator it = new DoubleListElementIterator(first, 0);
        while (it.hasNext()) {
            DLLItem<T> elem = it.next();
            if (set.remove(elem.getItem())) {
                if (set.isEmpty()) return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean addAll(Collection<? extends T> collection) {
        if (collection.isEmpty()) return false;
        for (T item : collection) {
            DLLItem<T> elem = new DLLItem<T>(item, last, null);
            if (size++ == 0) first = last = elem;
            else {
                last.setNext(elem);
                last = elem;
            }
            mod++;
        }
        return true;
    }
    
    @Override
    public boolean addAll(int i, Collection<? extends T> collection) {
        if (i < 0 || size < i) throw new IndexOutOfBoundsException(i);
        if (i == 0) return addAll(collection);
        if (collection.isEmpty()) return false;
        DLLItem<T> prev = getElem(i - 1);
        DLLItem<T> end = prev.getNext();
        for (T item : collection) {
            DLLItem<T> elem = new DLLItem<T>(item, prev, end);
            prev.setNext(elem);
            prev = elem;
            size++;
            mod++;
        }
        if (end == null) last = prev;
        else end.setPrev(prev);
        return true;
    }
    
    @Override
    public boolean removeAll(Collection<?> collection) {
        if (isEmpty()) return false;
        boolean changed = false;
        DoubleListElementIterator it = new DoubleListElementIterator(first, 0);
        while (it.hasNext()) {
            DLLItem<T> elem = it.next();
            if (collection.contains(elem)) {
                deleteElem(elem);
                mod++;
                changed = true;
            }
        }
        return changed;
    }
    
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        first = null;
        last = null;
    }
    
    @Override
    public T get(int index) {
        return getElem(index).getItem();
    }

    /**
     * @param index The index of the element to return.
     * @return The element at the given index.
     */
    private DLLItem<T> getElem(int index) {
        if (index < 0 || size <= index) throw new IndexOutOfBoundsException(index);
        if (first == null) throw new IndexOutOfBoundsException(index);
        ListIterator<DLLItem<T>> it;
        boolean forward;
        int i;
        boolean allowCache = (cached != null && cachedMod == mod);
        
        if ((!allowCache && index <= size / 2) ||
                (allowCache && index <= cachedIndex / 2)) { // first item is closest.
            it = new DoubleListElementIterator(first, 0);
            i = index;
            forward = true;

        } else if (!allowCache || index > (size + cachedIndex) / 2) { // last item is closest.
            it = new DoubleListElementIterator(last, size - 1);
            i = (size - 1) - index;
            forward = false;
            
        } else { // cached item is closest and is still valid.
            if (index == cachedIndex) return cached;
            forward = (cachedIndex < index);
            i = Math.abs(cachedIndex - index);
            it = new DoubleListElementIterator(cached, cachedIndex);
        }
        for ( ; i > 0; i--) {
            if (forward) it.next();
            else it.previous();
        }
        cachedMod = mod;
        cachedIndex = index;
        return cached = it.next();
    }
    
    @Override
    public T set(int i, T item) {
        DLLItem<T> elem = getElem(i);
        T oldItem = elem.getItem();
        elem.setItem(item);
        return oldItem;
    }
    
    @Override
    public void add(int i, T item) {
        if (i == 0) addFirst(item);
        else if (i == size - 1) addLast(item);
        else {
            DLLItem<T> next = getElem(i);
            DLLItem<T> prev = next.getNext();
            DLLItem<T> elem = new DLLItem<T>(item, prev, next);
            next.setPrev(elem);
            prev.setNext(elem);
        }
    }
    
    @Override
    public T remove(int i) {
        DLLItem<T> elem = getElem(i);
        deleteElem(elem);
        return elem.getItem();
    }
    
    @Override
    public int indexOf(Object obj) {
        int i = 0;
        for (T item : this) {
            if (Objects.equals(obj, item)) return i;
            i++;
        }
        return -1;
    }
    
    @Override
    public int lastIndexOf(Object obj) {
        DoubleListElementIterator it = new DoubleListElementIterator(last, size - 1);
        for (int i = 0; it.hasPrevious(); i++) {
            DLLItem<T> item = it.previous();
            if (Objects.equals(obj, item)) {
                return i;
            }
            i++;
        }
        return -1;
    }
    
    @Override
    public ListIterator<T> listIterator() {
        return new DoubleListIterator(first, 0);
    }
    
    @Override
    public ListIterator<T> listIterator(int i) {
        return new DoubleListIterator(getElem(i), i);
    }
    
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public DoublyLinkedList<T> clone() {
        return new DoublyLinkedList<T>(this);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (!(obj instanceof DoublyLinkedList)) return false;
        DoublyLinkedList<T> list = (DoublyLinkedList<T>) obj;
        if (list.size != this.size) return false;
        if (isEmpty()) return true;
        Iterator<T> thisIt = this.iterator();
        Iterator<T> listIt = list.iterator();
        while (thisIt.hasNext() && listIt.hasNext()) {
            if (!Objects.equals(listIt.next(), thisIt.next())) return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = 41;
        for (T item : this) {
            result = 37 * result + (item == null ? 0 : item.hashCode());
        }
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getCanonicalName() + "[");
        boolean first = true;
        for (T item : this) {
            if (first) first = false;
            else sb.append(",");
            sb.append(item == null ? "null" : item.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Deletes the given element from the linked list.
     * 
     * @param elem The element to be deleted.
     * @return {@code true} if the element is removed. {@code false} otherwise.
     */
    private boolean deleteElem(DLLItem<T> elem) {
        if (elem == null) return false;
        DLLItem<T> next = elem.getNext();
        DLLItem<T> prev = elem.getPrev();
        
        if (next == null && prev == null) {
            return false;
            
        } else if (next == null) {
            prev.setNext(null);
            first = prev;
            
        } else if (prev == null) {
            next.setPrev(null);
            last = next;
            
        } else {
            next.setPrev(prev);
            prev.setNext(next);
        }
        size--;
        mod++;
        return true;
    }

    /**
     * Deletes the chain of items starting at {@code fromIndex} (inclusivee), and ending at
     * {@code toIndex} (exclusive).
     * 
     * @param fromIndex The index to start removing elements from (inclusive)
     * @param toIndex   The index to stop removing elements (exclusive).
     * @throws IndexOutOfBoundsException If {@code toIndex < fromIndex}, or if either
     *      {@code fromIndex} or {@code toIndex} are out of bounds.
     */
    public void deleteChain(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < fromIndex || size < toIndex) {
            throw new IndexOutOfBoundsException(fromIndex + ", " + toIndex);
        }
        DLLItem<T> begin = getElem(fromIndex);
        DLLItem<T> end = getElem(toIndex - 1);
        DLLItem<T> prev = begin.getPrev();
        DLLItem<T> next = end.getNext();
        if (prev != null) prev.setNext(next);
        else first = next;
        if (next != null) next.setPrev(prev);
        else last = prev;
        size -= (toIndex - fromIndex);
        mod++;
    }

    /**
     * Inserts a collection of items from the given index onwards, i.e. the first element
     * in the collection will be added at the index {@code index}. <br>
     * This function only serves as a renaming of the {@link #addAll(int, Collection)} function.
     * 
     * @param index      The index to start adding from.
     * @param collection Collection containing the elements to be inserted.
     * @throws IndexOutOfBoundsException If {@code fromIndex} is out of bounds.
     * 
     * @see #addAll(int, Collection)
     */
    public void insertChain(int index, Collection<T> collection) {
        addAll(index, collection);
    }

    /**
     * Replaces the elements from the given chain with the elements in the collection.
     *
     * @param fromIndex  The index to start removing elements from (inclusive)
     * @param toIndex    The index to stop removing elements (exclusive).
     * @param collection The items to add in place of the removed elements.
     * @throws IndexOutOfBoundsException If {@code toIndex < fromIndex}, or if either
     *      {@code fromIndex} or {@code toIndex} are out of bounds.
     * 
     * @see #deleteChain(int, int)
     * @see #insertChain(int, Collection)
     */
    public void replaceChain(int fromIndex, int toIndex, Collection<T> collection) {
        deleteChain(fromIndex, toIndex);
        insertChain(fromIndex, collection);
    }
    
    
}
