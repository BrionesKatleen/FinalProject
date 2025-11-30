package database;

public class Main {
    public static void main(String[] args) {

        // Initialize database and tables
        DatabaseManager.initialize();

        // Create a player and a duck
        int playerId = DatabaseManager.addPlayer("Grace");
        int duckId = DatabaseManager.addDuck(playerId);

        System.out.println("Player ID: " + playerId);
        System.out.println("Duck ID: " + duckId);

        // Start the automatic stat update every minute
        DatabaseManager.startStatTimer(duckId);

        // Example: change duck state to playing
        DatabaseManager.setDuckState(duckId, "playing");

        // Keep program alive
        while (true) {
            // just to keep the main thread running
        }
    }
}
