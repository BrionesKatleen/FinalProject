package backend.models;

public class Hat {
    private int id;
    private String name;
    private String description;
    private int price;
    private int lvlRequired;

    public Hat() {}

    public Hat(int id, String name, String description, int price, int lvlRequired) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.lvlRequired = lvlRequired;
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

    public int getLvlRequired() { return lvlRequired; }
    public void setLvlRequired(int lvlRequired) { this.lvlRequired = lvlRequired; }

}