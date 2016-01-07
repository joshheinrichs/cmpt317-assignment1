

public class Location {

	int x;
	int y;

	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public double euclideanDistance(Location location) {
		return Math.sqrt((this.getX() - location.getX())
				* (this.getX() - location.getX())
				+ (this.getY() - location.getY())
				* (this.getY() - location.getY()));
	}

	public int manhattanDistance(Location location) {
		return Math.abs(this.getX() - location.getX())
				+ Math.abs(this.getY() - location.getY());
	}
}
