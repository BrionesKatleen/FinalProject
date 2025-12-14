//////package database;
//////
//////public class Main {
//////    public static void main(String[] args) {
//////        new DatabaseManager();
//////        System.out.println("Program finished successfully!");
//////    }
//////}
////package database;
////
////import java.sql.ResultSet;
////import java.sql.SQLException;
////
////public class Main {
////    public static void main(String[] args) {
////        DatabaseManager db = new DatabaseManager();
////
////        // 1️⃣ Create a new player
////        String username = "TestPlayer";
////        db.createPlayer(username);
////        int playerId = db.getPlayerId(username);
////        System.out.println("Player created with ID: " + playerId);
////
////        // 2️⃣ Create a duck for the player
////        db.createDuck(playerId, "Quacky");
////        System.out.println("Duck created for player " + username);
////
////        // 3️⃣ Give the player a hat (Frog)
////        int frogHatId = 1; // ID from default hats
////        db.giveHatToPlayer(playerId, frogHatId);
////        db.equipHat(playerId, frogHatId);
////        System.out.println("Gave Frog hat and equipped it.");
////
////        // 4️⃣ Give the player some food (Peas)
////        int peasFoodId = 1; // ID from default foods
////        db.addFoodToPlayer(playerId, peasFoodId, 5);
////        System.out.println("Added 5 Peas to player inventory.");
////
////        // 5️⃣ Update duck stats (simulate eating food)
////        db.updateDuckStats(1, 100.5, 100.5, 99.95, 100.05); // hunger +0.5, energy +0.5, cleanliness -0.05, happiness +0.05
////        System.out.println("Updated duck stats after eating.");
////
////        // 6️⃣ Read duck info and print
////        try (ResultSet rs = db.getDuck(playerId)) {
////            while (rs != null && rs.next()) {
////                System.out.println("Duck info:");
////                System.out.println("Name: " + rs.getString("name"));
////                System.out.println("Hunger: " + rs.getDouble("hunger"));
////                System.out.println("Energy: " + rs.getDouble("energy"));
////                System.out.println("Cleanliness: " + rs.getDouble("cleanliness"));
////                System.out.println("Happiness: " + rs.getDouble("happiness"));
////            }
////        } catch (SQLException e) {
////            e.printStackTrace();
////        }
////
////        System.out.println("CRUD test completed successfully!");
////    }
////}
////package database;
////
////import java.sql.ResultSet;
////import java.sql.SQLException;
////
////public class Main {
////
////    private static double clamp(double value) {
////        return Math.max(0, Math.min(100, value));
////    }
////
////    public static void main(String[] args) {
////
////        DatabaseManager db = new DatabaseManager();
////        db.initialize(); // tables + defaults ONLY ONCE
////
////        // 1️⃣ Create player
////        String username = "Player1";
////        db.createPlayer(username);
////        int playerId = db.getPlayerId(username);
////        System.out.println("Player ID: " + playerId);
////
////        // 2️⃣ Create duck
////        db.createDuck(playerId, "Quacky1");
////        int duckId = db.getDuckId(playerId);
////        System.out.println("Duck ID: " + duckId);
////
////        // 3️⃣ Simulate eating (values BEFORE clamp)
////        double hunger = clamp(100 + 0.5);
////        double energy = clamp(100 + 0.5);
////        double cleanliness = clamp(100 - 0.05);
////        double happiness = clamp(100 + 0.05);
////
////        db.updateDuckStats(duckId, hunger, energy, cleanliness, happiness);
////        System.out.println("Duck stats updated (clamped 0–100)");
////
////        // 4️⃣ Read duck back
////        try (ResultSet rs = db.getDuck(playerId)) {
////            while (rs != null && rs.next()) {
////                System.out.println("Duck Info:");
////                System.out.println("Name: " + rs.getString("name"));
////                System.out.println("Hunger: " + rs.getDouble("hunger"));
////                System.out.println("Energy: " + rs.getDouble("energy"));
////                System.out.println("Cleanliness: " + rs.getDouble("cleanliness"));
////                System.out.println("Happiness: " + rs.getDouble("happiness"));
////            }
////        } catch (SQLException e) {
////            e.printStackTrace();
////        }
////
////        System.out.println("Database test completed successfully!");
////    }
////}
////package database;
////public class Main {
////public static void main(String[] args) throws Exception {
////    Database db = new Database();
////    int playerId = db.createUser("test", "1234");
////    System.out.println("Player ID: " + playerId);
////}
////}
//package database;
//
//import backend.models.User;
//
//public class Main {
//    public static void main(String[] args) {
//        try {
//            // Initialize database
//            Database db = new Database();
//
//            // Create a new user
//            int playerId = db.createUser("test", "1234");
//            System.out.println("Player created! ID: " + playerId);
//
//            // Fetch the user we just created
//            User user = db.getUserByUsername("test");
//            if (user != null) {
//                System.out.println("User fetched: " + user.getUsername() + ", Coins: " + user.getCoins());
//            }
//
//            // Close database connection
//            db.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
//package database;
//
//import backend.models.User;
//
//public class Main {
//    public static void main(String[] args) throws Exception {
//        Database db = new Database();
//
//        String username = "test";
//        String password = "1234";
//
//        // Check if user exists first
//        if (!db.userExists(username)) {
//            int playerId = db.createUser(username, String.valueOf(password.hashCode()));
//            System.out.println("Player ID: " + playerId);
//        }
//
//        // Get user with hashed password
//        User user = db.getUser(username, String.valueOf(password.hashCode()));
//        if (user != null) {
//            System.out.println("Logged in as: " + user.getUsername() + " (ID: " + user.getId() + ")");
//        } else {
//            System.out.println("User not found!");
//        }
//
//        db.close();
//    }
//}
//package database;
//
//import backend.models.Duck;
//import backend.models.User;
//
//public class Main {
//    public static void main(String[] args) {
//        GameManager gameManager = new GameManager();
//
//        // Register a new user (if username doesn't exist)
//        String username = "test";
//        String password = "1234";
//
//        if (!gameManager.register(username, password)) {
//            System.out.println("User already exists, trying to login...");
//        }
//
//        // Login the user
//        if (gameManager.login(username, password)) {
//            System.out.println("Login successful!");
//            User currentUser = gameManager.getUser();
//            Duck currentDuck = gameManager.getDuck();
//
//            System.out.println("Player: " + currentUser.getUsername());
//            System.out.println("Duck: " + currentDuck.getStatusMessage());
//
//            // Example actions
//            gameManager.printDuckStatus();
//            gameManager.feedDuck(1);   // Make sure food with ID 1 exists in DB
//            gameManager.playWithDuck();
//            gameManager.cleanDuck();
//            gameManager.putDuckToSleep();
//
//            gameManager.printDuckStatus();
//        } else {
//            System.err.println("Login failed!");
//        }
//
//        // Shutdown properly
//        gameManager.shutdown();
//    }
//}
package database;

// Import GameManager from its package
import backend.services.GameManager;
import backend.models.Duck;
import backend.models.User;

public class Main {
    public static void main(String[] args) {
        GameManager gameManager = new GameManager();

        // Register a new user (if username doesn't exist)
        String username = "test";
        String password = "1234";

        if (!gameManager.register(username, password)) {
            System.out.println("User already exists, trying to login...");
        }

        // Login the user
        if (gameManager.login(username, password)) {
            System.out.println("Login successful!");
            User currentUser = gameManager.getUser();
            Duck currentDuck = gameManager.getDuck();

            System.out.println("Player: " + currentUser.getUsername());
            System.out.println("Duck: " + currentDuck.getStatusMessage());

            // Example actions
            gameManager.printDuckStatus();
            gameManager.feedDuck(1);   // Make sure food with ID 1 exists in DB
            gameManager.playWithDuck();
            gameManager.cleanDuck();
            gameManager.putDuckToSleep();

            gameManager.printDuckStatus();
        } else {
            System.err.println("Login failed!");
        }

        // Shutdown properly
        gameManager.shutdown();
    }
}


