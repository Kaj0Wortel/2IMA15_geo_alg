package convex_layers.prior_tree;

import lombok.AllArgsConstructor;
import tools.data.array.ArrayTools;

import java.util.Arrays;
import java.util.Collection;

public class PriorTree<T extends PriorTreeNode<T>> {

    /**
     * @apiNote Runs in {@code n*log(n)}.
     * 
     * @param nodes The nodes to be added.
     */
    @SuppressWarnings("unchecked")
    public PriorTree(Collection<T> nodes) {
        Elem<T>[] xData = new Elem[nodes.size()];
        Elem<T>[] yData = Arrays.copyOf(xData, xData.length);
        {
            int i = 0;
            for (T elem : nodes) {
                xData[i++] = new Elem<T>(elem);
            }
        }
        Arrays.sort(xData, Elem::compareToX);
        for (int i = 0; i < xData.length; i++) {
            xData[i].xIndex = i;
        }
        Arrays.sort(yData, Elem::compareToY);
        for (int i = 0; i < yData.length; i++) {
            yData[i].yIndex = i;
        }
        
        
    }
    
    private static class Elem<T extends PriorTreeNode<T>>
            implements PriorTreeNode<Elem<T>> {
        int xIndex;
        int yIndex;
        T elem;
        
        public Elem(T elem) {
            this.elem = elem;
        }
        
        @Override
        public int compareToX(Elem<T> node) {
            return elem.compareToX(node.elem);
        }
        
        @Override
        public int compareToY(Elem<T> node) {
            return elem.compareToY(node.elem);
        }
        
        
    }
    
    /**
     * Finds the {@code i}'th median of the given sorted array.
     * 
     * @apiNote Runs in {@code log(n)}.
     * 
     * @param arr   The array to get the medians from.
     * @param amt   The total number of medians.
     * @param index The median to get.
     * 
     * @return The {@code i}'th median when selecting {@code amt} medians.
     */
    private static <T> T getMedian(T[] arr, int amt, int index) {
        int i = (int) ((index + 0.5) * arr.length / amt);
        i = Math.max(0, Math.min(arr.length - 1, i));
        return arr[i];
    }
    
    
}
