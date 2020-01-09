package convex_layers.data;

import convex_layers.BaseInputVertex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tools.Var;

import java.util.*;

import static org.junit.Assert.*;

public abstract class Range2DSearchTest {
    
    /** The instance to test. */
    private Range2DSearch<BaseInputVertex> search;
    
    
    /**
     * Initialization done before each test.
     */
    @Before
    public void before() {
        search = getSearch();
    }
    
    /**
     * Tear-down done after each test.
     */
    @After
    public void after() {
        search = null;
    }

    /**
     * @return The instance to test the functionality of.
     */
    public abstract Range2DSearch<BaseInputVertex> getSearch();

    /**
     * Initializes a list of vertices containing all possible integer x and y values in the given range.
     * 
     * @param minX The minimum x-coordinate in the range.
     * @param maxX The maximum x-coordinate in the range.
     * @param minY The minimum y-coordinate in the range.
     * @param maxY The maximum y-coordinate in the range.
     * 
     * @return A list of vertices in the given range.
     */
    protected static List<BaseInputVertex> initList(int minX, int minY, int maxX, int maxY) {
        List<BaseInputVertex> data = new ArrayList<>();
        long id = 0;
        for (int i = minX; i < maxX; i++) {
            for (int j = minY; j < maxY; j++) {
                data.add(new BaseInputVertex(id++, i, j));
            }
        }
        return data;
    }
    
    @Test
    public void initTest() {
        List<BaseInputVertex> data = initList(0, 0, 50, 50);;
        search.init(data);
        
        List<BaseInputVertex> ignored = new ArrayList<>();
        for (BaseInputVertex biv : data) {
            if (!search.contains(biv)) ignored.add(biv);
        }
        
        assertTrue("These vertices didn't occur in the search structure: " +
                Var.LS + ignored.toString().replaceAll(",", "," + Var.LS), ignored.isEmpty());
    }
    
    @Test
    public void searchTest0() {
        List<BaseInputVertex> data = initList(0, 0, 50, 50);
        search.init(data);
        
        Collection<BaseInputVertex> rtn = search.getRange(0, 0, 0, 0, true, true);
        checkEq(rtn, List.of(data.get(0)));
    }
    
    @Test
    public void searchTest1() {
        int size = 50;
        List<BaseInputVertex> data = initList(1, 1, size, size);
        search.init(data);
        
        Collection<BaseInputVertex> rtn = search.getRange(0, 0, 2, 2, true, true);
        checkEq(rtn, List.of(
                data.get(0), data.get(1), data.get(2),
                data.get(size), data.get(size+1), data.get(size+2),
                data.get(2*size), data.get(2*size+2), data.get(2*size+2)
        ));
    }
    
    protected void checkEq(Collection<BaseInputVertex> toCheckCol, Collection<BaseInputVertex> ref) {
        assertTrue("Size too small! expected: " + Var.LS + ref + Var.LS + "but found:" + Var.LS + toCheckCol,
                ref.size() <= toCheckCol.size());
        Set<BaseInputVertex> toCheck = new HashSet<>(toCheckCol);
        assertEquals("Collection contains double elements!", toCheckCol.size(), toCheck.size());
        toCheck.containsAll(ref);
    }
    
    
}
