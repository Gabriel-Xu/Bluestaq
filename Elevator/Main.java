import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Main {
    private static ArrayList<Elevator> elevators;
    private static int NUM_ELEVATORS = 2;

    public static void request(int floor, String requestDir) {
        System.out.println(String.format("\nNew Request: Floor %d, Direction %s", floor, requestDir));
        Elevator bestElevator = null;
        int minScore = -10000;
        for (Elevator elevator : elevators) {
            int score = scoreElevator(elevator, floor, requestDir);
            if (minScore == -10000 || score < minScore) {
                minScore = score;
                bestElevator = elevator;
            }
        }
        bestElevator.addRequest(floor);
    }
    
    private static int scoreElevator(Elevator elevator, int floor, String requestDir) {
        int currentFloor = elevator.getCurrentFloor();
        String currentDir = elevator.getCurrentDirection();
        int score = Math.abs(currentFloor - floor) * 100;
        if (currentDir.equals(Elevator.NONE)) {
            score -= 50;
        } else if (currentDir.equals(requestDir)) {
            if ((requestDir.equals(Elevator.UP) && floor > currentFloor) || (requestDir.equals(Elevator.DOWN) && floor < currentFloor)) {
                score -= 10;
            } else {
                score += 500;
            }
        } else {
            score += 1000; 
        }
        return score;
    }

    public static void step() {
        System.out.println("\nOne Step Simulated");
        for (Elevator elevator : elevators) {
            elevator.step();
        }
    }

    public static ArrayList<String> getStatus() {
        ArrayList<String> statuses = new ArrayList<>();
        for (Elevator elevator : elevators) {
            statuses.add(elevator.getStatus());
        }
        return statuses;
    }
    
    public static void internalRequest(int elevatorId, int destinationFloor) {
        System.out.println(String.format("Internal Request for Elevator %d to Floor %d", elevatorId, destinationFloor));
        elevators.get(elevatorId - 1).addRequest(destinationFloor);
    }

    public static void main(String[] args) {
        elevators = new ArrayList<>();
        for (int i = 1; i <= NUM_ELEVATORS; i++) {
            elevators.add(new Elevator(i, 1)); 
        }
        System.out.println("Initial State:");
        getStatus().forEach(System.out::println);
        request(5, Elevator.UP);
        request(2, Elevator.UP);
        step();
        request(10, Elevator.DOWN);
        step();
        step();
        internalRequest(1, 8); 
        step();
        step();
        step();
        internalRequest(2, 1);
        step();
        step();
        step();
        System.out.println("\nFinal State:");
        getStatus().forEach(System.out::println);
    }
}

class Elevator {
    private final int id;
    private int currentFloor;
    private String currentDirection;
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String NONE = "NONE";
    private static final int MIN_FLOOR = 1;
    private static final int MAX_FLOOR = 10; 

    private final PriorityQueue<Integer> upRequests;
    private final PriorityQueue<Integer> downRequests;

    public Elevator(int id, int startingFloor) {
        this.id = id;
        this.currentFloor = startingFloor;
        this.currentDirection = NONE;
        this.upRequests = new PriorityQueue<>();
        this.downRequests = new PriorityQueue<>(Comparator.reverseOrder()); 
    }

    public void addRequest(int floor) {
        if (floor < MIN_FLOOR || floor > MAX_FLOOR) return;
        if (floor >= currentFloor) {
            upRequests.add(floor);
        } else {
            downRequests.add(floor);
        }
        if (currentDirection.equals(NONE)) {
            determineNewDirection();
        }
    }

    public void step() {
        int floor = currentFloor;
        if (upRequests.contains(floor) || downRequests.contains(floor)) {
            System.out.println(String.format("\nElevator %d STOPPING at Floor %d", id, floor));
            upRequests.remove(floor);
            downRequests.remove(floor);
            determineNewDirection();
            return;
        }

        if (currentDirection.equals(UP)) {
            currentFloor++;
        } else if (currentDirection.equals(DOWN)) {
            currentFloor--;
        }
        if (currentFloor > MAX_FLOOR) {
            currentFloor = MAX_FLOOR;
            determineNewDirection();
        }
        if (currentFloor < MIN_FLOOR) {
            currentFloor = MIN_FLOOR;
            determineNewDirection();
        }
        if (currentDirection.equals(NONE)) {
            determineNewDirection();
        }
    }

    private void determineNewDirection() {
        if (currentDirection.equals(UP) && !upRequests.isEmpty() && upRequests.peek() >= currentFloor) return;
        if (currentDirection.equals(DOWN) && !downRequests.isEmpty() && downRequests.peek() <= currentFloor) return;
        if (!upRequests.isEmpty()) {
            currentDirection = UP;
        } else if (!downRequests.isEmpty()) {
            currentDirection = DOWN;
        } else {
            currentDirection = NONE;
        }
    }

    public int getCurrentFloor() { return currentFloor; }
    public String getCurrentDirection() { return currentDirection; }
    public String getStatus() {
        return String.format("ID: %d | Floor: %d | Dir: %s | Up Stops: %s | Down Stops: %s", id, currentFloor, currentDirection, upRequests, downRequests);
    }
}

/*
Assumes a reasonably small amount of floors such that integer overflow in the scoring function will not occur.
Assumes that in one unit of time, each elevator can open/close doors and move exactly one floor.
Assumes a certain number of elevators and floors and sequence of requests, but this is easily adjustable.
Assumes an elevator movement pattern of going along the current direction until no more floors in current direction to visit.
Assumes there is only one thread calling the methods.

Features that could be implemented include:
- a more sophisticated elevator scoring algorithm
- more precise timing of door opening/closing
- tracking of number of passengers/weight
- allowing multiple threads to simultaneously call methods on the system of elevators
*/
