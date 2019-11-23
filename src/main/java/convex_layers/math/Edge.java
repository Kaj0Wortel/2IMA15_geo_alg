package convex_layers.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.PublicCloneable;

@Setter
@Getter
@AllArgsConstructor
public class Edge
            implements PublicCloneable {
    private Vector v1;
    private Vector v2;

    /**
     * @return The length of the edge.
     */
    public double length() {
        double x = v2.x() - v1.x();
        double y = v2.y() - v1.y();
        return Math.sqrt(x*x + y*y);
    }
    
    @Override
    public Edge clone() {
        return new Edge(v1.clone(), v2.clone());
    }
    
}
