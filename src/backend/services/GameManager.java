package backend.services;

import database.Database;
import backend.models.*;
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
            this.statsManager = new StatsManager(); // NEW: Initialize StatsManager
            loadShopItems();
            startStatTimer();

            db.testConnection();
        } catch (Exception e) {
            System.err.println("GameManager initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === UPDATED STAT DECAY TIMER ===
    private void startStatTimer() {
        statTimer = new Timer();
        statTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentDuck != null) {
                    // NEW: Use StatsManager for all stat calculations
                    statsManager.applyStatDecay(currentDuck);

                    // Save to database
                    db.updateDuck(currentDuck);

                    // Check for warnings
                    if (currentDuck.needsAttention()) {
                        System.out.println("[ALERT] Duck needs attention: " +
                                currentDuck.getStatusMessage());
                    }

                    // Check if duck is capable of doing activities
                    if (!currentDuck.isFine()) {
                        System.out.println("[CRITICAL] Duck has died! Stats are too low!");
                    }

                    // Log status every 5 minutes
                    if (System.currentTimeMillis() % (5 * 60 * 1000) < 1000) {
                        System.out.println("[DUCK STATUS] " + currentDuck.toString());
                        System.out.println("[TIME STATUS] " + statsManager.getNightTimeStatus());
                    }
                }
            }
        }, 0, 60000); // Check every 1 minute
    }

    // === UPDATED AUTHENTICATION ===
    public boolean login(String username, String password) {
        String hash = hashPassword(password);
        User user = db.getUser(username, hash);

        if (user != null) {
            this.currentUser = user;
            this.currentDuck = db.getDuck(user.getId());

            if (currentDuck != null) {
                // NEW: Use StatsManager to apply decay
                statsManager.applyStatDecay(currentDuck);
                db.updateDuck(currentDuck);
                System.out.println("Welcome back! " + currentDuck.getStatusMessage());
                System.out.println("Last updated: " +
                        new java.util.Date(currentDuck.getLastUpdatedTime()));
            }

            return true;
        }
        return false;
    }

    // === UPDATED DUCK ACTIONS ===
    public void feedDuck(int foodId) {
        if (currentDuck == null || currentUser == null) {
            System.out.println("No duck found! Please login first.");
            return;
        }

        Food food = getFoodById(foodId);
        if (food != null) {
            if (db.useFoodFromInventory(currentUser.getId(), foodId, 1)) {
                // NEW: Use StatsManager for feeding logic
                statsManager.feedDuck(currentDuck);

                // Apply food-specific boosts
                currentDuck.setHunger(currentDuck.getHunger() + food.getHungerRestore());
                currentDuck.setHappiness(currentDuck.getHappiness() + food.getHappinessBonus());
                if (food.getEnergyRestore() > 0) {
                    currentDuck.setEnergy(currentDuck.getEnergy() + food.getEnergyRestore());
                }

                currentUser.addCoins(3);
                currentDuck.setLastUpdatedTime(System.currentTimeMillis());

                db.updateDuck(currentDuck);
                db.updateUser(currentUser);

                System.out.println("Fed " + currentDuck.getStatusMessage());
            } else {
                System.out.println("No " + food.getName() + " in inventory!");
            }
        }
    }

    public void cleanDuck() {
        if (currentDuck == null || currentUser == null) return;

        // NEW: Use StatsManager for cleaning logic
        statsManager.batheDuck(currentDuck);
        currentDuck.setLastUpdatedTime(System.currentTimeMillis());

        currentUser.addCoins(2);

        db.updateDuck(currentDuck);
        db.updateUser(currentUser);

        System.out.println("Duck cleaned! " + currentDuck.getStatusMessage());
    }

    public void playWithDuck() {
        if (currentDuck == null || currentUser == null) return;

        // NEW: Use StatsManager for playing logic
        statsManager.playWithDuck(currentDuck);
        currentDuck.setLastUpdatedTime(System.currentTimeMillis());

        currentUser.addCoins(5);

        db.updateDuck(currentDuck);
        db.updateUser(currentUser);

        System.out.println("Played with duck! " + currentDuck.getStatusMessage());
    }

    public void putDuckToSleep() {
        if (currentDuck == null) return;

        // NEW: Use StatsManager for sleeping logic
        statsManager.sleepDuck(currentDuck);
        currentDuck.setLastUpdatedTime(System.currentTimeMillis());

        db.updateDuck(currentDuck);

        System.out.println("Duck is sleeping... Zzz");
    }

    // NEW: Added method to get nighttime status for UI
    public String getNightTimeStatus() {
        return statsManager.getNightTimeStatus();
    }

    // === REST OF THE METHODS REMAIN THE SAME (no conflicts) ===
    public boolean register(String username, String password) {
        // ... unchanged ...
        if (db.userExists(username)) {
            System.err.println("Username already exists: " + username);
            return false;
        }

        String hash = hashPassword(password);
        int userId = db.createUser(username, hash);

        if (userId > 0) {
            return login(username, password);
        }
        return false;
    }

    private String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }

    public void printDuckStatus() {
        // ... unchanged ...
        if (currentDuck == null) {
            System.out.println("No duck found!");
            return;
        }

        System.out.println("\n=== DUCK STATUS ===");
        System.out.println("Hunger:     " + String.format("%.1f", currentDuck.getHunger()) + "/100");
        System.out.println("Energy:     " + String.format("%.1f", currentDuck.getEnergy()) + "/100");
        System.out.println("Cleanliness: " + String.format("%.1f", currentDuck.getCleanliness()) + "/100");
        System.out.println("Happiness:   " + String.format("%.1f", currentDuck.getHappiness()) + "/100");
        System.out.println("State:       " + currentDuck.getState());
        System.out.println("Mood:        " + currentDuck.getStatusMessage());
        System.out.println("Time Status: " + statsManager.getNightTimeStatus()); // NEW
        System.out.println("===================\n");
    }

    // Shop methods unchanged
    public boolean buyHat(int hatId) {
        // ... unchanged ...
        if (currentUser == null) return false;

        Hat hat = getHatById(hatId);
        if (hat == null) {
            System.err.println("Hat not found: " + hatId);
            return false;
        }

        if (currentUser.deductCoins(hat.getPrice())) {
            currentUser.addHat(hatId);
            db.updateUser(currentUser);
            System.out.println("Bought hat: " + hat.getName());
            return true;
        } else {
            System.err.println("Not enough coins to buy: " + hat.getName());
            return false;
        }
    }

    public boolean buyFood(int foodId, int quantity) {
        // ... unchanged ...
        if (currentUser == null) return false;

        Food food = getFoodById(foodId);
        if (food == null) {
            System.err.println("Food not found: " + foodId);
            return false;
        }

        int totalCost = food.getPrice() * quantity;
        if (currentUser.deductCoins(totalCost)) {
            db.addFoodToInventory(currentUser.getId(), foodId, quantity);
            db.updateUser(currentUser);
            System.out.println("Bought " + quantity + "x " + food.getName());
            return true;
        } else {
            System.err.println("Not enough coins to buy: " + food.getName());
            return false;
        }
    }

    // Minigame methods unchanged
    public void saveDuckDashScore(int score) {
        // ... unchanged ...
        if (currentUser == null) return;

        int coinsEarned = score / 10;
        int expEarned = score / 5;

        db.saveScore(currentUser.getId(), "duck_dash", score, coinsEarned, expEarned);

        currentUser.addCoins(coinsEarned);
        currentUser.addExperience(expEarned);
        db.updateUser(currentUser);

        int highScore = db.getUserHighScore(currentUser.getId(), "duck_dash");
        if (score > highScore) {
            System.out.println("NEW HIGH SCORE! " + score + " points!");
        }

        int rank = db.getUserRank(currentUser.getId(), "duck_dash");
        System.out.println("Duck Dash score: " + score + " points (Rank: #" + rank + ")");
    }

    public void saveCardMatchScore(int score) {
        // ... unchanged ...
        if (currentUser == null) return;

        int coinsEarned = score * 2;
        int expEarned = score;

        db.saveScore(currentUser.getId(), "card_match", score, coinsEarned, expEarned);

        currentUser.addCoins(coinsEarned);
        currentUser.addExperience(expEarned);
        db.updateUser(currentUser);

        int highScore = db.getUserHighScore(currentUser.getId(), "card_match");
        if (score > highScore) {
            System.out.println("🎉 NEW HIGH SCORE! " + score + " matches!");
        }

        int rank = db.getUserRank(currentUser.getId(), "card_match");
        System.out.println("Card Match score: " + score + " matches (Rank: #" + rank + ")");
    }

    // Utility methods unchanged
    private void loadShopItems() {
        // ... unchanged ...
        shopHats = db.getAllHats();
        shopFoods = db.getAllFoods();
        System.out.println("Loaded " + shopHats.size() + " hats and " +
                shopFoods.size() + " foods from shop");
    }

    private Hat getHatById(int hatId) {
        // ... unchanged ...
        for (Hat hat : shopHats) {
            if (hat.getId() == hatId) {
                return hat;
            }
        }
        return null;
    }

    private Food getFoodById(int foodId) {
        // ... unchanged ...
        for (Food food : shopFoods) {
            if (food.getId() == foodId) {
                return food;
            }
        }
        return null;
    }

    // Getters unchanged
    public User getUser() { return currentUser; }
    public Duck getDuck() { return currentDuck; }
    public List<Hat> getShopHats() { return shopHats; }
    public List<Food> getShopFoods() { return shopFoods; }
    public List<Score> getHighScores(String gameType) {
        return db.getHighScores(gameType, 10);
    }

    // Cleanup unchanged
    public void shutdown() {
        if (statTimer != null) {
            statTimer.cancel();
        }
        db.close();
    }
}