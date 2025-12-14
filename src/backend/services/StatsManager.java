package backend.services;

import backend.models.Duck;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class StatsManager {

    // FIXED: Adjusted decay rates to be much slower (per minute)
    private static final double IDLE_ENERGY = -1.0;      // Was -2.5
    private static final double IDLE_HUNGER = -1.0;      // Was -2.0
    private static final double IDLE_CLEANLINESS = -1.0; // Was -2.0
    private static final double IDLE_HAPPINESS = -2.0;   // Was -2.0

    private static final double PLAYING_ENERGY = -3.0;   // Playing consumes energy
    private static final double PLAYING_HUNGER = -3.0;
    private static final double PLAYING_CLEANLINESS = -3.0;
    private static final double PLAYING_HAPPINESS = +5.0; // Playing makes duck happy

    private static final double SLEEPING_ENERGY = +50.0;   // Sleeping restores energy
    private static final double SLEEPING_HUNGER = -2.;

//    private static final double EATING_ENERGY = +2.0;
//    private static final double EATING_HUNGER = +15.0;    // Eating significantly reduces hunger
//    private static final double EATING_HAPPINESS = +3.0;

//    private static final double BATHING_CLEANLINESS = +40.0; // Bathing significantly increases cleanliness
//    private static final double BATHING_HAPPINESS = +3.0;

    private static final double NIGHTTIME_ENERGY_DECAY = -0.2;

    // Nighttime duration in minutes
    private static final int NIGHTTIME_DURATION_MINUTES = 30;

    // Daytime duration in minutes (assuming equal day/night cycle)
    private static final int DAYTIME_DURATION_MINUTES = 30;

    // Auto-wake threshold
    private static final double AUTO_WAKE_THRESHOLD = 100.0;

    /**
     * Apply stat decay based on real time passed
     * This is the core logic for your enhancement
     */
    public void applyStatDecay(Duck duck) {
        long currentTime = System.currentTimeMillis();
        long minutesPassed = (currentTime - duck.getLastUpdatedTime()) / (60 * 1000);

        if (minutesPassed > 0) {
            System.out.println("[STATS MANAGER] Applying decay for " + minutesPassed + " minute(s)");

            // Store old values for comparison
            double oldEnergy = duck.getEnergy();
            double oldHunger = duck.getHunger();
            double oldCleanliness = duck.getCleanliness();
            double oldHappiness = duck.getHappiness();
            String oldState = duck.getState();

            // Apply decay for each minute passed
            for (int i = 0; i < minutesPassed; i++) {
                applyMinuteDecay(duck);

                // Check auto-wake (once per minute)
                checkAutoWake(duck);
            }

            // Update the timestamp
            duck.setLastUpdatedTime(currentTime);

            // Log changes
            logStatChanges(duck, oldEnergy, oldHunger, oldCleanliness, oldHappiness, oldState);
        }
    }

    private void applyMinuteDecay(Duck duck) {
        String state = duck.getState();

        switch (state) {
            case "IDLE":
                applyIdleDecay(duck);
                break;
            case "PLAYING":
                applyPlayingDecay(duck);
                break;
            case "SLEEPING":
                applySleepingDecay(duck);
                break;
        }

        // Apply nighttime decay if it's nighttime
        if (isNightTime()) {
            double currentEnergy = duck.getEnergy();
            duck.setEnergy(Math.max(0, currentEnergy + NIGHTTIME_ENERGY_DECAY));
        }

        // Ensure stats stay in bounds
        clampStats(duck);
    }

    private void applyIdleDecay(Duck duck) {
        duck.setEnergy(duck.getEnergy() + IDLE_ENERGY);
        duck.setHunger(duck.getHunger() + IDLE_HUNGER);
        duck.setCleanliness(duck.getCleanliness() + IDLE_CLEANLINESS);
        duck.setHappiness(duck.getHappiness() + IDLE_HAPPINESS);
    }

    private void applyPlayingDecay(Duck duck) {
        duck.setEnergy(duck.getEnergy() + PLAYING_ENERGY);
        duck.setHunger(duck.getHunger() + PLAYING_HUNGER);
        duck.setCleanliness(duck.getCleanliness() + PLAYING_CLEANLINESS);
        duck.setHappiness(duck.getHappiness() + PLAYING_HAPPINESS);
    }

    private void applySleepingDecay(Duck duck) {
        duck.setEnergy(duck.getEnergy() + SLEEPING_ENERGY);
        duck.setHunger(duck.getHunger() + SLEEPING_HUNGER);
    }

    private void checkAutoWake(Duck duck) {
        if ("SLEEPING".equals(duck.getState()) && duck.getEnergy() >= AUTO_WAKE_THRESHOLD) {
            duck.setState("IDLE");
            System.out.println("Auto-wake triggered (energy ≥ " + AUTO_WAKE_THRESHOLD + ")");
        }
    }

    private void clampStats(Duck duck) {
        duck.setEnergy(Math.max(0, Math.min(100, duck.getEnergy())));
        duck.setHunger(Math.max(0, Math.min(100, duck.getHunger())));
        duck.setCleanliness(Math.max(0, Math.min(100, duck.getCleanliness())));
        duck.setHappiness(Math.max(0, Math.min(100, duck.getHappiness())));
    }

    private void logStatChanges(Duck duck, double oldEnergy, double oldHunger,
                                double oldCleanliness, double oldHappiness, String oldState) {
        System.out.println(String.format("Energy: %.1f → %.1f", oldEnergy, duck.getEnergy()));
        System.out.println(String.format("Hunger: %.1f → %.1f", oldHunger, duck.getHunger()));
        System.out.println(String.format("Cleanliness: %.1f → %.1f", oldCleanliness, duck.getCleanliness()));
        System.out.println(String.format("Happiness: %.1f → %.1f", oldHappiness, duck.getHappiness()));

        if (!oldState.equals(duck.getState())) {
            System.out.println("State: " + oldState + " → " + duck.getState());
        }
    }

    // Action methods - FIXED to use proper values
    public void feedDuck(Duck duck) {
        duck.setState("EATING");
        duck.setEnergy(Math.min(100, duck.getEnergy() + 20));
        duck.setHunger(Math.min(100, duck.getHunger() + 40));
        duck.setHappiness(Math.min(100, duck.getHappiness() + 15));
        System.out.println("[FEED] Duck was fed");
    }

    public void playWithDuck(Duck duck) {
        duck.setState("PLAYING");
        applyPlayingDecay(duck);
//        duck.setEnergy(Math.max(0, duck.getEnergy() - 10));
//        duck.setHunger(Math.max(0, duck.getHunger() - 5));
//        duck.setHappiness(Math.min(100, duck.getHappiness() + 25));
        System.out.println("[PLAY] Played with duck");
    }

    public void batheDuck(Duck duck) {
        duck.setState("BATHING");
        duck.setCleanliness(100);
        duck.setHappiness(Math.min(100, duck.getHappiness() + 20));
        System.out.println("[BATHE] Duck was cleaned");
    }

    public void sleepDuck(Duck duck) {
        duck.setState("SLEEPING");
        System.out.println("[SLEEP] Duck put to sleep");
        applySleepingDecay(duck);
    }

    // Time-based methods
    public boolean isNightTime() {
        // Simple implementation: night from 8 PM to 6 AM (20:00 - 06:00)
        int hour = java.time.LocalTime.now().getHour();
        return hour >= 20 || hour < 6;
    }

    public String getTimeStatus(Duck duck) {
        if (duck == null) return "No duck";

        long currentTime = System.currentTimeMillis();
        long lastUpdate = duck.getLastUpdatedTime();
        long minutesSinceUpdate = (currentTime - lastUpdate) / (60 * 1000);

        if (isNightTime()) {
            return String.format("Nighttime (updated %d min ago)", minutesSinceUpdate);
        } else {
            return String.format("Daytime (updated %d min ago)", minutesSinceUpdate);
        }
    }
}


