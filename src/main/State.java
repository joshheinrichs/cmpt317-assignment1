package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.SimpleGraph;

public class State implements Comparable<State> {

	static int mapWidth = 4;
	static int mapHeight = 3;
	static UndirectedGraph<Location, DefaultEdge> map;
	static NeighborIndex<Location, DefaultEdge> neighborIndex;

	static int numTrucks = 2;
	Location[] truckPositions;

	static int numPackages = 4;
	int[] packageHolders;
	Location[] packagePositions;
	static Location[] packageDestinations;
	static Location[][] locations;

	/**
	 * The cost to get to this state.
	 */
	int cost;

	/**
	 * The estimated cost to get to the solution state.
	 */
	int remainingCost;

	/**
	 * Sum of individual movements, which is recorded to prevent the solution
	 * state from containing pointless moves.
	 */
	int moves;

	/**
	 * The state from which this state was reached.
	 */
	State previousState = null;

	/**
	 * Constructs the first state
	 */
	public State() {
		truckPositions = new Location[numTrucks];

		packageHolders = new int[numPackages];
		for (int i = 0; i < numPackages; i++) {
			packageHolders[i] = -1;
		}
		packagePositions = new Location[numPackages];
		State.packageDestinations = new Location[numPackages];

		cost = 0;
		moves = 0;

		State.map = new SimpleGraph<Location, DefaultEdge>(DefaultEdge.class);
		State.neighborIndex = new NeighborIndex<Location, DefaultEdge>(map);

		generateMap();
		generateState();

		calculateRemainingCost();
	}

	/**
	 * Generates a random map of size {@link mapWidth} x {@link mapHeight}.
	 */
	void generateMap() {
		locations = new Location[mapWidth][mapHeight];
		for (int i = 0; i < mapWidth; i++) {
			for (int j = 0; j < mapHeight; j++) {
				locations[i][j] = new Location(i, j);

				map.addVertex(locations[i][j]);
				if (i > 0) {
					map.addEdge(locations[i][j], locations[i - 1][j]);
				}
				if (j > 0) {
					map.addEdge(locations[i][j], locations[i][j - 1]);
				}
			}
		}
	}

	/**
	 * Randomly adds the packages and trucks to the map, and gives the packages
	 * random destinations.
	 */
	void generateState() {
		Random rand = new Random();

		for (int i = 0; i < numTrucks; i++) {
			truckPositions[i] = locations[rand.nextInt(mapWidth)][rand
					.nextInt(mapHeight)];
		}

		for (int i = 0; i < numPackages; i++) {
			packagePositions[i] = locations[rand.nextInt(mapWidth)][rand
					.nextInt(mapHeight)];
			packageDestinations[i] = locations[rand.nextInt(mapWidth)][rand
					.nextInt(mapHeight)];
		}
	}

	/**
	 * Constructs a clone state
	 */
	public State(State state) {
		truckPositions = new Location[numTrucks];
		for (int i = 0; i < numTrucks; i++) {
			truckPositions[i] = state.truckPositions[i];
		}

		packageHolders = new int[numPackages];
		packagePositions = new Location[numPackages];
		for (int i = 0; i < numPackages; i++) {
			packageHolders[i] = state.packageHolders[i];
			packagePositions[i] = state.packagePositions[i];
		}

		this.cost = state.cost;
		this.moves = state.moves;
		this.previousState = state.previousState;
	}

