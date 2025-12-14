////////package database;
////////
////////public class Main {
////////    public static void main(String[] args) {
////////        new DatabaseManager();
////////        System.out.println("Program finished successfully!");
////////    }
////////}
//////package database;
//////
//////import java.sql.ResultSet;
//////import java.sql.SQLException;
//////
//////public class Main {
//////    public static void main(String[] args) {
//////        DatabaseManager db = new DatabaseManager();
//////
//////        // 1️⃣ Create a new player
//////        String username = "TestPlayer";
//////        db.createPlayer(username);
//////        int playerId = db.getPlayerId(username);
//////        System.out.println("Player created with ID: " + playerId);
//////
//////        // 2️⃣ Create a duck for the player
//////        db.createDuck(playerId, "Quacky");
//////        System.out.println("Duck created for player " + username);
//////
//////        // 3️⃣ Give the player a hat (Frog)
//////        int frogHatId = 1; // ID from default hats
//////        db.giveHatToPlayer(playerId, frogHatId);
//////        db.equipHat(playerId, frogHatId);
//////        System.out.println("Gave Frog hat and equipped it.");
//////
//////        // 4️⃣ Give the player some food (Peas)
//////        int peasFoodId = 1; // ID from default foods
//////        db.addFoodToPlayer(playerId, peasFoodId, 5);
//////        System.out.println("Added 5 Peas to player inventory.");
//////
//////        // 5️⃣ Update duck stats (simulate eating food)
//////        db.updateDuckStats(1, 100.5, 100.5, 99.95, 100.05); // hunger +0.5, energy +0.5, cleanliness -0.05, happiness +0.05
//////        System.out.println("Updated duck stats after eating.");
//////
//////        // 6️⃣ Read duck info and print
//////        try (ResultSet rs = db.getDuck(playerId)) {
//////            while (rs != null && rs.next()) {
//////                System.out.println("Duck info:");
//////                System.out.println("Name: " + rs.getString("name"));
//////                System.out.println("Hunger: " + rs.getDouble("hunger"));
//////                System.out.println("Energy: " + rs.getDouble("energy"));
//////                System.out.println("Cleanliness: " + rs.getDouble("cleanliness"));
//////                System.out.println("Happiness: " + rs.getDouble("happiness"));
//////            }
//////        } catch (SQLException e) {
//////            e.printStackTrace();
//////        }
//////
//////        System.out.println("CRUD test completed successfully!");
//////    }
//////}
//////package database;
//////
//////import java.sql.ResultSet;
//////import java.sql.SQLException;
//////
//////public class Main {
//////
//////    private static double clamp(double value) {
//////        return Math.max(0, Math.min(100, value));
//////    }
//////
//////    public static void main(String[] args) {
//////
//////        DatabaseManager db = new DatabaseManager();
//////        db.initialize(); // tables + defaults ONLY ONCE
//////
//////        // 1️⃣ Create player
//////        String username = "Player1";
//////        db.createPlayer(username);
//////        int playerId = db.getPlayerId(username);
//////        System.out.println("Player ID: " + playerId);
//////
//////        // 2️⃣ Create duck
//////        db.createDuck(playerId, "Quacky1");
//////        int duckId = db.getDuckId(playerId);
//////        System.out.println("Duck ID: " + duckId);
//////
//////        // 3️⃣ Simulate eating (values BEFORE clamp)
//////        double hunger = clamp(100 + 0.5);
//////        double energy = clamp(100 + 0.5);
//////        double cleanliness = clamp(100 - 0.05);
//////        double happiness = clamp(100 + 0.05);
//////
//////        db.updateDuckStats(duckId, hunger, energy, cleanliness, happiness);
//////        System.out.println("Duck stats updated (clamped 0–100)");
//////
//////        // 4️⃣ Read duck back
//////        try (ResultSet rs = db.getDuck(playerId)) {
//////            while (rs != null && rs.next()) {
//////                System.out.println("Duck Info:");
//////                System.out.println("Name: " + rs.getString("name"));
//////                System.out.println("Hunger: " + rs.getDouble("hunger"));
//////                System.out.println("Energy: " + rs.getDouble("energy"));
//////                System.out.println("Cleanliness: " + rs.getDouble("cleanliness"));
//////                System.out.println("Happiness: " + rs.getDouble("happiness"));
//////            }
//////        } catch (SQLException e) {
//////            e.printStackTrace();
//////        }
//////
//////        System.out.println("Database test completed successfully!");
//////    }
//////}
//////package database;
//////public class Main {
//////public static void main(String[] args) throws Exception {
//////    Database db = new Database();
//////    int playerId = db.createUser("test", "1234");
//////    System.out.println("Player ID: " + playerId);
//////}
//////}
////package database;
////
////import backend.models.User;
////
////public class Main {
////    public static void main(String[] args) {
////        try {
////            // Initialize database
////            Database db = new Database();
////
////            // Create a new user
////            int playerId = db.createUser("test", "1234");
////            System.out.println("Player created! ID: " + playerId);
////
////            // Fetch the user we just created
////            User user = db.getUserByUsername("test");
////            if (user != null) {
////                System.out.println("User fetched: " + user.getUsername() + ", Coins: " + user.getCoins());
////            }
////
////            // Close database connection
////            db.close();
////
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
////}
////package database;
////
////import backend.models.User;
////
////public class Main {
////    public static void main(String[] args) throws Exception {
////        Database db = new Database();
////
////        String username = "test";
////        String password = "1234";
////
////        // Check if user exists first
////        if (!db.userExists(username)) {
////            int playerId = db.createUser(username, String.valueOf(password.hashCode()));
////            System.out.println("Player ID: " + playerId);
////        }
////
////        // Get user with hashed password
////        User user = db.getUser(username, String.valueOf(password.hashCode()));
////        if (user != null) {
////            System.out.println("Logged in as: " + user.getUsername() + " (ID: " + user.getId() + ")");
////        } else {
////            System.out.println("User not found!");
////        }
////
////        db.close();
////    }
////}
////package database;
////
////import backend.models.Duck;
////import backend.models.User;
////
////public class Main {
////    public static void main(String[] args) {
////        GameManager gameManager = new GameManager();
////
////        // Register a new user (if username doesn't exist)
////        String username = "test";
////        String password = "1234";
////
////        if (!gameManager.register(username, password)) {
////            System.out.println("User already exists, trying to login...");
////        }
////
////        // Login the user
////        if (gameManager.login(username, password)) {
////            System.out.println("Login successful!");
////            User currentUser = gameManager.getUser();
////            Duck currentDuck = gameManager.getDuck();
////
////            System.out.println("Player: " + currentUser.getUsername());
////            System.out.println("Duck: " + currentDuck.getStatusMessage());
////
////            // Example actions
////            gameManager.printDuckStatus();
////            gameManager.feedDuck(1);   // Make sure food with ID 1 exists in DB
////            gameManager.playWithDuck();
////            gameManager.cleanDuck();
////            gameManager.putDuckToSleep();
////
////            gameManager.printDuckStatus();
////        } else {
////            System.err.println("Login failed!");
////        }
////
////        // Shutdown properly
////        gameManager.shutdown();
////    }
////}
//package database;
//
//// Import GameManager from its package
//import backend.services.GameManager;
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
//
package database;

