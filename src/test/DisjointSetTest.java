import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pillanalyser.pillanalyser.DisjointSet;
import org.junit.jupiter.api.Test;

public class DisjointSetTest {

    @Test
    public void testInitialization() {
        int[] a = new int[10];
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        for (int i = 0; i < a.length; i++) {
            assertEquals(i, DisjointSet.find(a, i)); //each element should be its own root initially
        }
    }

    @Test
    public void testUnion() {
        int[] a = new int[10];
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        DisjointSet.union(a, 1, 2);
        assertEquals(DisjointSet.find(a, 1), DisjointSet.find(a, 2));//1 and 2 should have the same root after union
    }

    @Test
    public void testPathCompression() {
        int[] a = new int[10];
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        DisjointSet.union(a, 1, 2);
        DisjointSet.union(a, 2, 3);
        DisjointSet.find(a, 3);
        assertEquals(DisjointSet.find(a, 1), DisjointSet.find(a, 3)); //Element 1 and 3 should have the same root after path compression
        assertEquals(a[3], a[1]); //Path compression should have updated the root of 3 to be the same as 1
    }
}