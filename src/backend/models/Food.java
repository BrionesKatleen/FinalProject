package backend.models;

public class Food {
    private int id;
    private String name;
    private String description;
    private int price;
    private int hungerRestore;
    private int energyRestore;
    private int happinessBonus;

    public Food() {}

    public Food(int id, String name, String description, int price,
                int hungerRestore, int energyRestore, int happinessBonus) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.hungerRestore = hungerRestore;
        this.energyRestore = energyRestore;
        this.happinessBonus = happinessBonus;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getHungerRestore() { return hungerRestore; }
    public void setHungerRestore(int hungerRestore) { this.hungerRestore = hungerRestore; }

    public int getEnergyRestore() { return energyRestore; }
    public void setEnergyRestore(int energyRestore) { this.energyRestore = energyRestore; }

    public int getHappinessBonus() { return happinessBonus; }
    public void setHappinessBonus(int happinessBonus) { this.happinessBonus = happinessBonus; }

    @Override
    public String toString() {
        return name + " - " + price + " coins (Hunger +" + hungerRestore + ")";
    }
}