//package backend.services;
//
//import java.util.*;

//public class StatsManager {
//
//    // ==============================
//    // STAT DECAY LOGIC (0–100 scale)
//    // ==============================
//    public Map<String, Double> applyStatDecay(Map<String, Double> stats) {
//
//        long currentTime = System.currentTimeMillis();
//
//        // Safety: new duck or missing timestamp
//        if (!stats.containsKey("last_updated") || stats.get("last_updated") <= 0) {
//            stats.put("last_updated", (double) currentTime);
//            return stats;
//        }
//
//        long lastUpdated = stats.get("last_updated").longValue();
//        long hoursPassed = (currentTime - lastUpdated) / (60 * 60 * 1000);
//
//        if (hoursPassed > 0) {
//            double decay = 5.0 * hoursPassed; // 5 points per hour
//
//            stats.put("happiness", stats.get("happiness") - decay * 0.5);
//            stats.put("hunger", stats.get("hunger") + decay);
//            stats.put("energy", stats.get("energy") - decay);
//            stats.put("cleanliness", stats.get("cleanliness") - decay * 0.3);
//
//            System.out.println("Applied decay for " + hoursPassed + " hours");
//        }
//
//        clampStats(stats);
//        stats.put("last_updated", (double) currentTime);
//        return stats;
//    }
//
//    // ==============================
//    // DUCK ACTION LOGIC
//    // ==============================
//    public Map<String, Double> feedDuck(Map<String, Double> stats, int foodType) {
//
//        stats.put("hunger", stats.get("hunger") - 20);
//        stats.put("happiness", stats.get("happiness") + 10);
//
//        switch (foodType) {
//            case 0: // peas
//            case 1: // birdseed
//                stats.put("energy", stats.get("energy") + 5);
//                break;
//
//            case 2: // corn
//            case 3: // oats
//                stats.put("energy", stats.get("energy") + 8);
//                stats.put("cleanliness", stats.get("cleanliness") - 2);
//                break;
//        }
//
//        clampStats(stats);
//        return stats;
//    }
//
//    public Map<String, Double> cleanDuck(Map<String, Double> stats) {
//        stats.put("cleanliness", stats.get("cleanliness") + 30);
//        stats.put("happiness", stats.get("happiness") + 5);
//        clampStats(stats);
//        return stats;
//    }
//
//    public Map<String, Double> playWithDuck(Map<String, Double> stats) {
//        stats.put("happiness", stats.get("happiness") + 15);
//        stats.put("energy", stats.get("energy") - 10);
//        stats.put("hunger", stats.get("hunger") + 5);
//        clampStats(stats);
//        return stats;
//    }
//
//    public Map<String, Double> sleepDuck(Map<String, Double> stats) {
//        stats.put("energy", stats.get("energy") + 25);
//        stats.put("happiness", stats.get("happiness") + 5);
//        clampStats(stats);
//        return stats;
//    }
//
//    public Map<String, Double> toggleNightMode(Map<String, Double> stats, boolean isNight) {
//        stats.put("is_night_mode", isNight ? 100.0 : 0.0);
//
//        if (isNight) {
//            // Night mode reduces energy slightly
//            stats.put("energy", stats.get("energy") - 5);
//        }
//
//        clampStats(stats);
//        return stats;
//    }
//
//    public Map<String, Double> equipOutfit(Map<String, Double> stats, int outfitIndex) {
//        stats.put("outfit_index", (double) outfitIndex);
//        return stats;
//    }
//
//    // ==============================
//    // FOOD PURCHASE LOGIC
//    // ==============================
//    public boolean canBuyFood(int currentCredits, int foodType, int quantity) {
//        int price = 10; // Each food costs 10 credits
//        int totalCost = price * quantity;
//        return currentCredits >= totalCost;
//    }
//
//    public int calculateFoodCost(int foodType, int quantity) {
//        return 10 * quantity;
//    }
//
//    public int[] updateFoodQuantities(int[] quantities, int foodType, int quantityChange) {
//        if (foodType >= 0 && foodType < 4) {
//            quantities[foodType] = Math.max(0, quantities[foodType] + quantityChange);
//        }
//        return quantities;
//    }
//
//    // ==============================
//    // MEMORY GAME LOGIC
//    // ==============================
//    public int calculateCreditsEarned(int score, String difficulty) {
//        int multiplier = switch (difficulty.toLowerCase()) {
//            case "easy" -> 2;
//            case "medium" -> 3;
//            case "hard" -> 5;
//            default -> 1;
//        };
//        return score * multiplier;
//    }
//
//    public int calculateExperience(int score) {
//        return score / 5; // 1 exp per 5 points
//    }
//
//    // ==============================
//    // UTILITY METHODS
//    // ==============================
//    private void clampStats(Map<String, Double> stats) {
//        stats.put("happiness", Math.max(0.0, Math.min(100.0, stats.get("happiness"))));
//        stats.put("hunger", Math.max(0.0, Math.min(100.0, stats.get("hunger"))));
//        stats.put("energy", Math.max(0.0, Math.min(100.0, stats.get("energy"))));
//        stats.put("cleanliness", Math.max(0.0, Math.min(100.0, stats.get("cleanliness"))));
//    }
//
//    public String getStatusMessage(Map<String, Double> stats) {
//        if (stats.get("hunger") < 15) return "I'm starving!";
//        if (stats.get("energy") < 15) return "I'm exhausted!";
//        if (stats.get("cleanliness") < 20) return "I'm so dirty!";
//        if (stats.get("happiness") < 25) return "I'm feeling sad...";
//        if (stats.get("hunger") < 50) return "I could eat something.";
//        if (stats.get("energy") < 50) return "Feeling a bit tired.";
//        if (stats.get("cleanliness") < 60) return "I could use a bath.";
//        if (stats.get("happiness") < 60) return "Let's play!";
//        return "I'm feeling great!";
//    }
//}


