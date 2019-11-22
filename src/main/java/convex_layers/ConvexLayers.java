package convex_layers;

import java.util.HashSet;
import java.util.Set;

/**
 * Main project class.
 */
public class ConvexLayers {
    //Set that keeps track of all variables that still need to be checked.
    Set<Vertex> todo = new HashSet<Vertex>();
    //Set containing the edges of the resulting convex partitioning
    Set<Edge> sol = new HashSet<Edge>();
    //Linked list of the inner convex hull
    DoublyLinkedList<Vertex> in;
    //Linked list of the outer convex hull
    DoublyLinkedList<Vertex> out;

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
