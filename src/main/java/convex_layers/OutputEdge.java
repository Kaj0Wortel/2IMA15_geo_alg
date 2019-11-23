
package convex_layers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Class representing an edge between two vertices.
 */
@Getter
@Setter
@AllArgsConstructor
public class OutputEdge {
    
    /** Vertex 1 of the edge. */
    private final InputVertex v1;
    /** Vertex 2 of the edge. */
    private final InputVertex v2;
    
    
}
