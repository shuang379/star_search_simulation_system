import java.util.Scanner;
import java.util.HashMap;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.io.*;

public class SimManager {

    private static Random randGenerator;

    private Integer regionWidth;
    private Integer regionHeight;
    private Integer numberOfDrones;
    private Integer numberOfUFO;
    private HashMap<String, Integer> xDIR_MAP;
    private HashMap<String, Integer> yDIR_MAP;
    private String trackAction;
    private String trackNewDirection;
    private Integer trackThrustDistance;
    private String trackMoveCheck;
    private String trackScanResults;
    
    private ArrayList<MovingObject> droneInfo = new ArrayList<MovingObject>();
    private ArrayList<MovingObject> ufoInfo = new ArrayList<MovingObject>();
    private ArrayList<Region> regionInfo = new ArrayList<Region>();

    private Integer turnLimit;
    private Integer numSuns;

    private final int EMPTY_CODE = 0;
    private final int STARS_CODE = 1;
    private final int SUN_CODE = 2;

    private final int OK_CODE = 1;
    private final int CRASH_CODE = -1;
    private final int STEALTH_CODE = 3;
    
    private final int FUEL_MULTIPLE = 3;
    private final int UFO_FREQ = 25;

    private final String[] ORIENT_LIST = {"north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest"};

    public SimManager() {
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

        turnLimit = -1;
    }

    public void uploadStartingFile(String testFileName) {
        final String DELIMITER = ",";

        try {
            Scanner takeCommand = new Scanner(new File(testFileName));
            String[] tokens;
            int i, j, k;

            // read in the region information
            tokens = takeCommand.nextLine().split(DELIMITER);
            regionWidth = Integer.parseInt(tokens[0]);
            tokens = takeCommand.nextLine().split(DELIMITER);
            regionHeight = Integer.parseInt(tokens[0]);

            // generate the region information
            for (i = 0; i < regionWidth; i++) {
                for (j = 0; j < regionHeight; j++) {
                    regionInfo.add(new Region());
                    regionInfo.get(i * regionHeight + j).setRegionX(i);
                    regionInfo.get(i * regionHeight + j).setRegionY(j);
                    regionInfo.get(i * regionHeight + j).setRegionInfo(STARS_CODE);
                    regionInfo.get(i * regionHeight + j).setRegionScanned(false);
                    regionInfo.get(i * regionHeight + j).setUFOSuspected(false);
                }
            }

            // read in the drone starting information
            tokens = takeCommand.nextLine().split(DELIMITER);
            numberOfDrones = Integer.parseInt(tokens[0]);

            for (k = 0; k < numberOfDrones; k++) {
                tokens = takeCommand.nextLine().split(DELIMITER);

                // Error handling.
                if (tokens.length != 4) {
                    System.out.println("ERROR: Drone input row: wrong number of attritubes.");
                    System.exit(1);
                }
                
                // Initiate drones based on the input.
                droneInfo.add(new MovingObject());
                droneInfo.get(k).setMovingObjectID(k);
                droneInfo.get(k).setMovingObjectX(Integer.parseInt(tokens[0]));
                droneInfo.get(k).setMovingObjectY(Integer.parseInt(tokens[1]));
                droneInfo.get(k).setMovingObjectDirection(tokens[2]);
                droneInfo.get(k).setMovingObjectStrategy(Integer.parseInt(tokens[3]));
                droneInfo.get(k).setFuelLevel(regionWidth * regionHeight * FUEL_MULTIPLE);
                droneInfo.get(k).setMovingObjectStatus(OK_CODE);
                droneInfo.get(k).setIsUFO(false);
                
                // explore the stars at the initial location
                regionInfo.get(droneInfo.get(k).getMovingObjectX() * regionHeight + droneInfo.get(k).getMovingObjectY()).setRegionInfo(EMPTY_CODE);
                regionInfo.get(droneInfo.get(k).getMovingObjectX() * regionHeight + droneInfo.get(k).getMovingObjectY()).setRegionScanned(true);
            }

            // create the UFO starting information
            numberOfUFO = (regionWidth * regionHeight / UFO_FREQ);
            
            // Initate drones based on the region size.
            for (k = 0; k < numberOfUFO; k++) {
                ufoInfo.add(new MovingObject());
                ufoInfo.get(k).setMovingObjectID(k);
                ufoInfo.get(k).setMovingObjectX(randGenerator.nextInt(regionWidth));
                ufoInfo.get(k).setMovingObjectY(randGenerator.nextInt(regionHeight));
                ufoInfo.get(k).setMovingObjectDirection(ORIENT_LIST[randGenerator.nextInt(8)]);
                ufoInfo.get(k).setMovingObjectStrategy(0);
                ufoInfo.get(k).setMovingObjectStatus(OK_CODE);
                ufoInfo.get(k).setIsUFO(true);
            }
            
            // read in the sun information
            tokens = takeCommand.nextLine().split(DELIMITER);
            numSuns = Integer.parseInt(tokens[0]);
            for (k = 0; k < numSuns; k++) {
                tokens = takeCommand.nextLine().split(DELIMITER);
                if (tokens.length != 2) {
                    System.out.println("ERROR: Sun input row: wrong number of attritubes.");
                    System.exit(1);
                }

                // place a sun at the given location
                regionInfo.get(Integer.parseInt(tokens[0]) * regionHeight + Integer.parseInt(tokens[1])).setRegionInfo(SUN_CODE);
            }

            tokens = takeCommand.nextLine().split(DELIMITER);
            turnLimit = Integer.parseInt(tokens[0]);

            takeCommand.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
        }
    }

