# cmpt317-assignment1

###Problem Description
Given N vehicles and K packages, each of which is currently located at some point within a city which has M locations, the goal is to find the best route for each vehicle such that all packages are delivered to their predetermined destination in the shortest amount of time possible. In other words, the goal is to minimize longest single package delivery, meaning that all packages are delivered within a reasonable time period. A vehicle is capable of carrying any number of packages at one time.

###Solution Description
In order to solve this problem, I decided to use a state space search, more specifically, A*. In order to implement this, the problem needs to be describable in a series of states, each of which has a set of actions which transition to adjacent states, an associated cost of reaching that state, as well as an initial and goal state. These requirements are detailed below.

#####State
The state specifies the current location of all trucks and packages, as well as specifying whether or not a package is currently held by a truck. It also contains information about the cost of that state, specifically the cost of getting to that state, as well as the estimated cost of reaching the solution. In addition, it contains a reference to the previous state to keep track of the path from the initial state to the solution state once the solution state is found.

#####Initial State
The initial state specifies the starting state of the program, from which the solution state must be reached. In an attempt to generalize my solution, there are no limitations placed upon the starting state such as limiting the starting positions of trucks or the destinations of packages.

#####Actions
Each vehicle has a set of available actions depending upon its current location. A vehicle can move to any adjacent location, pick up a package at their current location, or drop off a package at the package’s destination.

#####Transition Model
Given a state and a set of actions (one action per vehicle), this returns the resulting state. Because I am attempting to simulate time, all vehicles are capable of performing one action per transition, meaning that there are ~4N adjacent states.

#####Goal Test
This checks whether the state matches the goal, where each package is at its destination and is not currently being held by a vehicle.

#####Path Cost
Each step has a cost of one, representing one unit of time. It is assumed that all actions, whether moving a block in the city or picking up or dropping off a package all take the same amount of time.
 
###Implementation Description
To implement the project, I decided used Java, as it had all of the required data structures available either natively (in the case of priority queues) or through well-documented libraries such as JGraphT (in the case of graphs).  The two custom sets of data which I had to implement myself were State and Location.

The Location object is mostly a pretty basic integer x and y location, with a function capable of calculating the Manhattan distance from that Location to another given Location, which is used in estimating the future cost for a given state. Aside from this, the rest of the program is located within the State object.

The State object contains the majority of the logic for the program. It stores all of the information required for each state, including the current location of all trucks and packages, whether a given package is held, the cost of reaching that state, and a reference to the state from which it was reached. It also has a field for the estimated future cost, which is calculated when State.calculateRemainingCost() is called, after the trucks have been appropriately moved. While a field is not explicitly needed, computing the heuristic is expensive and as such is only done once per state, since the value will be referenced many times within the DFS priority queue. Lastly, the State object contains the map and package destinations to allow it to find adjacent states. Since these are not needed for every state, they are made static so that only one of these objects exists which is referenced by all states.

The majority of the complication in the program comes from the need for all trucks to be able concurrently. This means that AiN adjacent states must be generated, where Ai is the number of actions which can be taken by truck i. This translates to roughly 4N adjacent states. To make this possible, I created a recursive function called State.generateAdjacentStates(), which recursively generates all state combinations for each truck, and determines given a set of possible actions which can be made by that truck. Actions were divided into three types, movements, pickups and drop-offs. Each of these were handled separately by the following functions:
* State.getMoves() returns all possible Locations which a truck can reach.
* State.getPickups() returns all possible packages which a truck may pickup
* State.getDropoffs() returns all possible packages which a truck may drop-off

Because so many states were generated based off of each state, I tried to make cloning as simple and quick as possible, which is why arrays and indexes were used, instead of a proper object oriented design that would be expected with Java.

The heuristic used for this program is found in State.calculateRemainingCost(). Since all vehicles can move at the same time, the shortest amount of time that it could take to find the solution is the single longest Manhattan distance from the package to the nearest truck, added to the Manhattan distance of the truck to its destination. Depending upon whether or not the package has been picked up or dropped off, an additional two actions may be added. This heuristic is consistent as there is no possible way for a package to reach its destination faster, as the heuristic estimates the cost as when perfect conditions are met, meaning that there are no obstacles between the truck, the package, and the destination, and the truck is currently not moving towards another package.

###Results
In order to get an idea of how well this solution works for a variety of problems, random initial and solution states were generated for various numbers of trucks, packages, and map sizes, to analyze general performance. Performance was measured in terms of the number of states that were generated in order to find a solution. Since the number of states generated is the most quickly growing part of the program, it provides a decent estimation of the computation time, and can easily be compared to the theoretical worst case. 
Below are the performance results of these problems, grouped by number of packages:

 
fig. 1
 
fig.2
 
fig. 3
 
fig. 4

