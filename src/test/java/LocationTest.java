

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LocationTest {

	static double EPSILON = 1e-5;
	
	
	@Test
	public void testEuclideanDistance() {
		Location from = new Location(0,0);
		Location to = new Location(10,10);
		assertEquals(from.euclideanDistance(to), 14.1421356237, EPSILON);
		assertEquals(from.euclideanDistance(to), to.euclideanDistance(from), EPSILON);
	}
	
	@Test
	public void testManhattanDistance() {
		Location from = new Location(0,0);
		Location to = new Location(10,10);
		assertEquals(from.manhattanDistance(to), 20);
		assertEquals(from.manhattanDistance(to), to.manhattanDistance(from));
	}
}