    // Error handling
    public void checkInputFile() {
        // Check number of drones
        if (numberOfDrones <= 0 || numberOfDrones > 10) {
            System.out.println("ERROR: Number of drones has to be between 1 and 10.\n");
            System.exit(1);
        }

        // Check number of UFO
        if (numberOfDrones <= 0 || numberOfUFO > 5) {
            System.out.println("ERROR: Number of UFO has to be between 1 and 5.\n");
            System.exit(1);
        }

        // Check number of turns
        if (turnLimit <= 0 || turnLimit > 200) {
            System.out.println("ERROR: Number of turns of simulation has to be between 1 and 200.\n");
            System.exit(1);
        }

        // Check number of row and columns;
        if (regionWidth <= 0 || regionWidth > 20) {
            System.out.println("ERROR: Width has to be between 1 and 20.\n");
            System.exit(1);
        }
        if (regionHeight <= 0 || regionHeight > 15) {
            System.out.println("ERROR: Height has to be between 1 and 15.\n");
            System.exit(1);
        }

        // Check number of suns
        if (numSuns < 0 || numSuns > regionWidth * regionHeight / 2) {
            System.out.println("ERROR: Number of suns has to be between 0 and 50% of space region.\n");
            System.exit(1);
        }
    }
    
    public Integer simulationDuration() {
        return turnLimit;
    }

    public int[] regionSize() {
        int[] size_arr = {regionWidth,regionHeight};
        return size_arr;
    }

    // Return if all stardusts are explored.
    public Boolean spaceAllExplored(int regionWidth, int regionHeight) {
        for(int i = 0; i < regionWidth; i++) {
            for(int j = 0; j < regionHeight; j++) {
                if (regionInfo.get(i * regionHeight + j).getRegionInfo() == STARS_CODE) { return Boolean.FALSE; }
            }
        }
        return Boolean.TRUE;
    }

    // Return if all drones are crashed.
    public Boolean dronesAllStopped() {
        for(int k = 0; k < numberOfDrones; k++) {
            if (droneInfo.get(k).getMovingObjectStatus() == OK_CODE) { return Boolean.FALSE; }
        }
        return Boolean.TRUE;
    }

    // Return if the given drone is crashed.
    public Boolean droneStopped(int id) {
        return droneInfo.get(id).getMovingObjectStatus() == CRASH_CODE;
    }

