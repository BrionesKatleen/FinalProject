package backend.services;

import backend.models.*;
import javafx.application.Platform;
import java.util.*;

public class GameBridge {
    public enum DuckState { IDLE, SLEEPING, EATING, BATHE, PLAYING }

    private static GameBridge instance;
    private GameManager gameManager;
    private String currentUsername;

    private GameBridge() {
        this.gameManager = new GameManager();
    }

    public static synchronized GameBridge getInstance() {
        if (instance == null) instance = new GameBridge();
        return instance;
    }

    // ==================== USER MANAGEMENT ====================
    public boolean login(String username, String password) {
        boolean success = gameManager.login(username, password);
        if (success) {
            currentUsername = username;
            System.out.println("Login successful: " + username);
            Platform.runLater(() -> {/* Update UI */});
        }
        return success;
    }

    public boolean register(String username, String password) {
        boolean success = gameManager.register(username, password);
        if (success) {
            currentUsername = username;
            System.out.println("Registration successful: " + username);
        }
        return success;
    }

    public void logout() {
        currentUsername = null;
        System.out.println("User logged out");
    }

    // ==================== DUCK STATS ====================
    public double getStat(String statType) {
        Duck duck = gameManager.getDuck();
        if (duck == null) return getDefaultStat(statType);

        switch(statType.toUpperCase()) {
            case "HAPPINESS": return duck.getHappiness() / 100.0;
            case "HUNGER": return duck.getHunger() / 100.0;
            case "ENERGY": return duck.getEnergy() / 100.0;
            case "CLEANLINESS": return duck.getCleanliness() / 100.0;
            default: return 0.5;
        }
    }

    private double getDefaultStat(String statType) {
        Map<String, Double> defaults = Map.of(
                "HAPPINESS", 0.60, "HUNGER", 0.70,
                "ENERGY", 0.10, "CLEANLINESS", 0.20
        );
        return defaults.getOrDefault(statType.toUpperCase(), 0.5);
    }

    public String getDuckInfo(String infoType) {
        Duck duck = gameManager.getDuck();
        switch(infoType.toUpperCase()) {
            case "STATUS": return duck != null ? duck.getStatusMessage() : "Happy";
            case "STATE": return duck != null ? duck.getState() : "IDLE";
            default: return "";
        }
    }

    public void updateDuckStats(double happiness, double hunger, double energy, double cleanliness) {
        Duck duck = gameManager.getDuck();
        if (duck != null) {
            duck.setHappiness(happiness * 100);
            duck.setHunger(hunger * 100);
            duck.setEnergy(energy * 100);
            duck.setCleanliness(cleanliness * 100);
            duck.setLastUpdatedTime(System.currentTimeMillis());
            gameManager.printDuckStatus();
        }
    }

    // ==================== DUCK ACTIONS ====================
    public void performAction(String action, Object... params) {
        switch(action.toUpperCase()) {
            case "FEED":
                if (params.length > 0) gameManager.feedDuck((int)params[0]);
                break;
            case "CLEAN": gameManager.cleanDuck(); break;
            case "PLAY": gameManager.playWithDuck(); break;
            case "SLEEP": gameManager.putDuckToSleep(); break;
            case "WAKE": wakeDuck(); break;
            case "UPDATE_STATE":
                if (params.length > 0) updateDuckState((DuckState)params[0]);
                break;
        }
        updateUIStats();
    }

    private void wakeDuck() {
        Duck duck = gameManager.getDuck();
        if (duck != null && "SLEEPING".equals(duck.getState())) {
            duck.setState("IDLE");
            duck.setLastUpdatedTime(System.currentTimeMillis());
            duck.setEnergy(Math.min(100, duck.getEnergy() + 30));
        }
    }