//package backend.services;
//
//import java.util.*;
//
//public class StatsManager {
//
//    // === STAT DECAY LOGIC ===
//    public Map<String, Double> applyStatDecay(Map<String, Double> stats) {
//        long lastUpdated = stats.get("last_updated").longValue();
//        long currentTime = System.currentTimeMillis();
//        long hoursPassed = (currentTime - lastUpdated) / (60 * 60 * 1000);
//
//        if (hoursPassed > 0) {
//            double decay = 0.05 * hoursPassed;
//
//            // Apply decay
//            stats.put("happiness", Math.max(0.0, stats.get("happiness") - decay * 0.5));
//            stats.put("hunger", Math.min(0.0, stats.get("hunger") + decay));
//            stats.put("energy", Math.max(0.0, stats.get("energy") - decay));
//            stats.put("cleanliness", Math.max(0.0, stats.get("cleanliness") - decay * 0.3));
//
//            System.out.println("Applied decay for " + hoursPassed + " hours");
//        }
//
//        // Clamp all stats between 0.0 and 1.0
//        clampStats(stats);
//        stats.put("last_updated", (double) System.currentTimeMillis());
//
//        return stats;
//    }
//
//    // === DUCK ACTION LOGIC ===
//    public Map<String, Double> feedDuck(Map<String, Double> stats, int foodType) {
//        // Reduce hunger, increase happiness
//        stats.put("hunger", Math.max(1.0, stats.get("hunger") - 0.2));
//        stats.put("happiness", Math.min(1.0, stats.get("happiness") + 0.1));
//
//        // Different food types have different effects
//        switch (foodType) {
//            case 0: // peas
//            case 1: // birdseed
//                stats.put("energy", Math.min(1.0, stats.get("energy") + 0.05));
//                break;
//            case 2: // corn
//            case 3: // oats
//                stats.put("energy", Math.min(1.0, stats.get("energy") + 0.08));
//                stats.put("cleanliness", Math.max(0.0, stats.get("cleanliness") - 0.02));
//                break;
//        }
//
//        clampStats(stats);
//        return stats;
//    }
//
//    public Map<String, Double> cleanDuck(Map<String, Double> stats) {
//        stats.put("cleanliness", Math.min(1.0, stats.get("cleanliness") + 0.3));
//        stats.put("happiness", Math.min(1.0, stats.get("happiness") + 0.05));
//        clampStats(stats);
//        return stats;
//    }
//
//    public Map<String, Double> playWithDuck(Map<String, Double> stats) {
//        stats.put("happiness", Math.min(1.0, stats.get("happiness") + 0.15));
//        stats.put("energy", Math.max(1.0, stats.get("energy") - 0.1));
//        stats.put("hunger", Math.min(1.0, stats.get("hunger") + 0.05));
//        clampStats(stats);
//        return stats;
//    }
//
//    public Map<String, Double> sleepDuck(Map<String, Double> stats) {
//        stats.put("energy", Math.min(1.0, stats.get("energy") + 0.25));
//        stats.put("happiness", Math.min(1.0, stats.get("happiness") + 0.05));
//        clampStats(stats);
//        return stats;
//    }
//
//    public Map<String, Double> toggleNightMode(Map<String, Double> stats, boolean isNight) {
//        stats.put("is_night_mode", isNight ? 1.0 : 0.0);
//
//        if (isNight) {
//            // Night mode effects
//            stats.put("energy", Math.max(0.0, stats.get("energy") - 0.05));
//        }
//
//        clampStats(stats);
//        return stats;
//    }
//
//    public Map<String, Double> equipOutfit(Map<String, Double> stats, int outfitIndex) {
//        stats.put("outfit_index", (double) outfitIndex);
//        return stats;
//    }
//
//    // === FOOD PURCHASE LOGIC ===
//    public boolean canBuyFood(int currentCredits, int foodType, int quantity) {
//        int price = 10; // Each food costs 10 credits
//        int totalCost = price * quantity;
//        return currentCredits >= totalCost;
//    }
//
//    public int calculateFoodCost(int foodType, int quantity) {
//        return 10 * quantity; // 10 credits per food item
//    }
//
//    public int[] updateFoodQuantities(int[] quantities, int foodType, int quantityChange) {
//        if (foodType >= 0 && foodType < 4) {
//            quantities[foodType] = Math.max(0, quantities[foodType] + quantityChange);
//        }
//        return quantities;
//    }
//
//    // === MEMORY GAME LOGIC ===
//    public int calculateCreditsEarned(int score, String difficulty) {
//        int multiplier = switch(difficulty.toLowerCase()) {
//            case "easy" -> 2;
//            case "medium" -> 3;
//            case "hard" -> 5;
//            default -> 1;
//        };
//        return score * multiplier;
//    }
//
//    public int calculateExperience(int score) {
//        return score / 5; // 1 exp per 5 points
//    }
//
//    // === UTILITY METHODS ===
//    private void clampStats(Map<String, Double> stats) {
//        stats.put("happiness", Math.max(1.0, Math.min(0.0, stats.get("happiness"))));
//        stats.put("hunger", Math.max(1.0, Math.min(0.0, stats.get("hunger"))));
//        stats.put("energy", Math.max(1.0, Math.min(0.0, stats.get("energy"))));
//        stats.put("cleanliness", Math.max(1.0, Math.min(0.0, stats.get("cleanliness"))));
//    }
//
//    public String getStatusMessage(Map<String, Double> stats) {
//        if (stats.get("hunger") < 0.15) return "I'm starving!";
//        if (stats.get("energy") < 0.15) return "I'm exhausted!";
//        if (stats.get("cleanliness") < 0.20) return "I'm so dirty!";
//        if (stats.get("happiness") < 0.25) return "I'm feeling sad...";
//        if (stats.get("hunger") < 0.50) return "I could eat something.";
//        if (stats.get("energy") < 0.50) return "Feeling a bit tired.";
//        if (stats.get("cleanliness") < 0.60) return "I could use a bath.";
//        if (stats.get("happiness") < 0.60) return "Let's play!";
//        return "I'm feeling great!";
//    }
//}

