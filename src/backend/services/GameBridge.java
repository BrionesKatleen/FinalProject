//package backend.services;
//
//import backend.models.*;
//
//import java.util.List;
//
//public class GameBridge {
//    private static GameManager gameManager;
//
//    static {
//        gameManager = new GameManager();
//    }
//
//    /**
//     * GETTERS -> DUCK, USER, UserLevel, UserCoins, DuckStateMessage
//     * */
//    public static Duck getCurrentDuck() {
//        Duck duck = gameManager.getDuck();
//        if (duck == null) {
//            System.out.println("[BACKEND] No duck found - user not logged in?");
//        }
//        return duck;
//    }
//
//    public static User getCurrentUser() {
//        User user = gameManager.getUser();
//        if (user == null) {
//            System.out.println("[BACKEND] No user found - not logged in?");
//        }
//        return user;
//    }
//
//    public static int getUserLevel() {
//        User user = getCurrentUser();
//        if (user != null) {
//            System.out.println("[BACKEND] User Level: " + user.getLevel());
//            return user.getLevel();
//        }
//        return 1;
//    }
//
//    public static int getUserCoins() {
//        User user = getCurrentUser();
//        if (user != null) {
//            System.out.println("[BACKEND] User Coins: " + user.getCoins());
//            return user.getCoins();
//        }
//        return 100;
//    }
//
//    public static String getDuckStatusMessage() {
//        Duck duck = getCurrentDuck();
//        if (duck != null) {
//            String message = duck.getStatusMessage();
//            System.out.println("[BACKEND] Duck says: " + message);
//            return message;
//        }
//        return "No duck found!";
//    }
//
//    // CONVERT DOUBLE TO PERCENTAGE FORMAT
//    public static double getHungerPercentage() {
//        Duck duck = getCurrentDuck();
//        if (duck != null) {
//            double percentage = duck.getHunger() / 100.0;
//            System.out.println("[BACKEND] Hunger: " +
//                    String.format("%.1f", duck.getHunger()) + "/100 (" +
//                    String.format("%.1f", percentage*100) + "%)");
//            return percentage;
//        }
//        return 0.5;
//    }
//
//    public static double getEnergyPercentage() {
//        Duck duck = getCurrentDuck();
//        if (duck != null) {
//            double percentage = duck.getEnergy() / 100.0;
//            System.out.println("[BACKEND] Energy: " +
//                    String.format("%.1f", duck.getEnergy()) + "/100 (" +
//                    String.format("%.1f", percentage*100) + "%)");
//            return percentage;
//        }
//        return 0.5;
//    }
//
//    public static double getCleanlinessPercentage() {
//        Duck duck = getCurrentDuck();
//        if (duck != null) {
//            double percentage = duck.getCleanliness() / 100.0;
//            System.out.println("[BACKEND] Cleanliness: " +
//                    String.format("%.1f", duck.getCleanliness()) + "/100 (" +
//                    String.format("%.1f", percentage*100) + "%)");
//            return percentage;
//        }
//        return 0.5;
//    }
//
//    public static double getHappinessPercentage() {
//        Duck duck = getCurrentDuck();
//        if (duck != null) {
//            double percentage = duck.getHappiness() / 100.0;
//            System.out.println("[BACKEND] Happiness: " +
//                    String.format("%.1f", duck.getHappiness()) + "/100 (" +
//                    String.format("%.1f", percentage*100) + "%)");
//            return percentage;
//        }
//        return 0.5;
//    }
//
//    /**
//     * NEW FEATURE -> NIGHT TIME STATUS
//     * */
//    public static String getNightTimeStatus() {
//        return gameManager.getNightTimeStatus();
//    }
//
//    /**
//     * LOGIN & SIGN UP SYSTEM
//     * */
//
//    // Authentication methods (unchanged)
//    public static boolean loginUser(String username, String password) {
//        // ... unchanged ...
//        boolean result = gameManager.login(username, password);
//        if (result) {
//            System.out.println("[BACKEND] User logged in: " + username);
//            printDuckStatus();
//        } else {
//            System.out.println("[BACKEND] Login failed for: " + username);
//        }
//        return result;
//    }
//
//    public static boolean registerUser(String username, String password) {
//        // ... unchanged ...
//        boolean result = gameManager.register(username, password);
//        if (result) {
//            System.out.println("[BACKEND] User registered: " + username);
//            printDuckStatus();
//        } else {
//            System.out.println("[BACKEND] Registration failed for: " + username);
//        }
//        return result;
//    }
//
//    /**
//     *  DUCK ACTION METHODS
//     * */
//    public static void feedDuck(int foodId) {
//        // ... unchanged ...
//        System.out.println("[BACKEND] Feeding duck with food ID: " + foodId);
//        gameManager.feedDuck(foodId);
//        printDuckStatus();
//    }
//
//    public static void cleanDuck() {
//        // ... unchanged ...
//        System.out.println("[BACKEND] Cleaning duck");
//        gameManager.cleanDuck();
//        printDuckStatus();
//    }
//
//    public static void playWithDuck() {
//        // ... unchanged ...
//        System.out.println("[BACKEND] Playing with duck");
//        gameManager.playWithDuck();
//        printDuckStatus();
//    }
//
//    public static void putDuckToSleep() {
//        // ... unchanged ...
//        System.out.println("[BACKEND] Putting duck to sleep");
//        gameManager.putDuckToSleep();
//        printDuckStatus();
//    }
//
//    /**
//     * DUCK SHOP METHODS (UNCHECK)
//     * */
//    public static boolean buyHat(int hatId) {
//        // ... unchanged ...
//        System.out.println("[BACKEND] Attempting to buy hat ID: " + hatId);
//        boolean result = gameManager.buyHat(hatId);
//        if (result) {
//            System.out.println("[BACKEND] Hat purchased successfully");
//            printUserStatus();
//        } else {
//            System.out.println("[BACKEND] Failed to buy hat (not enough coins?)");
//        }
//        return result;
//    }
//
//    public static boolean buyFood(int foodId, int quantity) {
//        // ... unchanged ...
//        System.out.println("[BACKEND] Buying " + quantity + "x food ID: " + foodId);
//        boolean result = gameManager.buyFood(foodId, quantity);
//        if (result) {
//            System.out.println("[BACKEND] Food purchased successfully");
//            printUserStatus();
//        } else {
//            System.out.println("[BACKEND] Failed to buy food (not enough coins?)");
//        }
//        return result;
//    }
//
//    /**
//     * MINI-GAME METHODS (UNCHECK)
//     * */
//    public static void saveDuckDashScore(int score) {
//        // ... unchanged ...
//        System.out.println("[BACKEND] Saving Duck Dash score: " + score);
//        gameManager.saveDuckDashScore(score);
//        printUserStatus();
//    }
//
//    public static void saveCardMatchScore(int score) {
//        // ... unchanged ...
//        System.out.println("[BACKEND] Saving Card Match score: " + score);
//        gameManager.saveCardMatchScore(score);
//        printUserStatus();
//    }
//
//
//
//    // HELPER METHODS
//    private static void printDuckStatus() {
//        Duck duck = getCurrentDuck();
//        if (duck != null) {
//            System.out.println("=== DUCK STATUS ===");
//            System.out.println("Hunger: " + String.format("%.1f", duck.getHunger()) + "/100");
//            System.out.println("Energy: " + String.format("%.1f", duck.getEnergy()) + "/100");
//            System.out.println("Cleanliness: " + String.format("%.1f", duck.getCleanliness()) + "/100");
//            System.out.println("Happiness: " + String.format("%.1f", duck.getHappiness()) + "/100");
//            System.out.println("State: " + duck.getState());
//            System.out.println("Fine: " + duck.isFine());
//            System.out.println("Needs Attention: " + duck.needsAttention());
//            System.out.println("Time Status: " + getNightTimeStatus()); // NEW
//            System.out.println("========================\n");
//        }
//    }
//
//    private static void printUserStatus() {
//        User user = getCurrentUser();
//        if (user != null) {
//            System.out.println("=== USER STATUS ===");
//            System.out.println("Username: " + user.getUsername());
//            System.out.println("Coins: " + user.getCoins());
//            System.out.println("Level: " + user.getLevel());
//            System.out.println("Experience: " + user.getExperience() + "/" + (user.getLevel() * 100));
//            System.out.println("Owned Hats: " + user.getOwnedHatIds().size());
//            System.out.println("========================\n");
//        }
//    }
//
//    // SHUTDOWN BACKEND
//    public static void shutdown() {
//        System.out.println("[BACKEND] Shutting down GameManager...");
//        gameManager.shutdown();
//    }
//}