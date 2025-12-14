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
    // STAT DECAY TIMER
    // ====================
    private void startStatTimer() {
        statTimer = new Timer();
        statTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentDuck != null) {
                    statsManager.applyStatDecay(currentDuck);
                    db.updateDuck(currentDuck);

                    if (currentDuck.needsAttention()) {
                        System.out.println("[ALERT] Duck needs attention: " +
                                currentDuck.getStatusMessage());
                    }

                    if (!currentDuck.isFine()) {
                        System.out.println("[CRITICAL] Duck has died! Stats are too low!");
                    }
                }
            }
        }, 0, 60000); // Every 1 minute
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

            // Create duck if none exists
            if (currentDuck == null) {
                currentDuck = new Duck(user.getId());
                db.createDuck(currentDuck);
                System.out.println("New duck created for player!");
            }

            statsManager.applyStatDecay(currentDuck);
            db.updateDuck(currentDuck);

            System.out.println("Welcome! " + currentDuck.getStatusMessage());
            System.out.println("Last updated: " + new Date(currentDuck.getLastUpdatedTime()));
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

        if (userId > 0) return login(username, password);
        return false;
    }

    private String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }

    // ====================
    // DUCK ACTIONS
    // ====================
    public void feedDuck(int foodId) {
        if (currentDuck == null || currentUser == null) {
            System.out.println("No duck found! Please login first.");
            return;
        }

        Food food = getFoodById(foodId);
        if (food != null) {
            if (db.useFoodFromInventory(currentUser.getId(), foodId, 1)) {
                statsManager.feedDuck(currentDuck);

                currentDuck.setHunger(currentDuck.getHunger() + food.getHungerRestore());
                currentDuck.setHappiness(currentDuck.getHappiness() + food.getHappinessBonus());
                if (food.getEnergyRestore() > 0)
                    currentDuck.setEnergy(currentDuck.getEnergy() + food.getEnergyRestore());

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

        statsManager.batheDuck(currentDuck);
        currentDuck.setLastUpdatedTime(System.currentTimeMillis());

        currentUser.addCoins(2);

        db.updateDuck(currentDuck);
        db.updateUser(currentUser);

        System.out.println("Duck cleaned! " + currentDuck.getStatusMessage());
    }

    public void playWithDuck() {
        if (currentDuck == null || currentUser == null) return;

        statsManager.playWithDuck(currentDuck);
        currentDuck.setLastUpdatedTime(System.currentTimeMillis());

        currentUser.addCoins(5);

        db.updateDuck(currentDuck);
        db.updateUser(currentUser);

        System.out.println("Played with duck! " + currentDuck.getStatusMessage());
    }

    public void putDuckToSleep() {
        if (currentDuck == null) return;

        statsManager.sleepDuck(currentDuck);
        currentDuck.setLastUpdatedTime(System.currentTimeMillis());
        db.updateDuck(currentDuck);

        System.out.println("Duck is sleeping... Zzz");
    }

    public void printDuckStatus() {
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
        System.out.println("===================\n");
    }

    // ====================
    // SHOP METHODS
    // ====================
    private void loadShopItems() {
        shopHats = db.getAllHats();
        shopFoods = db.getAllFoods();
        System.out.println("Loaded " + shopHats.size() + " hats and " + shopFoods.size() + " foods from shop");
    }

    public boolean buyHat(int hatId) {
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
        if (currentUser == null) return false;

        Food food = getFoodById(foodId);
        if (food == null) {
            System.err.println("Food not found: " + foodId);
            return false;
        }

        int totalCost = food.getPrice() * quantity;
        if (currentUser.deductCoins(totalCost)) {
            // Add food to user's inventory
            String inv = currentUser.getFoodInventory();
            StringBuilder sb = new StringBuilder(inv.isEmpty() ? "" : inv + ",");
            sb.append(foodId).append(":").append(quantity);
            currentUser.setFoodInventory(sb.toString());

            db.updateUser(currentUser);
            System.out.println("Bought " + quantity + "x " + food.getName());
            return true;
        } else {
            System.err.println("Not enough coins to buy: " + food.getName());
            return false;
        }
    }

    private Hat getHatById(int hatId) {
        for (Hat hat : shopHats) {
            if (hat.getId() == hatId) return hat;
        }
        return null;
    }

    private Food getFoodById(int foodId) {
        for (Food food : shopFoods) {
            if (food.getId() == foodId) return food;
        }
        return null;
    }

    // ====================
    // GETTERS
    // ====================
    public User getUser() { return currentUser; }
    public Duck getDuck() { return currentDuck; }
    public List<Hat> getShopHats() { return shopHats; }
    public List<Food> getShopFoods() { return shopFoods; }

    // ====================
    // CLEANUP
    // ====================
    public void shutdown() {
        if (statTimer != null) statTimer.cancel();
        db.close();
    }
}
