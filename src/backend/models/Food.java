package backend.models;

public class Food {
    private int id;
    private String name;
    private String description;
    private double price;
    private double hungerRestore;
    private double energyRestore;
    private double cleanlinessReduction;
    private double happinessBonus;

    public Food() {}

    public Food(int id, String name, String description, double price,
                double hungerRestore, double energyRestore, double cleanlinessReduction, double happinessBonus) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.hungerRestore = hungerRestore;
        this.energyRestore = energyRestore;
        this.cleanlinessReduction = cleanlinessReduction;
        this.happinessBonus = happinessBonus;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getHungerRestore() { return hungerRestore; }
    public void setHungerRestore(double hungerRestore) { this.hungerRestore = hungerRestore; }

    public double getEnergyRestore() { return energyRestore; }
    public void setEnergyRestore(double energyRestore) { this.energyRestore = energyRestore; }

    public double getCleanlinessReduction() { return cleanlinessReduction; }
    public void setCleanlinessReduction(double cleanlinessReduction) { this.cleanlinessReduction = cleanlinessReduction; }

    public double getHappinessBonus() { return happinessBonus; }
    public void setHappinessBonus(double happinessBonus) { this.happinessBonus = happinessBonus; }

    @Override
    public String toString() {
        return name + " - " + price + " coins (Hunger +" + hungerRestore +
                ", Energy +" + energyRestore +
                ", Cleanliness -" + cleanlinessReduction +
                ", Happiness +" + happinessBonus + ")";
    }
}