    private void updateDuckState(DuckState state) {
        Duck duck = gameManager.getDuck();
        if (duck == null) return;

        duck.setState(state.name());
        duck.setLastUpdatedTime(System.currentTimeMillis());

        Map<DuckState, Runnable> stateEffects = Map.of(
                DuckState.SLEEPING, () -> duck.setEnergy(Math.min(100, duck.getEnergy() + 5)),
                DuckState.EATING, () -> {
                    duck.setHappiness(Math.min(100, duck.getHappiness() + 10));
                    duck.setHunger(Math.max(0, duck.getHunger() - 15));
                },
                DuckState.BATHE, () -> {
                    duck.setCleanliness(Math.min(100, duck.getCleanliness() + 20));
                    duck.setHappiness(Math.min(100, duck.getHappiness() + 5));
                },
                DuckState.PLAYING, () -> {
                    duck.setHappiness(Math.min(100, duck.getHappiness() + 15));
                    duck.setEnergy(Math.max(0, duck.getEnergy() - 10));
                },
                DuckState.IDLE, () -> {
                    if (duck.getEnergy() < 50) duck.setEnergy(Math.min(100, duck.getEnergy() + 1));
                }
        );

        stateEffects.getOrDefault(state, () -> {}).run();
        System.out.println("Duck state updated to: " + state);
    }

    private void updateUIStats() {
        Platform.runLater(() -> {/* Update UI components */});
    }

    // ==================== INVENTORY & SHOP ====================
    public int getUserValue(String valueType) {
        User user = gameManager.getUser();
        if (user == null) return 0;

        switch(valueType.toUpperCase()) {
            case "COINS": return user.getCoins();
            case "LEVEL": return user.getLevel();
            default: return 0;
        }
    }

    public List<Integer> getUserOwnedHats() {
        User user = gameManager.getUser();
        return user != null ? user.getOwnedHatIds() : new ArrayList<>();
    }

    public Map<Integer, Integer> getUserFoodInventory() {
        User user = gameManager.getUser();
        return user != null ? parseInventory(user.getFoodInventory()) : new HashMap<>();
    }

    public boolean purchaseItem(String itemType, int id, int quantity) {
        boolean success = false;
        switch(itemType.toUpperCase()) {
            case "HAT": success = gameManager.buyHat(id); break;
            case "FOOD": success = gameManager.buyFood(id, quantity); break;
        }

        if (success) Platform.runLater(() -> {/* Update UI */});
        return success;
    }

    public boolean useFood(int foodId, int quantity) {
        User user = gameManager.getUser();
        if (user == null) return false;

        Map<Integer, Integer> inventory = parseInventory(user.getFoodInventory());
        int currentQty = inventory.getOrDefault(foodId, 0);

        if (currentQty < quantity) return false;

        int newQty = currentQty - quantity;
        if (newQty <= 0) inventory.remove(foodId);
        else inventory.put(foodId, newQty);

        user.setFoodInventory(buildInventoryString(inventory));
        return true;
    }

    public List<?> getShopItems(String itemType) {
        switch(itemType.toUpperCase()) {
            case "HAT": return gameManager.getShopHats();
            case "FOOD": return gameManager.getShopFoods();
            default: return new ArrayList<>();
        }
    }

    public Object getItemById(String itemType, int id) {
        List<?> items = getShopItems(itemType);
        return items.stream()
                .filter(item -> {
                    if (item instanceof Hat) return ((Hat)item).getId() == id;
                    if (item instanceof Food) return ((Food)item).getId() == id;
                    return false;
                })
                .findFirst()
                .orElse(null);
    }