	/**
	 * Returns true if all packages have been delivered, false otherwise.
	 */
	boolean isSolution() {
		for (int i = 0; i < numPackages; i++) {
			if (!packageDelivered(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the package is currently being held, false otherwise.
	 * 
	 * @param i
	 *            Index of the package
	 */
	boolean packageHeld(int i) {
		return packageHolders[i] != -1;
	}

	/**
	 * Returns true if the package has been delivered, false otherwise.
	 * 
	 * @param i
	 *            Index of the package
	 */
	boolean packageDelivered(int i) {
		return packagePositions[i] == packageDestinations[i] && !packageHeld(i);
	}

	/**
	 * Calculates the estimated remaining cost for trucks at their current
	 * position.
	 */
	void calculateRemainingCost() {
		int maxCost = 0;
		// for each package, find nearest truck
		for (int i = 0; i < numPackages; i++) {
			// if not delivered
			if (!packageDelivered(i)) {
				int destinationDistance = packagePositions[i]
						.manhattanDistance(packageDestinations[i]);
				int truckDistance = 0;

				if (!packageHeld(i)) {
					truckDistance = packagePositions[i]
							.manhattanDistance(truckPositions[0]);
					for (int j = 1; j < numTrucks; j++) {
						truckDistance = Math.min(truckDistance,
								packagePositions[i]
										.manhattanDistance(truckPositions[j]));
					}

					// add one move for pickup
					truckDistance++;
				}

				// add one move for dropoff
				destinationDistance++;

				maxCost = Math
						.max(maxCost, truckDistance + destinationDistance);
			}
		}

		this.remainingCost = maxCost;
	}

	/**
	 * Returns the cost needed to reach the current state.
	 */
	int getCurrentCost() {
		return cost;
	}

	/**
	 * Returns the minimum remaining cost to reach a solution. Before calling
	 * this method, {@link calculateEstimatedCost()} should be called.
	 */
	int getRemainingCost() {
		return remainingCost;
	}

	/**
	 * Returns the total cost of this state, which is defined as the sum of the
	 * current and remaining cost. Before calling this method, {@link
	 * calculateEstimatedCost()} should be called.
	 */
	int getTotalCost() {
		return cost + remainingCost;
	}

	/**
	 * Returns a list of states which can be reached by the current state.
	 */
	public ArrayList<State> generateAdjacentStates() {
		ArrayList<State> states = generateAdjacentStates(numTrucks - 1);
		for (int i = 0; i < states.size(); i++) {
			states.get(i).calculateRemainingCost();
		}
		return states;
	}

	/**
	 * Returns a list of states which can be reached by the current state.
	 */
	public ArrayList<State> generateAdjacentStates(int truck) {
		ArrayList<State> states = new ArrayList<State>();
		if (truck < 0) {
			State state = this.clone();
			state.cost++;
			state.previousState = this;
			states.add(state);
			return states;
		} else {
			ArrayList<State> recursedStates = generateAdjacentStates(truck - 1);

			// for each state generated by previous trucks
			for (int i = 0; i < recursedStates.size(); i++) {
				// for all possible movements
				ArrayList<Location> moveList = getMoves(truck);
				for (int j = 0; j < moveList.size(); j++) {
					State state = recursedStates.get(i).clone();

					state.truckPositions[truck] = moveList.get(j);
					for (int k = 0; k < numPackages; k++) {
						if (state.packageHolders[k] == truck) {
							state.packagePositions[k] = moveList.get(j);
						}
					}

					state.moves++;
					states.add(state);
				}

				// for all possible pickups
				ArrayList<Integer> pickupList = getPickups(truck);
				for (int j = 0; j < pickupList.size(); j++) {
					State state = recursedStates.get(i).clone();

					state.packageHolders[pickupList.get(j)] = truck;

					state.moves++;
					states.add(state);
				}

				// for all possible dropoffs
				ArrayList<Integer> dropoffList = getDropoffs(truck);
				for (int j = 0; j < dropoffList.size(); j++) {
					State state = recursedStates.get(i).clone();

					state.packageHolders[dropoffList.get(j)] = -1;

					state.moves++;
					states.add(state);
				}

				// //do nothing
				// State state = recursedStates.get(i).clone();
				// states.add(state);
			}

			return states;
		}
	}

	/**
	 * Returns a list of locations which can be reached from the truck's current
	 * location.
	 * 
	 * @param truck
	 *            Index of the truck
	 */
	public ArrayList<Location> getMoves(int truck) {
		ArrayList<Location> list = new ArrayList<Location>(
				neighborIndex.neighborsOf(truckPositions[truck]));
		assert (list.size() == 2);
		return list;
	}

	/**
	 * Returns a list of package indexes which are available for pickup at the
	 * truck's current location
	 * 
	 * @param truck
	 *            Index of the truck
	 */
	public ArrayList<Integer> getPickups(int truck) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < numPackages; i++) {
			if (!packageHeld(i) && packagePositions[i] == truckPositions[truck]) {
				list.add(i);
			}
		}
		return list;
	}

	/**
	 * Returns a list of package indexes which are available for dropoff at the
	 * truck's current location
	 * 
	 * @param truck
	 *            Index of the truck
	 */
	public ArrayList<Integer> getDropoffs(int truck) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < numPackages; i++) {
			if (packageHolders[i] == truck
					&& packagePositions[i] == packageDestinations[i]) {
				list.add(i);
			}
		}
		return list;
	}

