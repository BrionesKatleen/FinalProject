package backend.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private int coins;
    private int level;
    private int experience;
    private List<Integer> ownedHatIds;
    private String foodInventory;
    private long lastLoginTime;

    // DEFAULT USER DATA
    public User() {
        this.coins = 100;
        this.level = 1;
        this.experience = 0;
        this.ownedHatIds = new ArrayList<>();
        this.foodInventory = "{}";
        this.lastLoginTime = System.currentTimeMillis();
    }

    // CONSTRUCTOR FOR LOGIN & SIGN UP
    public User(int id, String username, String passwordHash) {
        this();
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public List<Integer> getOwnedHatIds() { return ownedHatIds; }
    public void setOwnedHatIds(List<Integer> ownedHatIds) { this.ownedHatIds = ownedHatIds; }

    public String getFoodInventory() { return foodInventory; }
    public void setFoodInventory(String foodInventory) { this.foodInventory = foodInventory; }

    public long getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(long lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    // HELPER METHODS
    public void addCoins(int amount) {
        this.coins += amount;
    }

    public boolean deductCoins(int amount) {
        if (this.coins >= amount) {
            this.coins -= amount;
            return true;
        }
        return false;
    }

    public void addExperience(int amount) {
        this.experience += amount;
        checkLevelUp();
    }

    private void checkLevelUp() {
        int requiredExp = level * 100;
        while (experience >= requiredExp) {
            level++;
            experience -= requiredExp;
            requiredExp = level * 100;

            // Level up rewards
            coins += level * 50; // GIVES +50 COINS
            System.out.println("🎉 Level up! Now level " + level + "! Rewarded " + (level * 50) + " coins!");
        }
    }

    public void addHat(int hatId) {
        if (!ownedHatIds.contains(hatId)) {
            ownedHatIds.add(hatId);
        }
    }

    public boolean ownsHat(int hatId) {
        return ownedHatIds.contains(hatId);
    }

    public void removeHat(int hatId) {
        ownedHatIds.remove(Integer.valueOf(hatId));
    }

    public void updateLoginTime() {
        this.lastLoginTime = System.currentTimeMillis();
    }

    public long getTimeSinceLastLogin() {
        return System.currentTimeMillis() - lastLoginTime;
    }

    public int getMinutesSinceLastLogin() {
        return (int) (getTimeSinceLastLogin() / (60 * 1000));
    }

    @Override
    public String toString() {
        return username + " (Lvl " + level + ", " + coins + " coins, " + experience + " XP)";
    }
}
