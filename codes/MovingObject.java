import java.util.Scanner;
import java.util.HashMap;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

public class MovingObject {

    private int movingObjectID;
    private int movingObjectX;
    private int movingObjectY;
    private String movingObjectDirection;
    private int movingObjectStrategy;
    private int movingObjectStatus;
    private boolean isUFO;
    private int fuelLevel;
    
    private String trackAction;
    private String trackNewDirection;
    private Integer trackThrustDistance;

    private static Random randGenerator;
    private final String[] ORIENT_LIST = {"north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest"};
    private HashMap<String, Integer> xDIR_MAP;
    private HashMap<String, Integer> yDIR_MAP;

    private final int EMPTY_CODE = 0;
    private final int STARS_CODE = 1;
    private final int SUN_CODE = 2;
    private final int OK_CODE = 1;
    private final int CRASH_CODE = -1;
    private final int STEALTH_CODE = 3;
    
    public MovingObject() {
        randGenerator = new Random();
        
        xDIR_MAP = new HashMap<>();
        xDIR_MAP.put("north", 0);
        xDIR_MAP.put("northeast", 1);
        xDIR_MAP.put("east", 1);
        xDIR_MAP.put("southeast", 1);
        xDIR_MAP.put("south", 0);
        xDIR_MAP.put("southwest", -1);
        xDIR_MAP.put("west", -1);
        xDIR_MAP.put("northwest", -1);

        yDIR_MAP = new HashMap<>();
        yDIR_MAP.put("north", 1);
        yDIR_MAP.put("northeast", 1);
        yDIR_MAP.put("east", 0);
        yDIR_MAP.put("southeast", -1);
        yDIR_MAP.put("south", -1);
        yDIR_MAP.put("southwest", -1);
        yDIR_MAP.put("west", 0);
        yDIR_MAP.put("northwest", 1);
    }

    // Getter methods //
    public Integer getMovingObjectID() {
        return this.movingObjectID;
    }
    
    public Integer getMovingObjectX() {
        return this.movingObjectX;
    }
    
    public Integer getMovingObjectY() {
        return this.movingObjectY;
    }
    
    public String getMovingObjectDirection() {
        return this.movingObjectDirection;
    }
    
    public Integer getMovingObjectStrategy() {
        return this.movingObjectStrategy;
    }
    
    public Integer getMovingObjectStatus() {
        return this.movingObjectStatus;
    }
    
    public Integer getFuelLevel() {
        return this.fuelLevel;
    }
    
    public boolean getIsUFO() {
        return this.isUFO;
    }
    
    public String getAction() {
        return this.trackAction;
    }
    
    public String getNewDirection() {
        return this.trackNewDirection;
    }
    
    public Integer getThrustDistance() {
        return this.trackThrustDistance;
    }
    
    // Setter methods //
    public void setMovingObjectID(int movingObjectID) {
        this.movingObjectID = movingObjectID;
    }
    
    public void setMovingObjectX(int movingObjectX) {
        this.movingObjectX = movingObjectX;
    }
    
    public void setMovingObjectY(int movingObjectY) {
        this.movingObjectY = movingObjectY;
    }
    
    public void setMovingObjectDirection(String movingObjectDirection) {
        this.movingObjectDirection = movingObjectDirection;
    }
    
    public void setMovingObjectStrategy(int movingObjectStrategy) {
        this.movingObjectStrategy = movingObjectStrategy;
    }
    
    public void setMovingObjectStatus(int movingObjectStatus) {
        this.movingObjectStatus = movingObjectStatus;
    }
    
    public void setFuelLevel(int fuelLevel) {
        this.fuelLevel = fuelLevel;
    }
    
    public void setIsUFO(boolean isUFO) {
        this.isUFO = isUFO;
    }
    
    public void setAction(String trackAction) {
        this.trackAction = trackAction;
    }
    
    public void setNewDirection(String trackNewDirection) {
        this.trackNewDirection = trackNewDirection;
    }
    
    public void setThrustDistance(int trackThrustDistance) {
        this.trackThrustDistance = trackThrustDistance;
    }
    
