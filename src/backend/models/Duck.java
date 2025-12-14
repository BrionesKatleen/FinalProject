//package backend.models;
//enum STATE{
//    IDLE, SLEEPING, EATING, BATHING, PLAYING
//}
//public class Duck {
//    private int id;
//    private int userId;
//    private String name;
//    private double hunger;        // 0-100
//    private double energy;        // 0-100
//    private double cleanliness;   // 0-100
//    private double happiness;     // 0-100
//    private String state;         // IDLE, PLAYING, SLEEPING, EATING, BATHING
//    private Integer equippedHatId;
//    private long lastUpdated;
//
//    public Duck() {
//        this.hunger = 100.0;
//        this.energy = 100.0;
//        this.cleanliness = 100.0;
//        this.happiness = 100.0;
//        this.state = state;
//        this.lastUpdated = System.currentTimeMillis();
//    }
//
//    public Duck(int userId, String name) {
//        this();
//        this.userId = userId;
//        this.name = (name != null && !name.isEmpty()) ? name : "Quacky";
//    }
//
//    // Getters and Setters with clamping
//    public int getId() { return id; }
//    public void setId(int id) { this.id = id; }
//
//    public int getUserId() { return userId; }
//    public void setUserId(int userId) { this.userId = userId; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public double getHunger() { return hunger; }
//    public void setHunger(double hunger) {
//        this.hunger = Math.max(0, Math.min(100, hunger));
//    }
//
//    public double getEnergy() { return energy; }
//    public void setEnergy(double energy) {
//        this.energy = Math.max(0, Math.min(100, energy));
//    }
//
//    public double getCleanliness() { return cleanliness; }
//    public void setCleanliness(double cleanliness) {
//        this.cleanliness = Math.max(0, Math.min(100, cleanliness));
//    }
//
//    public double getHappiness() { return happiness; }
//    public void setHappiness(double happiness) {
//        this.happiness = Math.max(0, Math.min(100, happiness));
//    }
//
//    public String getState() { return state; }
//    public void setState(String state) { this.state = state; }
//
//    public Integer getEquippedHatId() { return equippedHatId; }
//    public void setEquippedHatId(Integer equippedHatId) {
//        this.equippedHatId = equippedHatId;
//    }
//
//    public long getLastUpdated() { return lastUpdated; }
//    public void setLastUpdated(long lastUpdated) {
//        this.lastUpdated = lastUpdated;
//    }
//
//    // Simple status check (no logic, just data)
//    public boolean isAlive() {
//        return hunger > 0 && energy > 0 && happiness > 0;
//    }
//
//    @Override
//    public String toString() {
//        return String.format("Duck[H:%.1f,E:%.1f,C:%.1f,Hp:%.1f]",
//                hunger, energy, cleanliness, happiness);
//    }
//}

package backend.models;

public class Duck {
    private int id;
    private int userId; // FOREIGN KEY
//    private String name;

    // Stats (0-100 scale)
    private double energy = 100.0;
    private double hunger = 100.0;
    private double cleanliness = 100.0;
    private double happiness = 100.0;

    private String state = "IDLE"; // IDLE, PLAYING, SLEEPING, EATING, BATHING
    private Integer equippedHatId;

    private long lastUpdatedTime; // Last time stats were updated
    private long lastSleepCheck;  // Last time auto-sleep was checked

    public Duck() {
        this.lastUpdatedTime = System.currentTimeMillis();
        this.lastSleepCheck = System.currentTimeMillis();
    }

    public Duck(int userId) {
        this();
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }

    public double getEnergy() { return energy; }
    public void setEnergy(double energy) {
        this.energy = clamp(energy, 0.0, 100.0);
    }

    public double getHunger() { return hunger; }
    public void setHunger(double hunger) {
        this.hunger = clamp(hunger, 0.0, 100.0);
    }

    public double getCleanliness() { return cleanliness; }
    public void setCleanliness(double cleanliness) {
        this.cleanliness = clamp(cleanliness, 0.0, 100.0);
    }

    public double getHappiness() { return happiness; }
    public void setHappiness(double happiness) {
        this.happiness = clamp(happiness, 0.0, 100.0);
    }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Integer getEquippedHatId() { return equippedHatId; }
    public void setEquippedHatId(Integer equippedHatId) {
        this.equippedHatId = equippedHatId;
    }

    public long getLastUpdatedTime() { return lastUpdatedTime; }
    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public long getLastSleepCheck() { return lastSleepCheck; }
    public void setLastSleepCheck(long lastSleepCheck) {
        this.lastSleepCheck = lastSleepCheck;
    }

    // Status checks (preserved from original)
    public boolean isFine() {
        return hunger > 10 && energy > 10 && happiness > 10 && cleanliness > 10; // TRACKS IF THE DUCK IS IN THE RIGHT CONDITION
    }

    public boolean needsAttention() {
        return hunger < 30 || energy < 30 || cleanliness < 30 || happiness < 30;
    }

    public String getStatusMessage() {
        if (hunger < 10) return "I'm starving!";
        if (energy < 10) return "I'm exhausted!";
        if (cleanliness < 10) return "I'm so dirty!";
        if (happiness < 15) return "I'm feeling sad...";
        if (hunger < 30) return "I could eat something.";
        if (energy < 20) return "Feeling a bit tired.";
        if (cleanliness < 30) return "I could use a bath.";
        if (happiness < 70) return "Let's play!";
        return "I'm feeling great!";
    }

    // Helper method for clamping values
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public String toString() {
        return String.format("Duck [H: %.1f, E: %.1f, C: %.1f, Hp: %.1f, State: %s]",
                hunger, energy, cleanliness, happiness, state);
    }
}