    // Return the number of drones.
    public Integer droneCount() {
        return numberOfDrones;
    }

    // Return the number of UFOs.
    public Integer ufoCount() {
        return numberOfUFO;
    }

    // Asking drone for action, and then take the action
    public void DroneAction(int id) {
        droneInfo.get(id).askMovingObjectForAction(droneInfo, ufoInfo, regionInfo, regionWidth, regionHeight);
        validateDroneAction(id);
    }

    // validate and take the drone's action
    public void validateDroneAction(int id) {
        trackAction = droneInfo.get(id).getAction();
        trackNewDirection = droneInfo.get(id).getNewDirection();
        trackThrustDistance = droneInfo.get(id).getThrustDistance();
        trackScanResults = "Energy exhausted";
        int xOrientation, yOrientation;

        // check if there are any nearby sun
        String sunForce = checkNearbySunDirection(id);
        int sunForceX = 0;
        int sunForceY = 0;
        if (sunForce != "none") {
            sunForceX = xDIR_MAP.get(sunForce);
            sunForceY = yDIR_MAP.get(sunForce);
        }
        
        // take action
        if (trackAction.equals("scan")) {
            // in the case of a scan, return the information for the eight surrounding squares
            // always use a northbound orientation
            if (droneInfo.get(id).getFuelLevel() - 2 < 0) {
                // if the drone has not sufficient fuel to scan, it will crash
                droneInfo.get(id).setMovingObjectStatus(CRASH_CODE);
            } else if (droneInfo.get(id).getFuelLevel() - 2 == 0) {
                // if the drone has not sufficient fuel after scan, it will return the scan result and crash afterward
                trackScanResults = scanAroundSquare(droneInfo.get(id).getMovingObjectX(), droneInfo.get(id).getMovingObjectY());
                droneInfo.get(id).setFuelLevel(droneInfo.get(id).getFuelLevel() - 2);
                droneInfo.get(id).setMovingObjectStatus(CRASH_CODE);
            } else if (droneInfo.get(id).getFuelLevel() - 2 > 0) {
                // if the drone has sufficient fuel, it will return the scan result
                trackScanResults = scanAroundSquare(droneInfo.get(id).getMovingObjectX(), droneInfo.get(id).getMovingObjectY());
                droneInfo.get(id).setFuelLevel(droneInfo.get(id).getFuelLevel() - 2);
            }
            if (sunForce != "none") {
                moveOneStep(id, sunForceX, sunForceY, false);
            }
            trackMoveCheck = "ok";

        } else if (trackAction.equals("pass")) {
            // Allow refuel when the drone decides to pass.
            droneInfo.get(id).setFuelLevel(droneInfo.get(id).getFuelLevel() + 15);
            if (sunForce != "none") {
                moveOneStep(id, sunForceX, sunForceY, false);
            }
            trackMoveCheck = "ok";

        } else if (trackAction.equals("steer")) {
            if (droneInfo.get(id).getFuelLevel() - 1 < 0) {
                // The drone will be crashed when there is no more fuel.
                droneInfo.get(id).setMovingObjectStatus(CRASH_CODE);
            } else if (droneInfo.get(id).getFuelLevel() - 1 == 0) {
                // if the drone has not sufficient fuel after steer, it will crash
                steerNewDirection(id, trackNewDirection, false);
                droneInfo.get(id).setFuelLevel(droneInfo.get(id).getFuelLevel() - 1);
                droneInfo.get(id).setMovingObjectStatus(CRASH_CODE);
            } else if (droneInfo.get(id).getFuelLevel() - 1 > 0) {
                // if the drone has sufficient fuel, it will steer
                steerNewDirection(id, trackNewDirection, false);
                droneInfo.get(id).setFuelLevel(droneInfo.get(id).getFuelLevel() - 1);
            }
            if (sunForce != "none") {
                moveOneStep(id, sunForceX, sunForceY, false);
            }
            trackMoveCheck = "ok";

        } else if (trackAction.equals("thrust")) {
            // Error handling if thrust distance is out of range.
            if (trackThrustDistance <= 0 || trackThrustDistance > 3) {
                trackMoveCheck = "action_not_recognized";
                return;
            }
            trackMoveCheck = "ok";

            // in the case of a thrust, ensure that the move doesn't cross suns or barriers
            xOrientation = xDIR_MAP.get(droneInfo.get(id).getMovingObjectDirection());
            yOrientation = yDIR_MAP.get(droneInfo.get(id).getMovingObjectDirection());

            int remainingThrust = trackThrustDistance;
            
            // check if sufficient fuel before and after thrust, will crash if not sufficient fuel
            while (remainingThrust > 0 && trackMoveCheck.equals("ok")) {
                if (droneInfo.get(id).getFuelLevel() - 1 < 0) {
                    droneInfo.get(id).setMovingObjectStatus(CRASH_CODE);
                } else if (droneInfo.get(id).getFuelLevel() - 1 == 0) {
                    moveOneStep(id, xOrientation, yOrientation, false);
                    droneInfo.get(id).setFuelLevel(droneInfo.get(id).getFuelLevel() - 1);
                    droneInfo.get(id).setMovingObjectStatus(CRASH_CODE);
                } else if (droneInfo.get(id).getFuelLevel() - 1 > 0) {
                    moveOneStep(id, xOrientation, yOrientation, false);
                    droneInfo.get(id).setFuelLevel(droneInfo.get(id).getFuelLevel() - 1);
                }
                remainingThrust = remainingThrust - 1;    
            }
            // the effect of sun gravitional pull happens after the thrust
            if (sunForce != "none" && trackMoveCheck == "ok") {
                moveOneStep(id, sunForceX, sunForceY, false);
            }
        } else {
            // in the case of an unknown action, treat the action as a pass
            trackMoveCheck = "action_not_recognized";
        }
    }

