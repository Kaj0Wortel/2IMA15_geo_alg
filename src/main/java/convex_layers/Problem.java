package convex_layers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Class for storing the data representing the problem.
 */
@Getter
@Setter
@AllArgsConstructor
public class Problem {
    /** The name of the problem. */
    private String name;
    /** The set of vertices representing the problem. */
    private Set<InputVertex> vertices;
    
    
}
