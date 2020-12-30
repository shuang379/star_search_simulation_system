import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        SimManager monitorSim = new SimManager();
        int trackTurnsCompleted = 0;
        Boolean showState = Boolean.FALSE;
        Scanner askUser = new Scanner(System.in);
        
        // Inputting next will complete the action for the current drone and move to the next drone
        String playerControl = "next";
        
        // Easy level will be able to see the exact positions of UFO as well as SUNs, 
        // while hard will only be able to see drones and stardusts in the map.
        String playerLevel = "easy";
        System.out.println("Please enter the difficulties (easy / hard): ");
        
        // Santize input to allow "Easy", "EASy" and etc.
        playerLevel = askUser.nextLine().toLowerCase();

        // Error handling
        while (!(playerLevel.equals("easy") || playerLevel.equals("hard"))) {
            System.out.println("The diffculties should be either (easy) or (hard). Please enter again: ");
            playerLevel = askUser.nextLine();
        }
        System.out.println("Player level is set to " + playerLevel);

        // check for the test scenario file name
        if (args.length < 1) {
            System.out.println("ERROR: Test scenario file name not found.");
            return;
        }
        if (args.length >= 2 && (args[1].equals("-v") || args[1].equals("-verbose"))) { showState = Boolean.TRUE; }

        monitorSim.uploadStartingFile(args[0]);
        monitorSim.checkInputFile();
        
        // Display initial map setting as well as the keys.
        System.out.println("====== Display initial map setting ======");
        if (showState) { monitorSim.renderRegion(playerLevel); }
        System.out.println("====== KEYS ======");
        System.out.println(". : stars");
        System.out.println("s : suns");
        System.out.println("  : explored");

        // Hard level will not be able to see the exact position of the SUNs and UFOs.
        if (playerLevel.equals("hard")) {
            System.out.println("! : stars + UFO suspected");
            System.out.println("? : explored + UFO suspected");
        }
        System.out.println(" ");
                        
        // run the simulation for a fixed number of steps
        for(int turns = 0; turns < monitorSim.simulationDuration(); turns++) {
            trackTurnsCompleted = turns;
            
            // Game will stop if all the star dusts are being explored.
            if (monitorSim.spaceAllExplored(monitorSim.regionSize()[0], monitorSim.regionSize()[1])) { break; }

            // Game will stop if all drones are crashed.
            if (monitorSim.dronesAllStopped()) { break; }

            // Player drones.
            for (int k = 0; k < monitorSim.droneCount(); k++) {

                // Game will stop if all the star dusts are being explored.
                if (monitorSim.spaceAllExplored(monitorSim.regionSize()[0], monitorSim.regionSize()[1])) { break; }
                
                // Run only if this drone is not crashed.
                if (!monitorSim.droneStopped(k)) {

                    // Display which drone is in action.
                    System.out.println("========== TURN " + turns + "; Drone " + k + " ============");
                    if (playerControl.equals("next")) {

                        System.out.println("Please select next action (Next / Fast-Forward / Stop): ");

                        // Sanitize player input.
                        playerControl = askUser.nextLine().toLowerCase();

                        // Error handling.
                        while (!(playerControl.equals("next") || playerControl.equals("fast-forward") || playerControl.equals("stop"))) {
                            System.out.println("The action should be either (Next) or (Fast-Forward) or (Stop). Please enter again: ");
                            playerControl = askUser.nextLine().toLowerCase();
                        }

                        // Halt and print the report if player chooses to stop.
                        if (playerControl.equals("stop")) {
                            monitorSim.finalReport(trackTurnsCompleted);
                            return;
                        }
                        
                        System.out.println("-----------------------------");
                    }
                    
                    // Determine drone's action
                    monitorSim.DroneAction(k);
                    monitorSim.displayActionAndResponses(k, "Drone ", playerLevel);

                    // render the state of the space region after each command
                    if (showState) { monitorSim.renderRegion(playerLevel); }
                }
            }

            // UFOs.
            if (monitorSim.ufoCount() > 0) { System.out.println("======= UFO is coming ======="); }
            
            for (int k = 0; k < monitorSim.ufoCount(); k++) {

                // Determine UFO's action.
                monitorSim.UFOAction(k);
                monitorSim.displayActionAndResponses(monitorSim.droneCount() + k, "UFO ", playerLevel);

                // render the state of the space region after each command
                if (showState) { monitorSim.renderRegion(playerLevel); }
            }

        }

        // Print the final report.
        monitorSim.finalReport(trackTurnsCompleted);
    }

}