    // Ask UFO for action
    public void UFOAction(int id) {
        ufoInfo.get(id).askMovingObjectForAction(droneInfo, ufoInfo, regionInfo, regionWidth, regionHeight);
        validateUFOAction(id);
    }

    // validate and move the drone according to given action
    public void validateUFOAction(int id) {
        trackAction = ufoInfo.get(id).getAction();
        trackNewDirection = ufoInfo.get(id).getNewDirection();
        trackThrustDistance = ufoInfo.get(id).getThrustDistance();
        
        int xOrientation, yOrientation;

        if (trackAction.equals("attack")) {
            // UFO attacks surrounding square
            ufoInfo.get(id).setMovingObjectStatus(OK_CODE);
            attackAroundSquare(ufoInfo.get(id).getMovingObjectX(), ufoInfo.get(id).getMovingObjectY());
            trackMoveCheck = "ok";

        } else if (trackAction.equals("pass")) {
            // UFO switch to stealth mode
            ufoInfo.get(id).setMovingObjectStatus(STEALTH_CODE);
            trackMoveCheck = "ok";

        } else if (trackAction.equals("steer")) {
            // UFO change directon
            ufoInfo.get(id).setMovingObjectStatus(OK_CODE);
            steerNewDirection(id, trackNewDirection, true);
            trackMoveCheck = "ok";

        } else if (trackAction.equals("thrust")) {
            // UFO move forward
            ufoInfo.get(id).setMovingObjectStatus(OK_CODE);
            if (trackThrustDistance <= 0 || trackThrustDistance > 3) {
                trackMoveCheck = "action_not_recognized";
                return;
            }
            trackMoveCheck = "ok";

            // in the case of a thrust, ensure that the move doesn't cross suns or barriers
            xOrientation = xDIR_MAP.get(ufoInfo.get(id).getMovingObjectDirection());
            yOrientation = yDIR_MAP.get(ufoInfo.get(id).getMovingObjectDirection());

            int remainingThrust = trackThrustDistance;
            
            while (remainingThrust > 0 && trackMoveCheck.equals("ok")) {
                // move if there are remaining moves
                moveOneStep(id, xOrientation, yOrientation, true);
                remainingThrust = remainingThrust - 1;    
            }
        } else {
            // in the case of an unknown action, treat the action as a pass
            trackMoveCheck = "action_not_recognized";
        }
    }

