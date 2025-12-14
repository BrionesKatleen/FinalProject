package backend.services;

import backend.models.*;
import database.Database;
import java.util.*;

public class GameManager {
    private Database db;
    private StatsManager statsManager;
    private User currentUser;
    private Duck currentDuck;
    private List<Hat> shopHats;
    private List<Food> shopFoods;
    private Timer statTimer;

    public GameManager() {
        try {
            this.db = new Database();
            this.statsManager = new StatsManager();
            loadShopItems();
            startStatTimer();
        } catch (Exception e) {
            System.err.println("GameManager initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ====================
    // STAT DECAY TIMER - FIXED to run less frequently
    // ====================
    private void startStatTimer() {
        statTimer = new Timer();
        statTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentDuck != null) {
                    // Apply decay
                    statsManager.applyStatDecay(currentDuck);

                    // Update database
                    db.updateDuck(currentDuck);

                    // Check status
                    if (currentDuck.needsAttention()) {
                        System.out.println("[ALERT] Duck needs attention: " + currentDuck.getStatusMessage());
                    }

                    if (!currentDuck.isFine()) {
                        System.out.println("[WARNING] Duck stats are very low!");
                        // Don't kill the duck, just warn
                    }

                    System.out.println("[TIME] " + statsManager.getTimeStatus(currentDuck));
                    printDuckStatus(); // Optional: print current status
                }
            }
        }, 30000, 30000); // Run every 1 minute (60000 ms)
    }

    // ====================
    // USER AUTHENTICATION
    // ====================
    public boolean login(String username, String password) {
        String hash = hashPassword(password);
        User user = db.getUser(username, hash);

        if (user != null) {
            this.currentUser = user;
            this.currentDuck = db.getDuck(user.getId());

            if (currentDuck == null) {
                currentDuck = new Duck(user.getId());
                db.createDuck(currentDuck);
                System.out.println("New duck created for player!");

                // Initialize with good stats
                currentDuck.setEnergy(100);
                currentDuck.setHunger(100);
                currentDuck.setCleanliness(100);
                currentDuck.setHappiness(100);
                db.updateDuck(currentDuck);
            }

            System.out.println("Welcome! " + currentDuck.getStatusMessage());
            System.out.println("Time Status: " + statsManager.getTimeStatus(currentDuck));
            printDuckStatus();
            return true;
        }
        return false;
    }

    public boolean register(String username, String password) {
        if (db.userExists(username)) {
            System.err.println("Username already exists: " + username);
            return false;
        }

        String hash = hashPassword(password);
        int userId = db.createUser(username, hash);

        return userId > 0 && login(username, password);
    }

    private String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }

    // ====================
    // DUCK ACTIONS - FIXED for 0-100 scale
    // ====================
    public void feedDuck(int foodId) {
        if (!checkDuck()) return;

        Food food = getFoodById(foodId);
        if (food == null) {
            System.out.println("Food not found with ID: " + foodId);
            return;
        }

        // CHECK INVENTORY FIRST
        if (!db.useFoodFromInventory(currentUser.getId(), foodId, 1)) {
            System.out.println("No " + food.getName() + " in inventory!");
            return;
        }

        // Only proceed if food is available
        currentDuck.setState("EATING");

        // Apply food effects (multiply by 100 to convert to 0-100 scale)
        double hungerRestore = food.getHungerRestore() * 100;
        double energyRestore = food.getEnergyRestore() * 100;
        double happinessBonus = food.getHappinessBonus() * 100;
        double cleanlinessReduction = food.getCleanlinessReduction() * 100;

        // Apply effects with bounds checking
        double newHunger = clampStat(currentDuck.getHunger() + hungerRestore);
        double newEnergy = clampStat(currentDuck.getEnergy() + energyRestore);
        double newHappiness = clampStat(currentDuck.getHappiness() + happinessBonus);
        double newCleanliness = clampStat(currentDuck.getCleanliness() + cleanlinessReduction);

        currentDuck.setHunger(newHunger);
        currentDuck.setEnergy(newEnergy);
        currentDuck.setHappiness(newHappiness);
        currentDuck.setCleanliness(newCleanliness);
        currentDuck.setLastUpdatedTime(System.currentTimeMillis());

        // Add coins for feeding
        int coinsEarned = (int) food.getPrice();
        currentUser.addCoins(coinsEarned);

        db.updateDuck(currentDuck);
        db.updateUser(currentUser);

        System.out.println("Fed " + food.getName() + "! " + currentDuck.getStatusMessage());
        System.out.println(String.format("Effects: Hunger +%.0f, Energy +%.0f, Happiness +%.0f, Cleanliness %.0f",
                hungerRestore, energyRestore, happinessBonus, cleanlinessReduction));
        System.out.println("Earned " + coinsEarned + " coins!");
        printDuckStatus();
    }

    private double clampStat(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    public void cleanDuck() {
        if (!checkDuck()) return;

        statsManager.batheDuck(currentDuck);
        currentDuck.setLastUpdatedTime(System.currentTimeMillis());
        currentUser.addCoins(2);

        db.updateDuck(currentDuck);
        db.updateUser(currentUser);

        System.out.println("Duck cleaned! " + currentDuck.getStatusMessage());
        printDuckStatus();
    }

    public void playWithDuck() {
        if (!checkDuck()) return;

        statsManager.playWithDuck(currentDuck);
        currentDuck.setLastUpdatedTime(System.currentTimeMillis());
        currentUser.addCoins(5);

        db.updateDuck(currentDuck);
        db.updateUser(currentUser);

        System.out.println("Played with duck! " + currentDuck.getStatusMessage());
        printDuckStatus();
    }

    public void putDuckToSleep() {
        if (!checkDuck()) return;

        statsManager.sleepDuck(currentDuck);
        currentDuck.setLastUpdatedTime(System.currentTimeMillis());
        db.updateDuck(currentDuck);

        System.out.println("Duck is sleeping... Zzz");
        printDuckStatus();
    }

    public void printDuckStatus() {
        if (!checkDuck()) return;

        System.out.println("\n=== DUCK STATUS ===");
        System.out.println("Hunger:      " + String.format("%.1f", currentDuck.getHunger()) + "/100");
        System.out.println("Energy:      " + String.format("%.1f", currentDuck.getEnergy()) + "/100");
        System.out.println("Cleanliness: " + String.format("%.1f", currentDuck.getCleanliness()) + "/100");
        System.out.println("Happiness:   " + String.format("%.1f", currentDuck.getHappiness()) + "/100");
        System.out.println("State:       " + currentDuck.getState());
        System.out.println("Mood:        " + currentDuck.getStatusMessage());
        System.out.println("Time Status: " + statsManager.getTimeStatus(currentDuck));
        System.out.println("===================\n");
    }

    // ====================
    // SHOP METHODS
    // ====================
    private void loadShopItems() {
        shopHats = db.getAllHats();
        shopFoods = db.getAllFoods();
        System.out.println("Loaded " + shopHats.size() + " hats and " + shopFoods.size() + " foods.");
    }

    public boolean buyHat(int hatId) {
        if (!checkUser()) return false;

        Hat hat = getHatById(hatId);
        if (hat == null) {
            System.err.println("Hat not found: " + hatId);
            return false;
        }

        if (currentUser.getLevel() < hat.getLvlRequired()) {
            System.err.println("Level too low to buy " + hat.getName());
            return false;
        }

        if (currentUser.ownsHat(hatId)) {
            System.err.println("Already own " + hat.getName());
            return false;
        }

        if (currentUser.deductCoins(hat.getPrice())) {
            currentUser.addHat(hatId);
            db.updateUser(currentUser);
            System.out.println("Bought hat: " + hat.getName());
            return true;
        } else {
            System.err.println("Not enough coins for " + hat.getName());
            return false;
        }
    }

    public boolean buyFood(int foodId, int quantity) {
        if (!checkUser()) return false;
        if (quantity <= 0) return false;

        Food food = getFoodById(foodId);
        if (food == null) return false;

        int pricePerUnit = (int) food.getPrice();
        int totalCost = pricePerUnit * quantity;

        if (!currentUser.deductCoins(totalCost)) {
            System.err.println("Not enough coins!");
            return false;
        }

        String inventory = currentUser.getFoodInventory();
        Map<Integer, Integer> foodMap = parseInventory(inventory);
        foodMap.put(foodId, foodMap.getOrDefault(foodId, 0) + quantity);
        currentUser.setFoodInventory(buildInventoryString(foodMap));
        db.updateUser(currentUser);

        System.out.println("Bought " + quantity + "x " + food.getName());
        return true;
    }

    private Map<Integer, Integer> parseInventory(String inventory) {
        Map<Integer, Integer> map = new HashMap<>();
        if (inventory == null || inventory.isEmpty() || inventory.equals("{}")) return map;

        inventory = inventory.replace("{", "").replace("}", "");
        String[] items = inventory.split(",");
        for (String item : items) {
            String[] parts = item.split(":");
            if (parts.length == 2) {
                try {
                    map.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }

    private String buildInventoryString(Map<Integer, Integer> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    // ====================
    // HELPERS
    // ====================
    private boolean checkDuck() {
        if (currentDuck == null) {
            System.out.println("No duck found! Login first.");
            return false;
        }
        return true;
    }

    private boolean checkUser() {
        if (currentUser == null) {
            System.out.println("Please login first!");
            return false;
        }
        return true;
    }

    private Hat getHatById(int hatId) {
        for (Hat hat : shopHats) if (hat.getId() == hatId) return hat;
        return null;
    }

    private Food getFoodById(int foodId) {
        for (Food food : shopFoods) if (food.getId() == foodId) return food;
        return null;
    }

    // ====================
    // GETTERS
    // ====================
    public User getUser() { return currentUser; }
    public Duck getDuck() { return currentDuck; }
    public List<Hat> getShopHats() { return shopHats; }
    public List<Food> getShopFoods() { return shopFoods; }
    public String getTimeStatus() {
        return currentDuck != null ? statsManager.getTimeStatus(currentDuck) : "No duck loaded";
    }

    // ====================
    // CLEANUP
    // ====================
    public void shutdown() {
        if (statTimer != null) {
            statTimer.cancel();
            statTimer = null;
        }
        if (db != null) {
            db.close();
        }
    }
}

//package backend.services;
//
//import database.Database;
//import java.util.*;
//
//public class GameManager {
//    private Database db;
//    private StatsManager statsManager;
//    private Map<String, Integer> userIds = new HashMap<>();
//
//    public GameManager() {
//        try {
//            db = new Database();
//            statsManager = new StatsManager();
//        } catch (Exception e) {
//            System.err.println("Failed to initialize GameManager: " + e.getMessage());
//        }
//    }
//
//    // --- AUTHENTICATION ---
//    public boolean register(String username, String password) {
//        if (db.createUser(username, password)) {
//            int userId = db.getUserId(username);
//            userIds.put(username, userId);
//            db.createDuck(userId);
//            db.initFood(userId);
//            System.out.println("Registered user: " + username);
//            return true;
//        }
//        return false;
//    }
//
//    public boolean login(String username, String password) {
//        boolean success = db.authenticate(username, password);
//        if (success) {
//            int userId = db.getUserId(username);
//            userIds.put(username, userId);
//
//            // Initialize stats if first login
//            Map<String, Double> stats = db.getDuckStats(username);
//            if (stats.isEmpty()) {
//                db.createDuck(userId);
//                db.initFood(userId);
//            }
//
//            // Apply stat decay
//            applyStatDecay(username);
//        }
//        return success;
//    }
//
//    private void applyStatDecay(String username) {
//        Map<String, Double> stats = db.getDuckStats(username);
//        if (!stats.isEmpty()) {
//            stats = statsManager.applyStatDecay(stats);
//            db.updateDuckStats(username, stats);
//        }
//    }
//
//    // --- DUCK ACTIONS ---
//    public void feedDuck(String username, int foodType) {
//        Map<String, Double> stats = db.getDuckStats(username);
//        int[] foodQty = db.getFoodQuantities(username);
//        if (foodQty[foodType] > 0) {
//            stats = statsManager.feedDuck(stats, foodType);
//            foodQty[foodType]--;
//            db.updateFoodQuantity(username, foodType, foodQty[foodType]);
//            db.updateDuckStats(username, stats);
//        }
//    }
//
//    public void cleanDuck(String username) {
//        Map<String, Double> stats = db.getDuckStats(username);
//        stats = statsManager.cleanDuck(stats);
//        db.updateDuckStats(username, stats);
//    }
//
//    public void playWithDuck(String username) {
//        Map<String, Double> stats = db.getDuckStats(username);
//        stats = statsManager.playWithDuck(stats);
//        db.updateDuckStats(username, stats);
//    }
//
//    public void sleepDuck(String username) {
//        Map<String, Double> stats = db.getDuckStats(username);
//        stats = statsManager.sleepDuck(stats);
//        db.updateDuckStats(username, stats);
//    }
//
//    public Map<String, Double> getDuckStats(String username) {
//        return db.getDuckStats(username);
//    }
//
//    public int getCredits(String username) {
//        return db.getUserCredits(username);
//    }
//
//    public int getLevel(String username) {
//        return db.getUserLevel(username);
//    }
//
//    public int[] getFoodQuantities(String username) {
//        return db.getFoodQuantities(username);
//    }
//
//    public void shutdown() {
//        db.close();
//    }
//}