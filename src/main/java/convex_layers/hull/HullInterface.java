package convex_layers.hull;

import convex_layers.BaseInputVertex;
import convex_layers.OutputEdge;
import convex_layers.math.Edge;
import tools.Pair;
import tools.Var;
import tools.data.collection.rb_tree.LinkedRBTree;
import tools.iterators.FunctionIterator;
import tools.iterators.InverseListIterator;
import tools.iterators.MultiIterator;
import tools.log.Logger;

import java.util.*;

public interface HullInterface <IV extends BaseInputVertex>
            extends Collection<IV> {


        /* ----------------------------------------------------------------------
         * Functions.
         * ----------------------------------------------------------------------
         */
        /**
         * Determines the four points near the intersection of the extended edge with the hull.
         *
         * @apiNote Runs in {@code O(log(n))}.
         *
         * @param vye     The edge to get the intersection of.
         * @param hullOnLeftSide Whether the given edge has nodes of it's convex hull on the left of it.
         *
         * @return A {@link NearIntersection} with the points.
         *
         * @throws IllegalStateException If {@link #isEmpty()}{@code == true}.
         * @throws IllegalArgumentException If the line created from the given edge
         *     intersects the top/bottom of the hull.
         */
        public NearIntersection<IV> getPointsNearLine(VectorYEdge<IV> vye, boolean hullOnLeftSide) ;




        /**
         * Gets the next node of the chain. If no next node exists, takes the maximum node of the
         * other chain. If this node also doesn't exist, then the same node is returned.
         *
         * @apiNote Runs in {@code O(1)}.
         *
         * @param node The node to get the next node of.
         *
         * @return The next node in the chain.
         */
        public VectorYNode<IV> next(VectorYNode<IV> node) ;

        /**
         * Gets the previous node of the chain. If no previous node exists, takes the minimum node
         * of the other chain. If this node also doesn't exist, then the same node is returned.
         *
         * @apiNote Runs in {@code O(1)}.
         *
         * @param node The node to get the previous node of.
         *
         * @return The previous node in the chain.
         */
        public VectorYNode<IV> prev(VectorYNode<IV> node) ;


        /**
         * Traverses the hull in clockwise order.
         *
         * @apiNote Runs in {@code O(1)}.
         *
         * @param node The node to get the next node for.
         *
         * @return The next node in clockwise order.
         *
         * @throws IllegalArgumentException If the given node is not part of this hull.
         */
        public VectorYNode<IV> clockwise(VectorYNode<IV> node) ;



        /**
         * Traverses the hull in counter clockwise order.
         *
         * @apiNote Runs in {@code O(1)}.
         *
         * @param node The node to get the next node for.
         *
         * @return The next node in counter clockwise order.
         *
         * @throws IllegalArgumentException If the given node is not part of this hull.
         */
        public VectorYNode<IV> counterClockwise(VectorYNode<IV> node) ;



        /**
         * @apiNote Runs in {@code O(log(n))}.
         *
         * @return A random edge from the hull.
         */
        public VectorYEdge<IV> getRandomEdge() ;

        /**
         * Returns the input vertex at the given index. The indices are order from the minimal
         * to the maximal vertex of the left tree, an then from the maximal to the minimal
         * vertex of the right tree.
         *
         * @apiNote This function runs in {@code O(log(n))}.
         *
         * @param i The input vertex to get.
         *
         * @return The input vertex at the given index.
         */
        public IV get(int i) ;

        /**
         * Returns the node at the given index. The indices are order from the minimal
         * to the maximal node of the left tree, an then from the maximal to the minimal
         * node of the right tree.
         *
         * @apiNote This function runs in {@code O(log(n))}.
         *
         * @param i The node to get.
         *
         * @return The node at the given index.
         */
        public VectorYNode<IV> getNode(int i) ;

        /**
         * Adds an input vertex and updates the hull accordingly by removing vertices from the hull. <br>
         * If the just added point was inside the hull, then it will be removed directly and returned in
         * the returned list. This is the only way for the given vertex to be removed. <br>
         * Moreover, if the given vertex is removed, then no other vertices will be removed.
         *
         * @apiNote Runs in {@code O(log(n) + k)}, where {@code k} denotes the number of removed elements.
         *
         * @param iv The input vertex to add.
         *
         * @return A list containing all removed vertices.
         */
        public List<IV> addAndUpdate(IV iv) ;

        /**
         * {@inheritDoc}
         *
         * @apiNote Runs in {@code O(k*log(n))}.
         *
         * @apiNote This function runs in {@code O(log(n) + k)}, where {@code k} denotes the size of the collection.
         */
        @Override
        default boolean containsAll(Collection<?> col) {
            for (Object obj : col) {
                if (!contains(obj)) return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @apiNote Runs in {@code O(k*log(n))}.
         *
         * @apiNote This function runs in {@code O(log(n) + k)}, where {@code k} denotes the size of the collection.
         */
        @Override
        @SuppressWarnings("unchecked")
        default boolean addAll(Collection<? extends IV> col) {
            return addAll((Iterable<IV>) col);
        }

        /**
         * {@inheritDoc}
         *
         * @apiNote Runs in {@code O(k*log(n))}.
         *
         * @apiNote This function runs in {@code O(log(n) + k)}, where {@code k} denotes the size of the collection.
         */
        @Override
        default boolean removeAll(Collection<?> col) {
            boolean mod = false;
            for (Object obj : col) {
                if (remove(obj)) mod = true;
            }
            return mod;
        }



        /**
         * Adds all vertices from the list.
         *
         * @apiNote Runs in {@code O(k*log(n))}.
         *
         * @param ivs The vertices to add.
         *
         * @return {@code true} if the data structure was modified. {@code false} otherwise.
         */
        default boolean addAll(Iterable<IV> ivs) {
            boolean mod = false;
            for (IV iv : ivs) {
                if (add(iv)) mod = true;
            }
            return mod;
        }


        default boolean removeAll(Iterable<IV> ivs) {
            boolean mod = false;
            for (IV iv : ivs) {
                if (remove(iv)) mod = true;
            }
            return mod;
        }

        /**
         * Removes all nodes from the list.
         *
         * @apiNote Runs in {@code O(k*log(n))}.
         *
         * @param vyns The nodes to remove.
         *
         * @return {@code true} if the data structure was modified. {@code false} otherwise.
         */
        default boolean removeAllNodes(Iterable<IV> vyns) {//TODO fix kaj
            boolean mod = false;
            for (IV vyn : vyns) {
                if (remove(vyn)) mod = true;
            }
            return mod;
        }

        @Override
        default boolean isEmpty() {
            return size() == 0;
        }

        @Override
        default Object[] toArray() {
            return toArray(new BaseInputVertex[size()]);
        }

        @Override
        @SuppressWarnings("unchecked")
        default <T> T[] toArray(T[] arr) {
            int i = 0;
            for (IV iv : this) {
                if (i >= arr.length) break;
                arr[i++] = (T) iv;
            }
            return arr;
        }


        public Collection<OutputEdge> getInnerPointConnections(IV center) ;



    }