    // Allow the UFO to destroy drones in the surrounding squares
    public void attackAroundSquare(int targetX, int targetY) {
        String nextSquare, resultString = "";

        for (int k = 0; k < ORIENT_LIST.length; k++) {
            String lookThisWay = ORIENT_LIST[k];
            int offsetX = xDIR_MAP.get(lookThisWay);
            int offsetY = yDIR_MAP.get(lookThisWay);

            int checkX = (targetX + offsetX + regionWidth) % regionWidth;;
            int checkY = (targetY + offsetY + regionHeight) % regionHeight;
            
            // attack drone next to it
            for (int j = 0; j < numberOfDrones; j++) {
                // check if there are drones in the surrounding square; if yes, destroy it
                if (droneInfo.get(j).getMovingObjectStatus() == OK_CODE && checkX == droneInfo.get(j).getMovingObjectX() && checkY == droneInfo.get(j).getMovingObjectY()) {
                    droneInfo.get(j).setMovingObjectStatus(CRASH_CODE);
                    regionInfo.get(droneInfo.get(j).getMovingObjectX() * regionHeight + droneInfo.get(j).getMovingObjectY()).setUFOSuspected(true);
                    System.out.println("UFO attacks and destroys drone " + j + ".");
                }
            }
        }
    }

    // Scan and check the details of surrounding squares for a given location
    public String scanAroundSquare(int targetX, int targetY) {
        String nextSquare, resultString = "";

        for (int k = 0; k < ORIENT_LIST.length; k++) {
            String lookThisWay = ORIENT_LIST[k];
            int offsetX = xDIR_MAP.get(lookThisWay);
            int offsetY = yDIR_MAP.get(lookThisWay);

            int checkX = (targetX + offsetX + regionWidth) % regionWidth;;
            int checkY = (targetY + offsetY + regionHeight) % regionHeight;
            regionInfo.get(checkX * regionHeight + checkY).setRegionScanned(true);
            
            // check if there are drones in the surrounding square; if yes, make TRUE
            boolean have_drones = false;
            for (int j = 0; j < numberOfDrones; j++) {
                if (droneInfo.get(j).getMovingObjectStatus() == OK_CODE && checkX == droneInfo.get(j).getMovingObjectX() && checkY == droneInfo.get(j).getMovingObjectY()) {
                    have_drones = true;
                }
            }

            // check if there are UFOs in the surrounding square; if yes, make TRUE
            boolean have_UFOs = false;
            for (int j = 0; j < numberOfUFO; j++) {
                if (ufoInfo.get(j).getMovingObjectStatus() == OK_CODE && checkX == ufoInfo.get(j).getMovingObjectX() && checkY == ufoInfo.get(j).getMovingObjectY()) {
                    regionInfo.get(checkX * regionHeight + checkY).setUFOSuspected(true);
                    have_UFOs = true;
                }
            }

            // return the stuffs in a certain surround square
            if (have_drones) {
                nextSquare = "drone";
            } else if (have_UFOs) {
                nextSquare = "UFO";
            } else {
                switch (regionInfo.get(checkX * regionHeight + checkY).getRegionInfo()) {
                    case EMPTY_CODE:
                        nextSquare = "empty";
                        break;
                    case STARS_CODE:
                        nextSquare = "stars";
                        break;
                    case SUN_CODE:
                        nextSquare = "sun";
                        break;
                    default:
                        nextSquare = "unknown";
                        break;
                }
            }

            if (resultString.isEmpty()) { resultString = nextSquare; }
            else { resultString = resultString + "," + nextSquare; }
        }

        return resultString;
    }

