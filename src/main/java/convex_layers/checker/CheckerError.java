package convex_layers.checker;

import convex_layers.BaseInputVertex;
import convex_layers.OutputEdge;
import convex_layers.visual.Visual;
import lombok.Getter;
import lombok.NonNull;
import tools.Var;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * This class is used to relay the errors from a {@link Checker} instance back to the caller.
 */
public class CheckerError {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** List of points which caused an error. */
    @Getter
    @NonNull
    private final Collection<BaseInputVertex> points;
    /** List of edges which caused an error. */
    @Getter
    @NonNull
    private final Collection<OutputEdge> edges;
    
    
    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates a new checker error.
     */
    public CheckerError() {
        points = new HashSet<>();
        edges = new HashSet<>();
    }
    
    /**
     * Creates a new checker error using the given lists as underlying data. Hence,
     * Modifying the given lists will alter the data in this class.
     * 
     * @param errorPoints The list containing the points which caused an error.
     * @param errorEdges  The list containing the edges which caused an error.
     */
    public CheckerError(List<BaseInputVertex> errorPoints, List<OutputEdge> errorEdges) {
        this.points = errorPoints;
        this.edges = errorEdges;
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * @return {@code true} if there was an error. {@code false} otherwise.
     */
    public boolean hasErrors() {
        return !points.isEmpty() || !edges.isEmpty();
    }
    
    /**
     * Draws the error edges and points
     * @param vis The visualizer used to draw on.
     */
    public void draw(Visual vis) {
        vis.addEdge(Visual.toEdge(edges), Color.RED);
        vis.addPoint(Visual.toVec(points), Visual.toLabel(points), Color.RED);
        vis.redraw();
    }
    
    /**
     * Adds a point to the error point list.
     * 
     * @param iv The input vertex to add to the list.
     */
    public void addPoint(BaseInputVertex iv) {
        points.add(iv);
    }
    
    /**
     * Adds a point to the error point list.
     *
     * @param ivs The input vertices to add to the list.
     */
    public void addPoints(Collection<BaseInputVertex> ivs) {
        points.addAll(ivs);
    }
    
    /**
     * Adds an edge to the error edge list.
     * 
     * @param edge The output edge to add to the list.
     */
    public void addEdge(OutputEdge edge) {
        edges.add(edge);
    }
    
    /**
     * Adds an edge to the error edge list.
     *
     * @param edges The output edges to add to the list.
     */
    public void addEdges(Collection<OutputEdge> edges) {
        this.edges.addAll(edges);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(Var.LS);
        sb.append("==========  ERROR  REPORT  ==========");
        sb.append(Var.LS);
        if (!hasErrors()) {
            sb.append("No errors were found!");
            sb.append(Var.LS);

        } else {
            sb.append("----------  ERROR  EDGES  ---------");
            sb.append(Var.LS);
            if (edges.isEmpty()) {
                sb.append("No error edges were found!");
                sb.append(Var.LS);
                
            } else {
                for (OutputEdge edge : edges) {
                    sb.append(edge.toShortString());
                    sb.append(Var.LS);
                }
            }
            
            sb.append(Var.LS);
            sb.append("----------  ERROR  POINTS  ---------");
            sb.append(Var.LS);

            if (points.isEmpty()) {
                sb.append("No error points were found!");

            } else {
                for (BaseInputVertex biv : points) {
                    sb.append(biv.toShortString());
                    sb.append(Var.LS);
                }
            }
        }
        return sb.toString();
    }
    
    
}