	/**
	 * Returns the path from the starting state to this state.
	 */
	public ArrayList<State> getPath() {
		LinkedList<State> path = new LinkedList<State>();

		path.addFirst(this);
		while (path.peek() != null) {
			path.addFirst(path.peek().previousState);
		}
		assert (path.peek() == null);
		path.pop();
		return new ArrayList<State>(path);
	}

	/**
	 * Compares states by their estimated total cost. If equal, it then compares
	 * the states by the number of moves taken
	 */
	@Override
	public int compareTo(State o) {
		if (this.getTotalCost() - o.getTotalCost() != 0) {
			return this.getTotalCost() - o.getTotalCost();
		}
		// else if(this.getRemainingCost() - o.getRemainingCost() != 0)
		// {
		return this.getRemainingCost() - o.getRemainingCost();
		// }
		// else
		// {
		// return this.moves - o.moves;
		// }
	}

	/**
	 * Creates a clone of the current state, duplicating the truck and package
	 * positions. Package destinations and map locations are shared between
	 * states.
	 */
	@Override
	public State clone() {
		return new State(this);
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		System.out.print("Number of trucks: ");
		State.numTrucks = in.nextInt();
		System.out.print("Number of packages: ");
		State.numPackages = in.nextInt();
		System.out.print("Map width: ");
		State.mapWidth = in.nextInt();
		System.out.print("Map height: ");
		State.mapHeight = in.nextInt();

		in.close();

		PriorityQueue<State> queue = new PriorityQueue<State>();
		State initialState = new State();

		System.out.println("");

		for (int i = 0; i < State.numTrucks; i++) {
			System.out.println("Truck " + i + " Position     : ("
					+ initialState.truckPositions[i].getX() + ","
					+ initialState.truckPositions[i].getY() + ")");
		}

		for (int i = 0; i < State.numPackages; i++) {
			System.out.println("Package " + i + " Position   : ("
					+ initialState.packagePositions[i].getX() + ","
					+ initialState.packagePositions[i].getY() + ")");
			System.out.println("Package " + i + " Destination: ("
					+ State.packageDestinations[i].getX() + ","
					+ State.packageDestinations[i].getY() + ")");
		}

		queue.add(initialState);

		int numStatesUsed = 1;
		while (!queue.element().isSolution()) {
			// System.out.println("Current Cost: " +
			// queue.element().getTotalCost());
			ArrayList<State> newStates = queue.remove()
					.generateAdjacentStates();
			queue.addAll(newStates);
			numStatesUsed++;
		}

		State solutionState = queue.poll();

		System.out.println("\nCost: " + solutionState.cost);
		System.out.println("States generated: "
				+ (queue.size() + numStatesUsed) + "\n");

		for (int i = 0; i < State.numTrucks; i++) {
			System.out.println("Truck " + i + " Path  : "
					+ solutionState.outputTruckMovement(i));
		}

		System.out.println("");

		for (int i = 0; i < State.numPackages; i++) {
			System.out.println("Package " + i + " Path: "
					+ solutionState.outputPackageMovement(i));
		}
	}

	String outputTruckMovement(int t) {
		String string = "";
		ArrayList<State> path = this.getPath();
		for (int i = 0; i < path.size(); i++) {
			string += "(" + path.get(i).truckPositions[t].getX() + ","
					+ path.get(i).truckPositions[t].getY() + "), ";
		}
		return string.substring(0, string.length() - 2);
	}

	String outputPackageMovement(int p) {
		String string = "";
		ArrayList<State> path = this.getPath();
		for (int i = 0; i < path.size(); i++) {
			string += "(" + path.get(i).packagePositions[p].getX() + ","
					+ path.get(i).packagePositions[p].getY() + "), ";
		}
		return string.substring(0, string.length() - 2);
	}

}