    // Check to see the direction of the sun for a given drone.
    public String checkNearbySunDirection(int id) {
        String resultString = "none";
        
        int targetX = droneInfo.get(id).getMovingObjectX();
        int targetY = droneInfo.get(id).getMovingObjectY();

        for (int k = 0; k < ORIENT_LIST.length; k++) {
        
            // initialize variable
            String lookThisWay = ORIENT_LIST[k];
            int offsetX = xDIR_MAP.get(lookThisWay);
            int offsetY = yDIR_MAP.get(lookThisWay);
            int checkX = (targetX + offsetX + regionWidth) % regionWidth;
            int checkY = (targetY + offsetY + regionHeight) % regionHeight;
            
            // check if have sun
            if (regionInfo.get(checkX * regionHeight + checkY).getRegionInfo() == SUN_CODE) {
                resultString = ORIENT_LIST[k];
            }
        }
        
        return resultString;
    }

    // Change the direction of a given moving object.
    public void steerNewDirection(int id, String trackNewDirection, boolean isUFO) {
        boolean contains_valid_steer = false;
        for (int i = 0; i < 8; i++) {
            contains_valid_steer = contains_valid_steer || ORIENT_LIST[i].equals(trackNewDirection);
        }
        if (!contains_valid_steer) {
            trackMoveCheck = "action_not_recognized";
            return;
        }
        
        // steer the corresponding object
        if (isUFO) {
            ufoInfo.get(id).setMovingObjectDirection(trackNewDirection);
        } else {
            droneInfo.get(id).setMovingObjectDirection(trackNewDirection);
        }
    }

    // For printing results
    public void displayActionAndResponses(int id, String objectName, String playerLevel) {
        // display the line for the drone
        if (playerLevel.equals("easy") || (playerLevel.equals("hard") && objectName.equals("Drone "))){
            System.out.print(objectName + String.valueOf(id) + ": " + trackAction);
        }
        if (trackAction.equals("steer")) {
            // display the line for the drone's steer acton
            if (playerLevel.equals("easy") || (playerLevel.equals("hard") && objectName.equals("Drone "))){
                System.out.println(" to " + trackNewDirection);
            }
        } else if (trackAction.equals("thrust")) {
            // display the line for the drone's thrust acton
            if (objectName.equals("Drone ")) {
                System.out.println(" towards " + droneInfo.get(id).getMovingObjectDirection() + " by " + trackThrustDistance + " step(s).");
            }
            // display the line for the UFO's thrust acton
            if (playerLevel.equals("easy") && objectName.equals("UFO ")) {
                System.out.println(" towards " + ufoInfo.get(id - numberOfDrones).getMovingObjectDirection() + " by " + trackThrustDistance + " step(s).");
            }
        } else {
            System.out.println();
        }

        // display the simulation checks and/or responses
        if (trackAction.equals("thrust") || trackAction.equals("steer") || trackAction.equals("pass") || trackAction.equals("attack")) {
            return;
        } else if (trackAction.equals("scan")) {
            System.out.println("Scan result: " + trackScanResults);
        } else {
            System.out.println("Action not recognized");
        }
    }