Sample Output
```
Number of trucks: 2
Number of packages: 2
Map width: 3
Map height: 3

Truck 0 Position     : (0,1)
Truck 1 Position     : (1,2)
Package 0 Position   : (0,1)
Package 0 Destination: (0,2)
Package 1 Position   : (0,2)
Package 1 Destination: (1,1)

Cost: 5
States generated: 51

Truck 0 Path  : (0,1), (0,0), (0,1), (0,1), (0,2), (0,2)
Truck 1 Path  : (1,2), (0,2), (0,2), (0,1), (1,1), (1,1)

Package 0 Path: (0,1), (0,1), (0,1), (0,1), (0,2), (0,2)
Package 1 Path: (0,2), (0,2), (0,2), (0,1), (1,1), (1,1) 
```

###Performance
The performance of this program quickly gets out of hand and does not scale well to real world numbers. Beyond about 10,000,000 states, the program is not likely to finish computation within a reasonable amount of time. That said, the heuristic significantly reduces the number of states generated to find a solution when compared to a DFS where no heuristic is used.

This is most visible when the heuristic is most effective, in the case of one vehicle and one package on a large map. The heuristic performs very well here, since there is only one package for which the distance must be minimized, and the vehicle will always move in the correct direction. In one instance of execution, 439 states were generated to find a solution that had a depth of 109. If we were instead to use a blind DFS, roughly 4.2125E+65 states would need to be generated, using 4ND as an estimation. 

While results are not always as dramatic, the heuristic makes it possible to solve many problems that would otherwise be incomputable. Even when the heuristic performs poorly, such as in the case of one truck and four packages, it still improves performance by a significant amount. In once instance of execution, 7470505 states were generated to find a solution that had a depth of 19. This solution is far superior to a blind DFS where the number of states generated would be 2.7488E+11.

#####Increasing N (Vehicles)
Increasing the number of vehicles has a complex effect on the number of states that are generated and the overall cost of the solution. Adding additional trucks increases the branching factor, since the number of adjacent states is roughly 4N, but it also reduces the depth of the solution, as it is more likely for a truck to be near a package. At a certain point, the cost of additional branching outweighs whatever reduction in cost an additional truck would bring.

#####Increasing K (Packages)
Increasing the number of packages increases the number of states generated and the overall cost of the solution, as there are more packages that need to be delivered and the depth of the solution always increases.  As shown in fig. 2, performance becomes exceedingly bad when the number of packages exceeds the number of vehicles. This behaviour is a result of a deficiency in the heuristic.

The heuristic’s only goal is to reduce the time of delivery for whichever package is currently furthest from its destination. As a result, packages that are near a vehicle will be avoided to instead reduce the estimated cost of the further package, and the nearest package will only be picked up in branches that are initially deemed less optimal.  This same behaviour can occur when the number of packages is less than the number of vehicle (seen with 4 trucks and 3 packages in fig. 3), but there is a lower likelihood of it occurring, since each vehicle will likely pickup its closest package.

#####Increasing M (Locations)
Increasing the number of locations generally increases the number of states generated. This is because as the size of the map increases, packages are likely to be further from their destination, increasing the depth of the solution. However, due to the deficiencies in heuristic used, it can also occasionally improve results, as packages become more spaced out, and trucks are less likely to pick up a large number of packages, which causes poor performance in this program.

###Conclusion
While this project was not successful in providing a realistic solution to the N-K problem, it was still worthwhile in the sense that I learned a lot about A* and heuristics in doing it. I managed to implement a general solution to the N-K problem that “works” for any N, K and M (given infinite computing power and memory), and is both correct and optimal. 

The performance of this project could be marginally improved by reducing the branching factor by avoiding inarguably incorrect paths (such as taking the same street back and forth), but these optimizations would be ultimately fruitless as the benefit would not be large enough to make this program scalable, and wouldn’t address the large issue that still remains in the heuristic preventing a larger number of packages than vehicles for which there is no easy solution.

The biggest success that my program had was in its flexibility. Additional factors such as refuelling trucks, limiting the number of packages that can be held by trucks and garages for the trucks could be easily added with just a few additions to the code. All of these features either add limitations to decision-making, or additional decisions to be made, which is easily changeable in my program. The problem with adding additional features lies in mapping these complications to a heuristic that is capable of effectively predicting a correct and optimal result.
Using artificial intelligence is a great way to solve complex problems for which obvious solutions do not exist. However, limiting yourself to finding optimal solutions can make performance tank. If I were to attempt this problem again, I would not focus on finding an optimal solution, and would attempt to better balance efficiency, to allow for realistic values for N and K. I would also not try to make all trucks move in synchronization to find the quickest solution, and instead impose a time limit by limiting the search’s depth, as allowing all trucks to make a decision for each transition complicates the heuristic significantly, and increases the branching factor by a huge degree.
