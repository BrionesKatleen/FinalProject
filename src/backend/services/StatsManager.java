package backend.services;

import backend.models.Duck;
import java.time.LocalTime;

public class StatsManager {

    // Decay rates per minute (using your exact values)
    private static final double IDLE_ENERGY = -0.025 * 100;
    private static final double IDLE_HUNGER = -0.02 * 100;
    private static final double IDLE_CLEANLINESS = -0.02 * 100;
    private static final double IDLE_HAPPINESS = -0.02 * 100;

    private static final double PLAYING_ENERGY = 0.4 * 100;
    private static final double PLAYING_HUNGER = -0.03 * 100;
    private static final double PLAYING_CLEANLINESS = -0.025 * 100;
    private static final double PLAYING_HAPPINESS = 0.3 * 100;

    private static final double SLEEPING_ENERGY = 0.09 * 100;
    private static final double SLEEPING_HUNGER = -0.02 *100;

    private static final double EATING_ENERGY = 0.2 * 100;
    private static final double EATING_HUNGER = 0.5 * 100;
    private static final double EATING_HAPPINESS = 0.2 * 100;

    private static final double BATHING_CLEANLINESS = 0.6 * 100;
    private static final double BATHING_HAPPINESS = 0.2 *100;

    // Nighttime decay (23:00 - 05:00)
    private static final double NIGHTTIME_ENERGY_DECAY = -0.1 * 100;

    // Auto-sleep threshold
    private static final double AUTO_SLEEP_THRESHOLD = 0.05 * 100;

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

                // Check auto-sleep (once per minute)
                checkAutoSleep(duck);
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
            case "EATING":
                applyEatingDecay(duck);
                break;
            case "BATHING":
                applyBathingDecay(duck);
                break;
        }

        // Apply nighttime decay (affects all states)
        if (isNightTime()) {
            duck.setEnergy(duck.getEnergy() + NIGHTTIME_ENERGY_DECAY);
            System.out.println("Nighttime: Extra -0.1 energy decay applied");
        }

        // Clamp all stats (0-100)
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
        // No cleanliness or happiness changes while sleeping
    }

    private void applyEatingDecay(Duck duck) {
        // This is actually a boost, not decay
        duck.setEnergy(duck.getEnergy() + EATING_ENERGY);
        duck.setHunger(duck.getHunger() + EATING_HUNGER);
        duck.setHappiness(duck.getHappiness() + EATING_HAPPINESS);
    }

    private void applyBathingDecay(Duck duck) {
        // This is actually a boost, not decay
        duck.setCleanliness(duck.getCleanliness() + BATHING_CLEANLINESS);
        duck.setHappiness(duck.getHappiness() + BATHING_HAPPINESS);
    }

    private void checkAutoSleep(Duck duck) {
        long currentTime = System.currentTimeMillis();
        long minutesSinceLastCheck = (currentTime - duck.getLastSleepCheck()) / (60 * 1000);

        if (minutesSinceLastCheck >= 1) {
            // Only check auto-sleep once per minute
            duck.setLastSleepCheck(currentTime);

            if (!duck.getState().equals("SLEEPING") && duck.getEnergy() <= AUTO_SLEEP_THRESHOLD) {
                duck.setState("SLEEPING");
                System.out.println("Auto-sleep activated (energy ≤ 0.5)");
            }

            // Auto-wake if energy is restored
            if (duck.getState().equals("SLEEPING") && duck.getEnergy() >= 95.0) {
                duck.setState("IDLE");
                System.out.println("Auto-wake (energy ≥ 95)");
            }
        }
    }

    private void clampStats(Duck duck) {
        duck.setEnergy(duck.getEnergy()); // Already clamped in setter
        duck.setHunger(duck.getHunger());
        duck.setCleanliness(duck.getCleanliness());
        duck.setHappiness(duck.getHappiness());
    }

    private void logStatChanges(Duck duck, double oldEnergy, double oldHunger,
                                double oldCleanliness, double oldHappiness, String oldState) {
        boolean changed = false;

        if (oldEnergy != duck.getEnergy()) {
            System.out.println("Energy: " + String.format("%.1f", oldEnergy) + " → " +
                    String.format("%.1f", duck.getEnergy()));
            changed = true;
        }
        if (oldHunger != duck.getHunger()) {
            System.out.println("Hunger: " + String.format("%.1f", oldHunger) + " → " +
                    String.format("%.1f", duck.getHunger()));
            changed = true;
        }
        if (oldCleanliness != duck.getCleanliness()) {
            System.out.println("Cleanliness: " + String.format("%.1f", oldCleanliness) + " → " +
                    String.format("%.1f", duck.getCleanliness()));
            changed = true;
        }
        if (oldHappiness != duck.getHappiness()) {
            System.out.println("Happiness: " + String.format("%.1f", oldHappiness) + " → " +
                    String.format("%.1f", duck.getHappiness()));
            changed = true;
        }
        if (!oldState.equals(duck.getState())) {
            System.out.println("State: " + oldState + " → " + duck.getState());
            changed = true;
        }

        if (!changed) {
            System.out.println("No stat changes this interval");
        }
    }

    // Action methods
    public void feedDuck(Duck duck) {
        duck.setState("EATING");
        duck.setEnergy(duck.getEnergy() + EATING_ENERGY);
        duck.setHunger(duck.getHunger() + EATING_HUNGER);
        duck.setHappiness(duck.getHappiness() + EATING_HAPPINESS);
        clampStats(duck);

        System.out.println("[FEED] Applied food boosts");
    }

    public void playWithDuck(Duck duck) {
        duck.setState("PLAYING");
        duck.setEnergy(duck.getEnergy() + PLAYING_ENERGY);
        duck.setHunger(duck.getHunger() + PLAYING_HUNGER);
        duck.setCleanliness(duck.getCleanliness() + PLAYING_CLEANLINESS);
        duck.setHappiness(duck.getHappiness() + PLAYING_HAPPINESS);
        clampStats(duck);

        System.out.println("[PLAY] Applied playing effects");
    }

    public void batheDuck(Duck duck) {
        duck.setState("BATHING");
        duck.setCleanliness(duck.getCleanliness() + BATHING_CLEANLINESS);
        duck.setHappiness(duck.getHappiness() + BATHING_HAPPINESS);
        clampStats(duck);

        System.out.println("[BATHE] Applied bath restores");
    }

    public void sleepDuck(Duck duck) {
        duck.setState("SLEEPING");
        System.out.println("[SLEEP] Duck manually put to sleep");
    }

    // Nighttime check
    public boolean isNightTime() {
        LocalTime now = LocalTime.now();
        LocalTime nightStart = LocalTime.of(23, 0); // 11:00 PM
        LocalTime nightEnd = LocalTime.of(5, 0);    // 5:00 AM

        if (nightStart.isAfter(nightEnd)) {
            // Night spans across midnight
            return now.isAfter(nightStart) || now.isBefore(nightEnd);
        } else {
            return now.isAfter(nightStart) && now.isBefore(nightEnd);
        }
    }

    // Get current nighttime status for UI
    public String getNightTimeStatus() {
        if (isNightTime()) {
            return "Nighttime (23:00-05:00): -0.1 energy/min";
        }
        return "Daytime";
    }
}
