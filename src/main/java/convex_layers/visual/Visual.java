package convex_layers.visual;

import convex_layers.BaseInputVertex;
import convex_layers.OutputEdge;
import convex_layers.math.Edge;
import convex_layers.math.Vector;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface Visual {

    /**
     * Redraws the image on the canvas.
     */
    void redraw();
    
    /**
     * Sets the points to be shown. <br>
     * Each collection in the list can get a separate color by using the {@link #setPointColors(List)} function.
     * If no color is specified, or the color is {@code null}, then the default color is used.
     *
     * @param points The list of collection of points to be shown.
     */
    void setPoints(List<Iterable<Vector>> points);
    
    /**
     * Adds the given point list to the editor.
     *
     * @param pointList The extra point list to display.
     */
    void addPoint(Iterable<Vector> pointList);
    
    /**
     * Adds the given point list to the editor.
     *
     * @param pointList The extra point list to display.
     * @param labelList The list of labels for the points.
     */
    void addPoint(Iterable<Vector> pointList, Iterable<String> labelList);
    
    /**
     * Adds the given point list to the editor.
     *
     * @param pointList The extra point list to display.
     * @param labelList The list of labels for the points.
     * @param paint     The color of the points to display.
     */
    void addPoint(Iterable<Vector> pointList, Iterable<String> labelList, Paint paint);
    
    /**
     * Sets the colors of the point collections. <br>
     * The n'th element in the list corresponds to the color of the n'th collection of points.
     * The amount of colors doesn't have to match the total number of collections of points.
     * The {@code null} value denotes the default color.
     *
     * @param pointColors The colors of the points.
     */
    void setPointColors(List<Paint> pointColors);
    
    /**
     * Sets the edges to be shown. <br>
     * Each collection in the list can get a separate color by using the {@link #setEdgeColors(List)} function.
     * If no color is specified, or the color is {@code null}, then the default color is used.
     *
     * @param edges The list of collection of edges to be shown.
     */
    void setEdges(List<Iterable<Edge>> edges);
    
    /**
     * Adds the given edge list to the editor.
     *
     * @param edgeList The extra edge list to display.
     */
    void addEdge(Iterable<Edge> edgeList);

    /**
     * Adds the given edge list to the editor.
     *
     * @param edgeList The extra edge list to display.
     * @param paint    The paint used to color the edges.
     */
    void addEdge(Iterable<Edge> edgeList, Paint paint);
    
    /**
     * Sets the colors of the edge collections. <br>
     * The n'th element in the list corresponds to the color of the n'th collection of edges.
     * The amount of colors doesn't have to match the total number of collections of edges.
     * The {@code null} value denotes the default color.
     *
     * @param edgeColors The colors of the edges.
     */
    void setEdgeColors(List<Paint> edgeColors);
    
    /**
     * Sets the labels to be shown.
     *
     * @param labelList The list of collection of labels to be shown.
     */
    void setLabels(List<Iterable<String>> labelList);
    
    /**
     * Adds the given label list to the editor.
     *
     * @param labelList The extra edge list to display.
     */
    void addLabel(Iterable<String> labelList);
    
    /**
     * Clears all current data and sets the new data.
     *
     * @param data The data to be set for the visualizer.
     */
    void setData(List<Iterable<? extends BaseInputVertex>> data);
    
    /**
     * Adds the given data to the current data.
     *
     * @param data The data to be added.
     */
    void addData(List<Iterable<? extends BaseInputVertex>> data);


    /**
     * Clears all point, edge and label data, but leaves the colors and images untouched.
     */
    void clear();

    /**
     * Clears all data except for the colors.
     */
    void clearAll();
    
    /**
     * Converts a collection of {@link BaseInputVertex} to a collection of {@link Vector}. <br>
     * This function clones the data, which implies that the data <b>won't</b> be modified when the
     * original collection is modified.
     *
     * @param in The collection to be converted.
     *
     * @return The underlying data of the input collection.
     *
     * @see #toVec(Iterable)
     */
    static Iterable<Vector> cloneToVec(Iterable<? extends BaseInputVertex> in) {
        List<Vector> out = new ArrayList<>();
        for (BaseInputVertex iv : in) {
            out.add(iv.getV().clone());
        }
        return out;
    }
    
    /**
     * Converts a collection of {@link BaseInputVertex} to a collection of {@link Vector}. <br>
     * This function does not clone the data, which implies that the data <b>will</b> be updated
     * when the original collection is modified.
     *
     * @param in The collection to be converted.
     *
     * @return The underlying data of the input collection.
     *
     * @see #cloneToVec(Iterable)
     */
    static Iterable<Vector> toVec(final Iterable<? extends BaseInputVertex> in) {
        return () -> new Iterator<Vector>() {
            private final Iterator<? extends BaseInputVertex> it = in.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Vector next() {
                return it.next().getV();
            }
        };
    }
    
    /**
     * Converts a collection of {@link BaseInputVertex} to a collection of labels. <br>
     * This function does not clone the data, which implies that the data <b>will</b> be updated
     * when the original collection is modified.
     *
     * @param in The collection to be converted.
     *
     * @return The labels matching the given input vertices.
     */
    static Iterable<String> toLabel(final Iterable<? extends BaseInputVertex> in) {
        return () -> new Iterator<String>() {
            private final Iterator<? extends BaseInputVertex> it = in.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public String next() {
                return Long.toString(it.next().getId());
            }
        };
    }
    
    /**
     * Converts a collection of {@link OutputEdge} to a collection of {@link Edge}. <br>
     * This function clones the data, which implies that the data <b>won't</b> be modified when the
     * original collection is modified.
     *
     * @param in The collection to be converted.
     * @return The underlying data of the input collection.
     *
     * @see #toEdge(Iterable)
     */
    static Iterable<Edge> cloneToEdge(Iterable<OutputEdge> in) {
        List<Edge> out = new ArrayList<>();
        for (OutputEdge e : in) {
            out.add(new Edge(e.getV1().getV().clone(), e.getV2().getV().clone()));
        }
        return out;
    }
    
    /**
     * Converts a collection of {@link OutputEdge} to a collection of {@link Edge}. <br>
     * This function does not clone the data, which implies that the data <b>will</b> be updated
     * when the original collection is modified.
     *
     * @param in The collection to be converted.
     *
     * @return The underlying data of the input collection.
     *
     * @see #cloneToEdge(Iterable)
     */
    static Iterable<Edge> toEdge(final Iterable<OutputEdge> in) {
        return () -> new Iterator<>() {
            private final Iterator<OutputEdge> it = in.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Edge next() {
                OutputEdge e = it.next();
                return new Edge(e.getV1().getV(), e.getV2().getV());
            }
        };
    }
    
    /**
     * Connects the vectors given in the input in the order they are given.
     *
     * @param in The vectors to connect.
     *
     * @return An iterable over the connections between the vectors.
     */
    static Iterable<Edge> connectEdges(final Iterable<Vector> in) {
        return () -> new Iterator<>() {
            private final Iterator<Vector> it = in.iterator();
            private Vector prev = (it.hasNext() ? it.next() : null);

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Edge next() {
                Vector cur = it.next();
                Edge e = new Edge(prev, cur);
                prev = cur;
                return e;
            }
        };
    }
    
    
}