    // ==================== HELPER METHODS ====================
    private Map<Integer, Integer> parseInventory(String inventory) {
        Map<Integer, Integer> map = new HashMap<>();
        if (inventory == null || inventory.isEmpty() || inventory.equals("{}")) return map;

        inventory = inventory.replaceAll("[{}]", "");
        Arrays.stream(inventory.split(","))
                .map(item -> item.split(":"))
                .filter(parts -> parts.length == 2)
                .forEach(parts -> {
                    try {
                        map.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {}
                });
        return map;
    }

    private String buildInventoryString(Map<Integer, Integer> map) {
        return map.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    public int getFoodQuantity(int foodIndex) {
        int foodId = mapFoodIndexToId(foodIndex);
        return getUserFoodInventory().getOrDefault(foodId, 0);
    }

    public void setFoodQuantity(int foodIndex, int newQuantity) {
        int foodId = mapFoodIndexToId(foodIndex);
        Map<Integer, Integer> inventory = getUserFoodInventory();

        if (newQuantity <= 0) inventory.remove(foodId);
        else inventory.put(foodId, newQuantity);

        User user = gameManager.getUser();
        if (user != null) user.setFoodInventory(buildInventoryString(inventory));
    }

    private int mapFoodIndexToId(int foodIndex) {
        int[] mapping = {1, 2, 3, 4}; // peas, birdseed, corn, oats
        return (foodIndex >= 0 && foodIndex < mapping.length) ? mapping[foodIndex] : 1;
    }

    // ==================== UTILITY METHODS ====================
    public boolean isDuckAlive() {
        Duck duck = gameManager.getDuck();
        return duck != null && duck.isFine();
    }

    public boolean doesDuckNeedAttention() {
        Duck duck = gameManager.getDuck();
        return duck != null && duck.needsAttention();
    }

    public void modifyCoins(int amount) {
        User user = gameManager.getUser();
        if (user != null) {
            user.addCoins(amount);
            Platform.runLater(() -> {/* Update UI */});
        }
    }

    public void updatePlayerLevel(int newLevel) {
        User user = gameManager.getUser();
        if (user != null) user.setLevel(newLevel);
    }

    public void syncStatsWithUI() {
        Duck duck = gameManager.getDuck();
        if (duck != null) gameManager.printDuckStatus();
    }

    public void shutdown() {
        if (gameManager != null) gameManager.shutdown();
        instance = null;
    }

    public String getCurrentUsername() { return currentUsername; }
    public boolean isLoggedIn() { return currentUsername != null; }
    public String getTimeStatus() { return gameManager.getTimeStatus(); }
}


//package backend.services;
//
//import java.util.Map;
//
///**
// * Simple GameBridge that connects the frontend to the backend GameManager.
// */
//public class GameBridge {
//    private static GameManager gameManager = new GameManager();
//
//    // ===== AUTHENTICATION =====
//    /**
//     * Logs in a user
//     * @return true if login successful, false otherwise
//     */
//    public static boolean login(String username, String password) {
//        return gameManager.login(username, password);
//    }
//
//    /**
//     * Registers a new user
//     * return true if registration successful, false otherwise
//     */
//    public static boolean register(String username, String password) {
//        return gameManager.register(username, password);
//    }
//
//    // ===== DUCK ACTIONS =====
//
//    /**
//     * Feeds the duck with specified food type
//     * username The username
//     * foodType The type of food (0-3)
//     */
//    public static void feedDuck(String username, int foodType) {
//        gameManager.feedDuck(username, foodType);
//    }
//
//    /**
//     * Cleans the duck
//     */
//    public static void cleanDuck(String username) {
//        gameManager.cleanDuck(username);
//    }
//
//    /**
//     * Plays with the duck
//     */
//    public static void playWithDuck(String username) {
//        gameManager.playWithDuck(username);
//    }
//
//    /**
//     * Puts the duck to sleep
//     */
//    public static void sleepDuck(String username) {
//        gameManager.sleepDuck(username);
//    }
//
//    /**
//     * Toggles night mode
//     * username The username
//     * isNight true for night mode, false for day mode
//     */
//    public static void toggleNightMode(String username, boolean isNight) {
//        gameManager.toggleNightMode(username, isNight);
//    }
//
//    /**
//     * Equips an outfit
//     * username The username
//     * outfitIndex The outfit index
//     */
//    public static void equipOutfit(String username, int outfitIndex) {
//        gameManager.equipOutfit(username, outfitIndex);
//    }
//
//    // ===== STAT GETTERS =====
//
//    /**
//     * Gets duck happiness
//     * username The username
//     * happiness value (0.0 - 1.0)
//     */
//    public static double getHappiness(String username) {
//        return gameManager.getHappiness(username);
//    }
//
//    /**
//     * Gets duck hunger
//     * username The username
//     * return hunger value (0.0 - 1.0)
//     */
//    public static double getHunger(String username) {
//        return gameManager.getHunger(username);
//    }
//
//    /**
//     * Gets duck energy
//     * username The username
//     * return energy value (0.0 - 1.0)
//     */
//    public static double getEnergy(String username) {
//        return gameManager.getEnergy(username);
//    }
//
//    /**
//     * Gets duck cleanliness
//     * username The username
//     * return cleanliness value (0.0 - 1.0)
//     */
//    public static double getCleanliness(String username) {
//        return gameManager.getCleanliness(username);
//    }
//
//    /**
//     * Gets equipped outfit index
//     * username The username
//     * return outfit index
//     */
//    public static int getOutfitIndex(String username) {
//        return gameManager.getOutfitIndex(username);
//    }
//
//    /**
//     * Checks if night mode is active
//     * username The username
//     * return true if night mode is active
//     */
//    public static boolean isNightMode(String username) {
//        return gameManager.isNightMode(username);
//    }
//
//    // ===== USER STATS =====
//
//    /**
//     * Gets user credits
//     * username The username
//     * return credits amount
//     */
//    public static int getCredits(String username) {
//        return gameManager.getCredits(username);
//    }
//
//    /**
//     * Gets user level
//     * username The username
//     * return user level
//     */
//    public static int getLevel(String username) {
//        return gameManager.getLevel(username);
//    }
//
//    /**
//     * Gets food quantities
//     * username The username
//     * return array of food quantities [type0, type1, type2, type3]
//     */
//    public static int[] getFoodQuantities(String username) {
//        return gameManager.getFoodQuantities(username);
//    }
//
//    /**
//     * Gets status message for the duck
//     * username The username
//     * return status message string
//     */
//    public static String getStatusMessage(String username) {
//        return gameManager.getStatusMessage(username);
//    }
//
//    // ===== FOOD SYSTEM =====
//
//    /**
//     * Buys food for the user
//     * username The username
//     * foodType The type of food (0-3)
//     * quantity The quantity to buy
//     * return true if purchase successful, false otherwise
//     */
//    public static boolean buyFood(String username, int foodType, int quantity) {
//        return gameManager.buyFood(username, foodType, quantity);
//    }
//
//    // ===== MEMORY GAME =====
//
//    /**
//     * Saves memory game score
//     * username The username
//     *  score The score achieved
//     *  creditsEarned Credits earned from the game
//     *  mistakes Number of mistakes made
//     */
//    public static void saveMemoryScore(String username, int score, int creditsEarned, int mistakes) {
//        gameManager.saveMemoryScore(username, score, creditsEarned, mistakes);
//    }
//
//    // ===== SHUTDOWN =====
//
//    /**
//     * Shuts down the game manager and cleans up resources
//     */
//    public static void shutdown() {
//        gameManager.shutdown();
//    }
//
//    // ===== BATCH OPERATIONS =====
//
//    /**
//     * Gets all duck stats at once for efficiency
//     * username The username
//     * return Map containing all duck stats
//     */
//    public static Map<String, Double> getAllDuckStats(String username) {
//        // This is a convenient method to get all stats at once
//        // You might want to add this to GameManager if it doesn't exist
//        return Map.of(
//                "happiness", getHappiness(username),
//                "hunger", getHunger(username),
//                "energy", getEnergy(username),
//                "cleanliness", getCleanliness(username),
//                "outfit_index", (double) getOutfitIndex(username),
//                "is_night_mode", isNightMode(username) ? 1.0 : 0.0
//        );
//    }
//
//    /**
//     * Gets all user data at once
//     * username The username
//     * return Map containing user data
//     */
//    public static Map<String, Object> getUserData(String username) {
//        return Map.of(
//                "username", username,
//                "credits", getCredits(username),
//                "level", getLevel(username),
//                "food_quantities", getFoodQuantities(username),
//                "stats", getAllDuckStats(username)
//        );
//    }
//}

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
