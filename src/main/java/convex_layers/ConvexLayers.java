package convex_layers;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Main project class. <br>
 * This class delegates the work for solving the problem.
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
    
    /**
     * Responsibility Pieter
     */
    private void GiftWrapping() {
        
    }
    
    /**
     * Responsibility Kaj
     */
    private void UpdateHull() {
        
    }
    
    /**
     * Responsibility, Rowin
     */
    private void ReadInput(File input) {
        
    }
    
    /**
     * Responsibility, Stijn
     */
    private void WriteOutput(File output) {
        
    }
    
    /**
     * Responsibility, Stijn, Pieter
     */
    void ComputeIntersection() {
        
    }


    /**
     * The main function used to initialize the program.
     * 
     * @param args The runtime arguments.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
    
}