//package backend.services;
//
//import backend.models.Duck;
//import java.time.LocalTime;
//
//public class StatsManager {
//
//    // Decay rates per minute (using your exact values)
//    private static final double IDLE_ENERGY = -0.025 * 100;
//    private static final double IDLE_HUNGER = -0.02 * 100;
//    private static final double IDLE_CLEANLINESS = -0.02 * 100;
//    private static final double IDLE_HAPPINESS = -0.02 * 100;
//
//    private static final double PLAYING_ENERGY = 0.4 * 100;
//    private static final double PLAYING_HUNGER = -0.03 * 100;
//    private static final double PLAYING_CLEANLINESS = -0.025 * 100;
//    private static final double PLAYING_HAPPINESS = 0.3 * 100;
//
//    private static final double SLEEPING_ENERGY = 0.09 * 100;
//    private static final double SLEEPING_HUNGER = -0.02 *100;
//
//    private static final double EATING_ENERGY = 0.2 * 100;
//    private static final double EATING_HUNGER = 0.5 * 100;
//    private static final double EATING_HAPPINESS = 0.2 * 100;
//
//    private static final double BATHING_CLEANLINESS = 0.6 * 100;
//    private static final double BATHING_HAPPINESS = 0.2 *100;
//
//    // Nighttime decay (23:00 - 05:00)
//    private static final double NIGHTTIME_ENERGY_DECAY = -0.1 * 100;
//
//    // Auto-sleep threshold
//    private static final double AUTO_SLEEP_THRESHOLD = 0.05 * 100;
//
//    /**
//     * Apply stat decay based on real time passed
//     * This is the core logic for your enhancement
//     */
//    public void applyStatDecay(Duck duck) {
//        long currentTime = System.currentTimeMillis();
//        long minutesPassed = (currentTime - duck.getLastUpdatedTime()) / (60 * 1000);
//
//        if (minutesPassed > 0) {
//            System.out.println("[STATS MANAGER] Applying decay for " + minutesPassed + " minute(s)");
//
//            // Store old values for comparison
//            double oldEnergy = duck.getEnergy();
//            double oldHunger = duck.getHunger();
//            double oldCleanliness = duck.getCleanliness();
//            double oldHappiness = duck.getHappiness();
//            String oldState = duck.getState();
//
//            // Apply decay for each minute passed
//            for (int i = 0; i < minutesPassed; i++) {
//                applyMinuteDecay(duck);
//
//                // Check auto-sleep (once per minute)
//                checkAutoSleep(duck);
//            }
//
//            // Update the timestamp
//            duck.setLastUpdatedTime(currentTime);
//
//            // Log changes
//            logStatChanges(duck, oldEnergy, oldHunger, oldCleanliness, oldHappiness, oldState);
//        }
//    }
//
//    private void applyMinuteDecay(Duck duck) {
//        String state = duck.getState();
//
//        switch (state) {
//            case "IDLE":
//                applyIdleDecay(duck);
//                break;
//            case "PLAYING":
//                applyPlayingDecay(duck);
//                break;
//            case "SLEEPING":
//                applySleepingDecay(duck);
//                break;
//            case "EATING":
//                applyEatingDecay(duck);
//                break;
//            case "BATHING":
//                applyBathingDecay(duck);
//                break;
//        }
//
//        // Apply nighttime decay (affects all states)
//        if (isNightTime()) {
//            duck.setEnergy(duck.getEnergy() + NIGHTTIME_ENERGY_DECAY);
//            System.out.println("Nighttime: Extra -0.1 energy decay applied");
//        }
//
//        // Clamp all stats (0-100)
//        clampStats(duck);
//    }
//
//    private void applyIdleDecay(Duck duck) {
//        duck.setEnergy(duck.getEnergy() + IDLE_ENERGY);
//        duck.setHunger(duck.getHunger() + IDLE_HUNGER);
//        duck.setCleanliness(duck.getCleanliness() + IDLE_CLEANLINESS);
//        duck.setHappiness(duck.getHappiness() + IDLE_HAPPINESS);
//    }
//
//    private void applyPlayingDecay(Duck duck) {
//        duck.setEnergy(duck.getEnergy() + PLAYING_ENERGY);
//        duck.setHunger(duck.getHunger() + PLAYING_HUNGER);
//        duck.setCleanliness(duck.getCleanliness() + PLAYING_CLEANLINESS);
//        duck.setHappiness(duck.getHappiness() + PLAYING_HAPPINESS);
//    }
//
//    private void applySleepingDecay(Duck duck) {
//        duck.setEnergy(duck.getEnergy() + SLEEPING_ENERGY);
//        duck.setHunger(duck.getHunger() + SLEEPING_HUNGER);
//        // No cleanliness or happiness changes while sleeping
//    }
//
//    private void applyEatingDecay(Duck duck) {
//        // This is actually a boost, not decay
//        duck.setEnergy(duck.getEnergy() + EATING_ENERGY);
//        duck.setHunger(duck.getHunger() + EATING_HUNGER);
//        duck.setHappiness(duck.getHappiness() + EATING_HAPPINESS);
//    }
//
//    private void applyBathingDecay(Duck duck) {
//        // This is actually a boost, not decay
//        duck.setCleanliness(duck.getCleanliness() + BATHING_CLEANLINESS);
//        duck.setHappiness(duck.getHappiness() + BATHING_HAPPINESS);
//    }
//
//    private void checkAutoSleep(Duck duck) {
//        long currentTime = System.currentTimeMillis();
//        long minutesSinceLastCheck = (currentTime - duck.getLastSleepCheck()) / (60 * 1000);
//
//        if (minutesSinceLastCheck >= 1) {
//            // Only check auto-sleep once per minute
//            duck.setLastSleepCheck(currentTime);
//
//            if (!duck.getState().equals("SLEEPING") && duck.getEnergy() <= AUTO_SLEEP_THRESHOLD) {
//                duck.setState("SLEEPING");
//                System.out.println("Auto-sleep activated (energy ≤ 0.5)");
//            }
//
//            // Auto-wake if energy is restored
//            if (duck.getState().equals("SLEEPING") && duck.getEnergy() >= 95.0) {
//                duck.setState("IDLE");
//                System.out.println("Auto-wake (energy ≥ 95)");
//            }
//        }
//    }
//
//    private void clampStats(Duck duck) {
//        duck.setEnergy(duck.getEnergy()); // Already clamped in setter
//        duck.setHunger(duck.getHunger());
//        duck.setCleanliness(duck.getCleanliness());
//        duck.setHappiness(duck.getHappiness());
//    }
//
//    private void logStatChanges(Duck duck, double oldEnergy, double oldHunger,
//                                double oldCleanliness, double oldHappiness, String oldState) {
//        boolean changed = false;
//
//        if (oldEnergy != duck.getEnergy()) {
//            System.out.println("Energy: " + String.format("%.1f", oldEnergy) + " → " +
//                    String.format("%.1f", duck.getEnergy()));
//            changed = true;
//        }
//        if (oldHunger != duck.getHunger()) {
//            System.out.println("Hunger: " + String.format("%.1f", oldHunger) + " → " +
//                    String.format("%.1f", duck.getHunger()));
//            changed = true;
//        }
//        if (oldCleanliness != duck.getCleanliness()) {
//            System.out.println("Cleanliness: " + String.format("%.1f", oldCleanliness) + " → " +
//                    String.format("%.1f", duck.getCleanliness()));
//            changed = true;
//        }
//        if (oldHappiness != duck.getHappiness()) {
//            System.out.println("Happiness: " + String.format("%.1f", oldHappiness) + " → " +
//                    String.format("%.1f", duck.getHappiness()));
//            changed = true;
//        }
//        if (!oldState.equals(duck.getState())) {
//            System.out.println("State: " + oldState + " → " + duck.getState());
//            changed = true;
//        }
//
//        if (!changed) {
//            System.out.println("No stat changes this interval");
//        }
//    }
//
//    // Action methods
//    public void feedDuck(Duck duck) {
//        duck.setState("EATING");
//        duck.setEnergy(duck.getEnergy() + EATING_ENERGY);
//        duck.setHunger(duck.getHunger() + EATING_HUNGER);
//        duck.setHappiness(duck.getHappiness() + EATING_HAPPINESS);
//        clampStats(duck);
//
//        System.out.println("[FEED] Applied food boosts");
//    }
//
//    public void playWithDuck(Duck duck) {
//        duck.setState("PLAYING");
//        duck.setEnergy(duck.getEnergy() + PLAYING_ENERGY);
//        duck.setHunger(duck.getHunger() + PLAYING_HUNGER);
//        duck.setCleanliness(duck.getCleanliness() + PLAYING_CLEANLINESS);
//        duck.setHappiness(duck.getHappiness() + PLAYING_HAPPINESS);
//        clampStats(duck);
//
//        System.out.println("[PLAY] Applied playing effects");
//    }
//
//    public void batheDuck(Duck duck) {
//        duck.setState("BATHING");
//        duck.setCleanliness(duck.getCleanliness() + BATHING_CLEANLINESS);
//        duck.setHappiness(duck.getHappiness() + BATHING_HAPPINESS);
//        clampStats(duck);
//
//        System.out.println("[BATHE] Applied bath restores");
//    }
//
//    public void sleepDuck(Duck duck) {
//        duck.setState("SLEEPING");
//        System.out.println("[SLEEP] Duck manually put to sleep");
//    }
//
//    // Nighttime check
//    public boolean isNightTime() {
//        LocalTime now = LocalTime.now();
//        LocalTime nightStart = LocalTime.of(23, 0); // 11:00 PM
//        LocalTime nightEnd = LocalTime.of(5, 0);    // 5:00 AM
//
//        if (nightStart.isAfter(nightEnd)) {
//            // Night spans across midnight
//            return now.isAfter(nightStart) || now.isBefore(nightEnd);
//        } else {
//            return now.isAfter(nightStart) && now.isBefore(nightEnd);
//        }
//    }
//
//    // Get current nighttime status for UI
//    public String getNightTimeStatus() {
//        if (isNightTime()) {
//            return "Nighttime (23:00-05:00): -0.1 energy/min";
//        }
//        return "Daytime";
//    }
//}
