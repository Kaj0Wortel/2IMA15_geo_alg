package convex_layers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * Class for storing the data representing an optimized version for storing the data.
 */
@Getter
@Setter
@AllArgsConstructor
public class Problem2 {
    /** The name of the problem. */
    private String name;
    /** The vertices representing the problem. */
    private Collection<BaseInputVertex> vertices;


}
