package backend.models;

public class Hat {
    private int id;
    private String name;
    private String description;
    private int price;
    private String rarity;

    public Hat() {}

    public Hat(int id, String name, String description, int price, String rarity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.rarity = rarity;
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

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    @Override
    public String toString() {
        return name + " (" + rarity + ") - " + price + " coins";
    }
}