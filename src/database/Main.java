package database;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        // Initialize database (drops old one automatically)
        DatabaseManager.initialize();

        // Create player
        int playerId = DatabaseManager.addPlayer("Grace");
        if (playerId == -1) {
            System.out.println("Failed to create player. Exiting.");
            return;
        }

        // Create duck/s
        int duckId = DatabaseManager.addDuck(playerId);
        if (duckId == -1) {
            System.out.println("Failed to create duck. Exiting.");
            return;
        }

        System.out.println("Player ID: " + playerId);
        System.out.println("Duck ID: " + duckId);

        // Start auto stat updates
        DatabaseManager.startStatTimer(duckId);

        // Example: change duck state to playing
        DatabaseManager.setDuckState(duckId, "playing");

        // Keep program alive
        Thread.sleep(Long.MAX_VALUE);
    }
}