import backend.models.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("🧪 DATABASE TESTER STARTING...\n");

        Database db = null;

        try {
            // 1. Initialize Database
            System.out.println("1️⃣ Creating Database connection...");
            db = new Database();
            System.out.println("✅ Database initialized successfully!\n");

            // 2. Test User Operations
            System.out.println("2️⃣ Testing User Operations...");
            testUserOperations(db);

            // 3. Test Duck Operations
            System.out.println("3️⃣ Testing Duck Operations...");
            testDuckOperations(db);

            // 4. Test Shop Operations
            System.out.println("4️⃣ Testing Shop Operations...");
            testShopOperations(db);

            // 5. Test Inventory Operations
            System.out.println("5️⃣ Testing Inventory Operations...");
            testInventoryOperations(db);

            System.out.println("\n🎉 ALL TESTS COMPLETED SUCCESSFULLY!");

        } catch (Exception e) {
            System.err.println("❌ Test failed with error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
                System.out.println("\n🔒 Database connection closed.");
            }
        }
    }

    private static void testUserOperations(Database db) {
        System.out.println("   Creating test user...");
        int userId = db.createUser("testuser1", "hashedpassword123");
        System.out.println("   ✅ Created user with ID: " + userId);

        System.out.println("   Checking if user exists...");
        boolean exists = db.userExists("testuser1");
        System.out.println("   ✅ User exists check: " + exists);

        System.out.println("   Retrieving user...");
        User user = db.getUser("testuser1", "hashedpassword123");
        if (user != null) {
            System.out.println("   ✅ Retrieved user: " + user.getUsername());
            System.out.println("   Initial coins: " + user.getCoins());

            // Update coins
            user.addCoins(50);
            db.updateUser(user);

            // Retrieve again to verify update
            User updatedUser = db.getUser("testuser1", "hashedpassword123");
            System.out.println("   ✅ Updated coins: " + updatedUser.getCoins());
        } else {
            System.out.println("   ❌ Failed to retrieve user!");
        }
        System.out.println();
    }

    private static void testDuckOperations(Database db) {
        System.out.println("   Creating duck for user...");
        Duck duck = new Duck(1); // Assuming user ID 1 from previous test
        db.createDuck(duck);
        System.out.println("   ✅ Created duck with ID: " + duck.getId());

        System.out.println("   Retrieving duck...");
        Duck retrievedDuck = db.getDuck(1);
        if (retrievedDuck != null) {
            System.out.println("   ✅ Retrieved duck for user ID: " + retrievedDuck.getUserId());
            System.out.println("   Initial hunger: " + retrievedDuck.getHunger());

            // Update duck stats
            retrievedDuck.setHunger(75.5);
            retrievedDuck.setHappiness(85.0);
            retrievedDuck.setLastUpdatedTime(System.currentTimeMillis());
            db.updateDuck(retrievedDuck);

            // Retrieve again to verify update
            Duck updatedDuck = db.getDuck(1);
            System.out.println("   ✅ Updated hunger: " + updatedDuck.getHunger());
        } else {
            System.out.println("   ❌ Failed to retrieve duck!");
        }
        System.out.println();
    }

    private static void testShopOperations(Database db) {
        System.out.println("   Getting all hats...");
        List<Hat> hats = db.getAllHats();
        System.out.println("   ✅ Found " + hats.size() + " hats in shop");
        if (!hats.isEmpty()) {
            System.out.println("   First hat: " + hats.get(0).getName() +
                    " (Price: " + hats.get(0).getPrice() + " coins)");

            // Test getHatById
            Hat hatById = db.getHatById(hats.get(0).getId());
            System.out.println("   ✅ Retrieved hat by ID: " + hatById.getName());
        }

        System.out.println("   Getting all foods...");
        List<Food> foods = db.getAllFoods();
        System.out.println("   ✅ Found " + foods.size() + " foods in shop");
        if (!foods.isEmpty()) {
            System.out.println("   First food: " + foods.get(0).getName() +
                    " (Price: " + foods.get(0).getPrice() + " coins)");

            // Test getFoodById
            Food foodById = db.getFoodById(foods.get(0).getId());
            System.out.println("   ✅ Retrieved food by ID: " + foodById.getName());
        }
        System.out.println();
    }

    private static void testInventoryOperations(Database db) {
        System.out.println("   Testing food inventory...");

        // First, create a test user with food inventory
        int testUserId = db.createUser("inventory_test", "password123");
        User testUser = db.getUser("inventory_test", "password123");

        if (testUser != null) {
            // Set up some food inventory (format: "foodId:quantity,foodId:quantity")
            testUser.setFoodInventory("1:3,2:5,3:1");
            testUser.addCoins(100);
            db.updateUser(testUser);
            System.out.println("   ✅ Set up test user with inventory");

            // Try to use food from inventory
            System.out.println("   Using 2 of food ID 1 from inventory...");
            boolean usedFood = db.useFoodFromInventory(testUserId, 1, 2);
            System.out.println("   ✅ Used food from inventory: " + usedFood);

            // Check updated inventory
            User updatedUser = db.getUser("inventory_test", "password123");
            System.out.println("   Updated food inventory: " + updatedUser.getFoodInventory());
            System.out.println("   Updated coins: " + updatedUser.getCoins());

            // Try to use more food than available
            System.out.println("   Trying to use 10 of food ID 2 (only 5 available)...");
            boolean failedUse = db.useFoodFromInventory(testUserId, 2, 10);
            System.out.println("   ❌ Should fail: " + (!failedUse));

            // Use all remaining of food ID 2
            System.out.println("   Using all 5 of food ID 2...");
            boolean usedAll = db.useFoodFromInventory(testUserId, 2, 5);
            System.out.println("   ✅ Used all food ID 2: " + usedAll);

            // Check final inventory
            User finalUser = db.getUser("inventory_test", "password123");
            System.out.println("   Final food inventory: " + finalUser.getFoodInventory());
        }
        System.out.println();
    }
}