    // Move the object one step forward in given direction
    public void moveOneStep(int id, int xOrientation, int yOrientation, boolean isUFO) {
        trackMoveCheck = "ok";
        if (isUFO) {
            // move a UFO
            int newSquareX = (ufoInfo.get(id).getMovingObjectX() + xOrientation + regionWidth) % regionWidth;
            int newSquareY = (ufoInfo.get(id).getMovingObjectY() + yOrientation + regionHeight) % regionHeight;
            
            if (trackMoveCheck != "crash") {
                // check if UFO crash into drone
                for (int k = 0; k < numberOfDrones; k++) {
                    if (droneInfo.get(k).getMovingObjectStatus() == OK_CODE & newSquareX == droneInfo.get(k).getMovingObjectX() && newSquareY == droneInfo.get(k).getMovingObjectY()){
                        regionInfo.get(droneInfo.get(k).getMovingObjectX() * regionHeight + droneInfo.get(k).getMovingObjectY()).setUFOSuspected(true);
                        droneInfo.get(k).setMovingObjectStatus(CRASH_CODE);
                        System.out.println("A UFO crashes into and destroys drone " + k + ".");
                    }
                }
            }
            if (trackMoveCheck != "crash") {
                // UFO thrust is successful
                ufoInfo.get(id).setMovingObjectX(newSquareX);
                ufoInfo.get(id).setMovingObjectY(newSquareY);
            }
        } else {
            // move a drone
            int newSquareX = (droneInfo.get(id).getMovingObjectX() + xOrientation + regionWidth) % regionWidth;
            int newSquareY = (droneInfo.get(id).getMovingObjectY() + yOrientation + regionHeight) % regionHeight;
            regionInfo.get(newSquareX * regionHeight + newSquareY).setRegionScanned(true);

            if (regionInfo.get(newSquareX * regionHeight + newSquareY).getRegionInfo() == SUN_CODE) {
                // drone hit a sun
                droneInfo.get(id).setMovingObjectStatus(CRASH_CODE);
                trackMoveCheck = "crash";
                System.out.println("Drone " + id + " has been vaporized by sun.");
            }
            if (trackMoveCheck != "crash") {
                // drone collided with the other drone
                for (int k = 0; k < numberOfDrones; k++) {
                    if (k != id & droneInfo.get(k).getMovingObjectStatus() == OK_CODE & newSquareX == droneInfo.get(k).getMovingObjectX() && newSquareY == droneInfo.get(k).getMovingObjectY()){
                        droneInfo.get(id).setMovingObjectStatus(CRASH_CODE);
                        droneInfo.get(k).setMovingObjectStatus(CRASH_CODE);
                        trackMoveCheck = "crash";
                        System.out.println("Drone " + id + " crashes with drone " + k + ".");
                    }
                }
                // drone collided with the UFO
                for (int k = 0; k < numberOfUFO; k++) {
                    if (newSquareX == ufoInfo.get(k).getMovingObjectX() && newSquareY == ufoInfo.get(k).getMovingObjectY()){
                        regionInfo.get(ufoInfo.get(k).getMovingObjectX() * regionHeight + ufoInfo.get(k).getMovingObjectY()).setUFOSuspected(true);
                        droneInfo.get(id).setMovingObjectStatus(CRASH_CODE);
                        trackMoveCheck = "crash";
                        System.out.println("Drone " + id + " crashes into a UFO and the drone is destroyed.");
                    }
                }
            }
            if (trackMoveCheck != "crash") {
                // drone thrust is successful
                droneInfo.get(id).setMovingObjectX(newSquareX);
                droneInfo.get(id).setMovingObjectY(newSquareY);
                // update region status
                regionInfo.get(newSquareX * regionHeight + newSquareY).setRegionInfo(EMPTY_CODE);
                regionInfo.get(droneInfo.get(id).getMovingObjectX() * regionHeight + droneInfo.get(id).getMovingObjectY()).setUFOSuspected(false);
            }
        }

    }

