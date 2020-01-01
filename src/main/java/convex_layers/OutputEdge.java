
package convex_layers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.Var;

import java.util.Objects;

/**
 * Class representing an edge between two vertices.
 */
@Getter
@Setter
@AllArgsConstructor
public class OutputEdge {
    
    /** Vertex 1 of the edge. */
    private final BaseInputVertex v1;
    /** Vertex 2 of the edge. */
    private final BaseInputVertex v2;
    
    
    public boolean hasEqualEndpointWith(OutputEdge oe) {
        return (Objects.equals(v1, oe.v1) || Objects.equals(v2, oe.v2) ||
                Objects.equals(v1, oe.v2) || Objects.equals(v2, oe.v1));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OutputEdge)) return false;
        OutputEdge oe = (OutputEdge) obj;
        return (Objects.equals(v1, oe.v1) && Objects.equals(v2, oe.v1)) ||
                (Objects.equals(v1, oe.v2) && Objects.equals(v2, oe.v1));
    }
    
    @Override
    public int hashCode() {
        return 41 + 11 * (v1.hashCode() + v2.hashCode());
    }
    
    @Override
    public String toString() {
        return getClass().getCanonicalName() + "[" + Var.LS +
                "    " + v1 + "," + Var.LS +
                "    " + v2 + "," + Var.LS +
                "]";
    }
    
    public String toShortString() {
        return "[v1=" + v1.toShortString() + ",v2=" + v2.toShortString() + "]";
    }
    
    
}