    // Determine the moving object's action
    public void askMovingObjectForAction(ArrayList<MovingObject> droneInfo, ArrayList<MovingObject> ufoInfo, ArrayList<Region> regionInfo, int regionWidth, int regionHeight) {

        // UFO has a different strategy to determine the next action.
        if (this.isUFO) {
            // generate a move randomly
            int moveRandomChoice = randGenerator.nextInt(100);
            if (moveRandomChoice < 5) {
                // attack
                this.trackAction = "attack";
            } else if (moveRandomChoice < 15) {
                // stealth
                this.trackAction = "pass";
            } else if (moveRandomChoice < 90) {
                // change direction
                this.trackAction = "steer";
            } else {
                // thrust forward with only 1 space
                this.trackAction = "thrust";
                this.trackThrustDistance = 1;
            }

            // determine a new direction
            int steerRandomChoice = randGenerator.nextInt(8);
            if (this.trackAction.equals("steer")) {
                this.trackNewDirection = ORIENT_LIST[steerRandomChoice];
            }
        } else {
            if (this.movingObjectStrategy == 2) {
                // manual strategy - action to be input by user
                Scanner askUser = new Scanner(System.in);
                // generate a move by asking the user - DIAGNOSTIC ONLY
                System.out.print("action?: ");
                this.trackAction = askUser.nextLine();

                // ask for parameter of steer and thrust action
                if (this.trackAction.equals("steer")) {
                    System.out.print("direction?: ");
                    this.trackNewDirection = askUser.nextLine();
                } else if (this.trackAction.equals("thrust")) {
                    System.out.print("distance?: ");
                    this.trackThrustDistance = Integer.parseInt(askUser.nextLine());
                }

            } else if (this.movingObjectStrategy == 1) {
                // initialize variable for strategy 1
                int currentX = this.movingObjectX;
                int currentY = this.movingObjectY;
                String currentDirection = this.movingObjectDirection;
                
                // Taken Gravity from the Suns into consideration.
                int distortedX = 0;
                int distortedY = 0;

                // initialize variables for storing status of surrounding region
                boolean [] scanned_around = new boolean[8];
                boolean [] explored_around = new boolean[8];
                boolean [] sun_around = new boolean[8];
                boolean [] safe_around = new boolean[8];
                String sun_known_around = "none";

                // calculate status of surrounding region
                for (int i = 0; i < ORIENT_LIST.length; i++) {

                    // calculate status of surrounding region
                    String lookThisWay = ORIENT_LIST[i];
                    scanned_around[i] = regionInfo.get(checkSquareID(currentX, currentY, lookThisWay, 1, regionWidth, regionHeight)).getRegionScanned();
                    explored_around[i] = regionInfo.get(checkSquareID(currentX, currentY, lookThisWay, 1, regionWidth, regionHeight)).getRegionInfo() != STARS_CODE;
                    sun_around[i] = regionInfo.get(checkSquareID(currentX, currentY, lookThisWay, 1, regionWidth, regionHeight)).getRegionInfo() == SUN_CODE;
                    safe_around[i] = !sun_around[i] && !regionInfo.get(checkSquareID(currentX, currentY, lookThisWay, 1, regionWidth, regionHeight)).getUFOSuspected();

                    // if there are sun around, calculate the location as if the drone is dstorted
                    if (scanned_around[i] & sun_around[i]) {
                        sun_known_around = ORIENT_LIST[i];
                        distortedX = (currentX + xDIR_MAP.get(sun_known_around) + regionWidth) % regionWidth;
                        distortedY = (currentY + yDIR_MAP.get(sun_known_around) + regionHeight) % regionHeight;
                    }

                    // if there are drones in the surrounding region, overwrite
                    int checkX = regionInfo.get(checkSquareID(currentX, currentY, lookThisWay, 1, regionWidth, regionHeight)).getRegionX();
                    int checkY = regionInfo.get(checkSquareID(currentX, currentY, lookThisWay, 1, regionWidth, regionHeight)).getRegionY();
                    for (int j = 0; j < droneInfo.size(); j++) {
                        if (droneInfo.get(j).getMovingObjectStatus() == OK_CODE && checkX == droneInfo.get(j).getMovingObjectX() && checkY == droneInfo.get(j).getMovingObjectY()) { safe_around[i] = false; }
                    }

                }

                // initialize variables for storing status of region ahead
                boolean [] scanned_forward = new boolean[3];
                boolean [] explored_forward = new boolean[3];
                boolean [] sun_forward = new boolean[3];
                boolean [] safe_forward = new boolean[3];
                boolean [] scanned_forward_distorted = new boolean[3];
                boolean [] explored_forward_distorted = new boolean[3];
                boolean [] sun_forward_distorted = new boolean[3];
                boolean [] safe_forward_distorted = new boolean[3];

                // calculate status of region ahead
                for (int i = 0; i < 3; i++) {

                    // calculate status of region ahead
                    scanned_forward[i] = regionInfo.get(checkSquareID(currentX, currentY, currentDirection, i + 1, regionWidth, regionHeight)).getRegionScanned();
                    explored_forward[i] = regionInfo.get(checkSquareID(currentX, currentY, currentDirection, i + 1, regionWidth, regionHeight)).getRegionInfo() != STARS_CODE;
                    sun_forward[i] = regionInfo.get(checkSquareID(currentX, currentY, currentDirection, i + 1, regionWidth, regionHeight)).getRegionInfo() == SUN_CODE;
                    safe_forward[i] = !sun_forward[i] && !regionInfo.get(checkSquareID(currentX, currentY, currentDirection, i + 1, regionWidth, regionHeight)).getUFOSuspected();

                    // if there are sun around, calculate the status of region ahead as if the drone is dstorted
                    if (sun_known_around != "none") {
                        scanned_forward_distorted[i] = regionInfo.get(checkSquareID(distortedX, distortedY, currentDirection, i + 1, regionWidth, regionHeight)).getRegionScanned();
                        explored_forward_distorted[i] = regionInfo.get(checkSquareID(distortedX, distortedY, currentDirection, i + 1, regionWidth, regionHeight)).getRegionInfo() != STARS_CODE;
                        sun_forward_distorted[i] = regionInfo.get(checkSquareID(distortedX, distortedY, currentDirection, i + 1, regionWidth, regionHeight)).getRegionInfo() == SUN_CODE;
                        safe_forward_distorted[i] = !sun_forward[i] && !regionInfo.get(checkSquareID(distortedX, distortedY, currentDirection, i + 1, regionWidth, regionHeight)).getUFOSuspected();
                    }

                    // if there are drones in the region ahead, overwrite
                    int checkX = regionInfo.get(checkSquareID(currentX, currentY, currentDirection, i + 1, regionWidth, regionHeight)).getRegionX();
                    int checkY = regionInfo.get(checkSquareID(currentX, currentY, currentDirection, i + 1, regionWidth, regionHeight)).getRegionY();
                    int checkDistortedX = regionInfo.get(checkSquareID(distortedX, distortedY, currentDirection, i + 1, regionWidth, regionHeight)).getRegionX();
                    int checkDistortedY = regionInfo.get(checkSquareID(distortedX, distortedY, currentDirection, i + 1, regionWidth, regionHeight)).getRegionY();
                    for (int j = 0; j < droneInfo.size(); j++) {
                        if (droneInfo.get(j).getMovingObjectStatus() == OK_CODE && checkX == droneInfo.get(j).getMovingObjectX() && checkY == droneInfo.get(j).getMovingObjectY()) { safe_forward[i] = false; }
                        if (droneInfo.get(j).getMovingObjectStatus() == OK_CODE && checkDistortedX == droneInfo.get(j).getMovingObjectX() && checkDistortedY == droneInfo.get(j).getMovingObjectY()) { safe_forward_distorted[i] = false; }
                    }

                }

                // if-else loop for rule based strategy (a conservative strategy)
                if (sun_known_around != "none") {
                    // escape if we know there is a sun nearby
                    this.trackAction = "thrust";
                    if (Math.abs(Arrays.asList(ORIENT_LIST).indexOf(this.movingObjectDirection) - Arrays.asList(ORIENT_LIST).indexOf(sun_known_around)) == 4) {
                        // move 2 square if the sun is behind
                        this.trackThrustDistance = 2;
                    } else if (this.fuelLevel >= 4 & scanned_forward[0] & safe_forward[0] & scanned_forward[1] & safe_forward[1] & scanned_forward[2] & safe_forward[2] & scanned_forward_distorted[2] & safe_forward_distorted[2]) {
                        // if 3 square ahead and the distortation square is scanned and safe
                        this.trackThrustDistance = 3;
                    } else if (this.fuelLevel >= 3 & scanned_forward[0] & safe_forward[0] & scanned_forward[1] & safe_forward[1] & scanned_forward_distorted[1] & safe_forward_distorted[1]) {
                        // if 2 square ahead and the distortation square is scanned and safe
                        this.trackThrustDistance = 2;
                    } else if (this.fuelLevel >= 2 & scanned_forward[0] & safe_forward[0] & scanned_forward_distorted[0] & safe_forward_distorted[0]) {
                        // if 1 square ahead and the distortation square is scanned and safe
                        this.trackThrustDistance = 1;
                    } else {
                        // move as fuel available
                        if (this.fuelLevel > 1) {
                            this.trackThrustDistance = Math.min(this.fuelLevel - 1, 3);
                        } else {
                            // no possible move, so let's pass and refuel!
                            this.trackAction = "pass";
                        }
                    }
                } else if (this.fuelLevel <= 4) {
                    // refuel
                    this.trackAction = "pass";
                } else if (scanned_forward[0] & safe_forward[0] & scanned_forward[1] & safe_forward[1] & scanned_forward[2] & !explored_forward[2] & safe_forward[2]) {
                    // if 3 square ahead is scanned, safe, and unexplored
                    this.trackAction = "thrust";
                    this.trackThrustDistance = 3;
                } else if (scanned_forward[0] & safe_forward[0] & scanned_forward[1] & !explored_forward[1] & safe_forward[1]) {
                    // if 2 square ahead is scanned, safe, and unexplored
                    this.trackAction = "thrust";
                    this.trackThrustDistance = 2;
                } else if (scanned_forward[0] & !explored_forward[0] & safe_forward[0]) {
                    // if 1 square ahead is scanned, safe, and unexplored
                    this.trackAction = "thrust";
                    this.trackThrustDistance = 1;
                } else if (!scanned_forward[0] & !explored_forward[0]) {
                    // if something around is not scanned
                    this.trackAction = "scan";
                } else if (explored_forward[0]) {
                    // if all thing around is scanned, but not all have be explored
                    boolean all_scanned = true;
                    boolean all_explored = true;
                    boolean all_safe = true;
                    ArrayList<Integer> can_explore = new ArrayList<Integer>();
                    ArrayList<Integer> can_steer = new ArrayList<Integer>();
                    for (int k = 0; k < ORIENT_LIST.length; k++) {
                        if (!scanned_around[k]) {
                            all_scanned = false;
                        }
                        if (safe_around[k]) {
                            can_steer.add(k);
                        } else {
                            all_safe = false;
                        }
                        if (!explored_around[k]) {
                            all_explored = false;
                            if (safe_around[k]) {
                                can_explore.add(k);
                            }
                        }
                    }
                    // if not all surrounding region are scanned, then scan
                    if (!all_scanned) {
                        this.trackAction = "scan";
                    } else {
                        // if all surroundng region are scanned, but not all are explored, steer to the unexplored region
                        if (!all_explored){
                            if (can_explore.size() > 0) {
                                this.trackAction = "steer";
                                this.trackNewDirection = ORIENT_LIST[can_explore.get(0)];
                            } else {
                                this.trackAction = "pass";
                            }
                        }
                        // if all surroundng region are scanned and explored
                        if (all_explored) {
                            if (!all_safe) {
                            // but if not all are safe, steer to a safe direction
                                if (can_steer.size() > 0) {
                                    this.trackAction = "steer";
                                    this.trackNewDirection = ORIENT_LIST[can_steer.get(0)];
                                } else {
                                    this.trackAction = "pass";
                                }
                            } 
                            if (all_safe) {
                               // if all surroundng region are scanned and explored, and all direction are safe, then try to move to the closest unexplore region
                                ArrayList<Integer> unexplored = new ArrayList<Integer>();
                                ArrayList<Double> distance = new ArrayList<Double>();
                                for (int k = 0; k < regionWidth * regionHeight; k++) {
                                    if (regionInfo.get(k).getRegionInfo() == STARS_CODE) {
                                        unexplored.add(k);
                                        distance.add(Math.sqrt(Math.pow(regionInfo.get(k).getRegionX() - currentX, 2) + Math.pow(regionInfo.get(k).getRegionY() - currentY, 2)));
                                    }
                                }
                                if (unexplored.size() == 0) {
                                    this.trackAction = "pass";
                                } else {
                                    // Euclidean norm is used for approximate the distance.
                                    double closest_distance = Collections.min(distance);
                                    int closest_ID = unexplored.get(distance.indexOf(closest_distance));
                                    int closest_X = regionInfo.get(closest_ID).getRegionX();
                                    int closest_Y = regionInfo.get(closest_ID).getRegionY();
                                    int distance_X = closest_X - currentX;
                                    int distance_Y = closest_Y - currentY;
                                    String direction_desired = " ";
                                    // calculate to the corresponding direction
                                    if (distance_X > 0) {
                                        if (distance_Y > 0) {direction_desired = "northeast";}
                                        if (distance_Y == 0) {direction_desired = "east";}
                                        if (distance_Y < 0) {direction_desired = "southeast";}
                                    }
                                    if (distance_X == 0) {
                                        if (distance_Y > 0) {direction_desired = "north";}
                                        if (distance_Y < 0) {direction_desired = "south";}
                                    }
                                    if (distance_X < 0) {
                                        if (distance_Y > 0) {direction_desired = "northwest";}
                                        if (distance_Y == 0) {direction_desired = "west";}
                                        if (distance_Y < 0) {direction_desired = "southwest";}
                                    }
                                    // thrust to the desired direction as resonable as possible, steer to the desired drection if not in correct direction
                                    if (direction_desired.equals(currentDirection)) {
                                        this.trackAction = "thrust";
                                        this.trackThrustDistance = 1;
                                        if (safe_forward[0] & safe_forward[1] & closest_distance >= 1.5) {this.trackThrustDistance = 2;}
                                        if (safe_forward[0] & safe_forward[1] & safe_forward[2] & closest_distance >= 3) {this.trackThrustDistance = 3;}
                                    } else {
                                        this.trackAction = "steer";
                                        this.trackNewDirection = direction_desired;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (this.movingObjectStrategy == 0) {
                // generate a move randomly
                int moveRandomChoice = randGenerator.nextInt(100);
                if (moveRandomChoice < 5) {
                    // do nothing
                    this.trackAction = "pass";
                } else if (moveRandomChoice < 20) {
                    // check your surroundings
                    this.trackAction = "scan";
                } else if (moveRandomChoice < 50) {
                    // change direction
                    this.trackAction = "steer";
                } else {
                    // thrust forward
                    this.trackAction = "thrust";
                    int thrustRandomChoice = randGenerator.nextInt(3);
                    this.trackThrustDistance = thrustRandomChoice + 1;
                }

                // determine a new direction
                int steerRandomChoice = randGenerator.nextInt(8);
                if (this.trackAction.equals("steer")) {
                    this.trackNewDirection = ORIENT_LIST[steerRandomChoice];
                }
            } else {
                System.out.println("ERROR: Only 0, 1, 2 is allowed for strategy code.\n");
                System.exit(1);
            }
        }
    }
    
    // return the square based on the moving object current x , y , distance and region width, height.
    public int checkSquareID(int movingObjectX, int movingObjectY, String direction, int distance, int regionWidth, int regionHeight) {
        int tempX = (movingObjectX + xDIR_MAP.get(direction) * distance + regionWidth) % regionWidth;
        int tempY = (movingObjectY + yDIR_MAP.get(direction) * distance + regionHeight) % regionHeight;
        return (tempX * regionHeight + tempY);
    }
    
}