    public void renderRegion(String playerLevel) {
        int i, j;
        int charWidth = 2 * regionWidth + 2;

        // display the rows of the region from top to bottom
        for (j = regionHeight - 1; j >= 0; j--) {
            renderHorizontalBar(charWidth);

            // display the Y-direction identifier
            System.out.print(j);

            // display the contents of each square on this row
            for (i = 0; i < regionWidth; i++) {
                System.out.print("|");

                // check if there are drones nearby; if YES, mark down the ID
                boolean have_drones = false;
                int drone_id = -1;
                for (int k = 0; k < numberOfDrones; k++) {
                    if (droneInfo.get(k).getMovingObjectStatus() == OK_CODE && i == droneInfo.get(k).getMovingObjectX() && j == droneInfo.get(k).getMovingObjectY()) {
                        have_drones = true;
                        drone_id = k;
                    }
                }
                // check if there are UFO nearby; if YES, mark down the ID
                boolean have_UFOs = false;
                int ufo_id = -1;
                for (int k = 0; k < numberOfUFO; k++) {
                    if (i == ufoInfo.get(k).getMovingObjectX() && j == ufoInfo.get(k).getMovingObjectY()) {
                        have_UFOs = true;
                        ufo_id = numberOfDrones + k;
                    }
                }
                
                // printing of drone ID (only if easy) and drone ID
                if (have_drones) {
                    System.out.print(drone_id);
                } else if (playerLevel.equals("easy") && have_UFOs) {
                    System.out.print(ufo_id);
                } else if (playerLevel.equals("hard") && regionInfo.get(i * regionHeight + j).getUFOSuspected()) {
                    // (hard mode) UFO suspected
                    if (regionInfo.get(i * regionHeight + j).getRegionInfo() == STARS_CODE) {
                        System.out.print("!"); // stars + UFO suspected
                    }
                    if (regionInfo.get(i * regionHeight + j).getRegionInfo() == EMPTY_CODE) {
                        System.out.print("?"); // explored + UFO suspected
                    }
                } else {
                    // print out empty / start / sun (only when it is easy, or it is hard and exploreded)
                    switch (regionInfo.get(i * regionHeight + j).getRegionInfo()) {
                        case EMPTY_CODE:
                            System.out.print(" ");
                            break;
                        case STARS_CODE:
                            System.out.print(".");
                            break;
                        case SUN_CODE:
                            if (playerLevel.equals("easy") || regionInfo.get(i * regionHeight + j).getRegionScanned()) {
                                System.out.print("s");
                            } else {
                                System.out.print(".");
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            System.out.println("|");
        }
        renderHorizontalBar(charWidth);

        // display the column X-direction identifiers
        System.out.print(" ");
        for (i = 0; i < regionWidth; i++) {
            System.out.print(" " + i);
        }
        System.out.println("");

        // display the drone's directions
        for(int k = 0; k < numberOfDrones; k++) {
            // TODO
            String res = droneInfo.get(k).getMovingObjectStatus() == CRASH_CODE ? "crashed": "alive"; 
            System.out.println("[Drone " + String.valueOf(k) + "] Direction: " + droneInfo.get(k).getMovingObjectDirection() + "; Fuel: " + droneInfo.get(k).getFuelLevel() + "; Status: " + res);
        }
        for(int k = 0; k < numberOfUFO; k++) {
            // calculuate status of UFO
            String ufoStatus = "normal";
            if (ufoInfo.get(k).getMovingObjectStatus() == OK_CODE) {
                ufoStatus = "normal";
           } else if (ufoInfo.get(k).getMovingObjectStatus() == STEALTH_CODE) {
                ufoStatus = "stealth";
            }
            // print of status of the UFO if in easy mode
            if (playerLevel.equals("easy")) {
                System.out.println("[UFO   " + String.valueOf(numberOfDrones + k) + "] Direction: " + ufoInfo.get(k).getMovingObjectDirection() + "; Visibility: " + ufoStatus);
            }
        }
        System.out.println("");
    }

    private void renderHorizontalBar(int size) {
        System.out.print(" ");
        for (int k = 0; k < size; k++) {
            System.out.print("-");
        }
        System.out.println("");
    }

    public void finalReport(int completeTurns) {
        // initialize variable
        int regionSize = regionWidth * regionHeight;
        int numSuns = 0;
        int numStars = 0;
        
        // calculate the number of star
        for (int i = 0; i < regionWidth; i++) {
            for (int j = 0; j < regionHeight; j++) {
                if (regionInfo.get(i * regionHeight + j).getRegionInfo() == SUN_CODE) { numSuns++; }
                if (regionInfo.get(i * regionHeight + j).getRegionInfo() == STARS_CODE) { numStars++; }
            }
        }

        // calculate and output report
        int potentialCut = regionSize - numSuns;
        int actualCut = potentialCut - numStars;
        System.out.println("=============================");
        System.out.println("Total region size: " + String.valueOf(regionSize));
        System.out.println("Region need to be explored: " + String.valueOf(potentialCut));
        System.out.println("Region explored: " + String.valueOf(actualCut));
        System.out.println("Turns completed: " + String.valueOf(completeTurns));
    }

}
