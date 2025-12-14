package database;

import backend.models.*;
import java.sql.*;
import java.util.*;

public class Database {
    private Connection connection;

    private static final String URL = "jdbc:sqlite:quackmate.db";

    public Database() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(URL);
            initializeDatabase();
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    // ====================
    // DATABASE INITIALIZATION
    // ====================
    private void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Users table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "username TEXT UNIQUE NOT NULL, " +
                            "password_hash TEXT NOT NULL, " +
                            "coins INTEGER DEFAULT 100, " +  // New users get 100 credits
                            "level INTEGER DEFAULT 1, " +
                            "experience INTEGER DEFAULT 0, " +
                            "hat_inventory TEXT DEFAULT '', " +
                            "food_inventory TEXT DEFAULT '{}', " +
                            "last_login_time INTEGER DEFAULT (strftime('%s', 'now') * 1000)" +
                            ")"
            );

            // Ducks table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS ducks (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "user_id INTEGER UNIQUE NOT NULL, " +
                            "energy REAL DEFAULT 100.0, " +
                            "hunger REAL DEFAULT 100.0, " +
                            "cleanliness REAL DEFAULT 100.0, " +
                            "happiness REAL DEFAULT 100.0, " +
                            "state TEXT DEFAULT 'IDLE', " +
                            "equipped_hat_id INTEGER, " +
                            "last_updated_time INTEGER DEFAULT (strftime('%s', 'now') * 1000), " +
                            "last_sleep_check INTEGER DEFAULT (strftime('%s', 'now') * 1000), " +
                            "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                            ")"
            );

            // Hats table (Only special hats with level requirements)
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS hats (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT NOT NULL, " +
                            "description TEXT, " +
                            "price INTEGER NOT NULL, " +
                            "required_level INTEGER NOT NULL, " +  // Level requirement
                            "experience_reward INTEGER NOT NULL, " +  // XP reward when buying
                            "stat_multiplier REAL NOT NULL" +  // Multiplier for stats improvement
                            ")"
            );

            // Food table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS foods (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT NOT NULL, " +
                            "description TEXT, " +
                            "price INTEGER NOT NULL, " +
                            "hunger_restore REAL NOT NULL, " +  // Changed to REAL for decimal values
                            "energy_restore REAL NOT NULL, " +
                            "cleanliness_effect REAL NOT NULL, " +  // Negative effect on cleanliness
                            "happiness_bonus REAL NOT NULL" +
                            ")"
            );

            // Insert default hats (only the 3 special hats)
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM hats");
            if (rs.getInt("count") == 0) {
                insertDefaultHats();
            }

            // Insert default foods (Peas, Bird Seeds, Oats, Corn)
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM foods");
            if (rs.getInt("count") == 0) {
                insertDefaultFoods();
            }
        }
    }

    private void insertDefaultHats() throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO hats (name, description, price, required_level, experience_reward, stat_multiplier) VALUES (?, ?, ?, ?, ?, ?)")) {

            // Frog Hat - Level 5 - 300 credits
            pstmt.setString(1, "Frog");
            pstmt.setString(2, "A cute frog hat that gives 0.02x stat boost and +50 XP");
            pstmt.setInt(3, 300);
            pstmt.setInt(4, 5);  // Required level
            pstmt.setInt(5, 50);  // XP reward
            pstmt.setDouble(6, 0.02);  // Stat multiplier
            pstmt.executeUpdate();

            // Cat Hat - Level 10 - 500 credits
            pstmt.setString(1, "Cat");
            pstmt.setString(2, "A cool cat hat that gives 0.03x stat boost and +100 XP");
            pstmt.setInt(3, 500);
            pstmt.setInt(4, 10);  // Required level
            pstmt.setInt(5, 100);  // XP reward
            pstmt.setDouble(6, 0.03);  // Stat multiplier
            pstmt.executeUpdate();

            // Stitches Hat - Level 20 - 1000 credits
            pstmt.setString(1, "Stitches");
            pstmt.setString(2, "An epic stitches hat that gives 0.05x stat boost and +200 XP");
            pstmt.setInt(3, 1000);
            pstmt.setInt(4, 20);  // Required level
            pstmt.setInt(5, 200);  // XP reward
            pstmt.setDouble(6, 0.05);  // Stat multiplier
            pstmt.executeUpdate();
        }
    }

    private void insertDefaultFoods() throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO foods (name, description, price, hunger_restore, energy_restore, cleanliness_effect, happiness_bonus) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            // Peas - 5 credits
            pstmt.setString(1, "Peas");
            pstmt.setString(2, "Simple peas that ducks love to eat");
            pstmt.setInt(3, 5);
            pstmt.setDouble(4, 0.5);    // Hunger +0.5
            pstmt.setDouble(5, 0.5);    // Energy +0.5
            pstmt.setDouble(6, -0.05);  // Cleanliness -0.05
            pstmt.setDouble(7, 0.05);   // Happiness +0.05
            pstmt.executeUpdate();

            // Bird Seeds - 5 credits
            pstmt.setString(1, "Bird Seeds");
            pstmt.setString(2, "Nutritious bird seeds for your duck");
            pstmt.setInt(3, 5);
            pstmt.setDouble(4, 0.5);    // Hunger +0.5
            pstmt.setDouble(5, 0.5);    // Energy +0.5
            pstmt.setDouble(6, -0.05);  // Cleanliness -0.05
            pstmt.setDouble(7, 0.05);   // Happiness +0.05
            pstmt.executeUpdate();

            // Oats - 10 credits
            pstmt.setString(1, "Oats");
            pstmt.setString(2, "Healthy oats for energy");
            pstmt.setInt(3, 10);
            pstmt.setDouble(4, 0.8);    // Hunger +0.8
            pstmt.setDouble(5, 0.8);    // Energy +0.8
            pstmt.setDouble(6, -0.05);  // Cleanliness -0.05
            pstmt.setDouble(7, 0.08);   // Happiness +0.08
            pstmt.executeUpdate();

            // Corn - 10 credits
            pstmt.setString(1, "Corn");
            pstmt.setString(2, "Sweet corn kernels");
            pstmt.setInt(3, 10);
            pstmt.setDouble(4, 0.8);    // Hunger +0.8
            pstmt.setDouble(5, 0.8);    // Energy +0.8
            pstmt.setDouble(6, -0.05);  // Cleanliness -0.05
            pstmt.setDouble(7, 0.08);   // Happiness +0.08
            pstmt.executeUpdate();
        }
    }

    // ====================
    // USER OPERATIONS
    // ====================
    public User getUser(String username, String passwordHash) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                );

                user.setCoins(rs.getInt("coins"));
                user.setLevel(rs.getInt("level"));
                user.setExperience(rs.getInt("experience"));
                user.setFoodInventory(rs.getString("food_inventory"));
                user.setLastLoginTime(rs.getLong("last_login_time"));

                // Parse hat inventory
                String hatInventory = rs.getString("hat_inventory");
                if (hatInventory != null && !hatInventory.isEmpty()) {
                    List<Integer> ownedHatIds = new ArrayList<>();
                    String[] hatIds = hatInventory.split(",");
                    for (String hatId : hatIds) {
                        try {
                            ownedHatIds.add(Integer.parseInt(hatId.trim()));
                        } catch (NumberFormatException e) {
                            // Skip invalid entries
                        }
                    }
                    user.setOwnedHatIds(ownedHatIds);
                }

                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
        return null;
    }

    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) as count FROM users WHERE username = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt("count") > 0;
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }

    public int createUser(String username, String passwordHash) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
        return -1;
    }

    public void updateUser(User user) {
        String sql = "UPDATE users SET coins = ?, level = ?, experience = ?, " +
                "hat_inventory = ?, food_inventory = ?, last_login_time = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user.getCoins());
            pstmt.setInt(2, user.getLevel());
            pstmt.setInt(3, user.getExperience());

            // Convert ownedHatIds list to comma-separated string
            List<Integer> ownedHats = user.getOwnedHatIds();
            StringBuilder hatInventory = new StringBuilder();
            for (int i = 0; i < ownedHats.size(); i++) {
                hatInventory.append(ownedHats.get(i));
                if (i < ownedHats.size() - 1) {
                    hatInventory.append(",");
                }
            }
            pstmt.setString(4, hatInventory.toString());

            pstmt.setString(5, user.getFoodInventory());
            pstmt.setLong(6, user.getLastLoginTime());
            pstmt.setInt(7, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }

    // ====================
    // DUCK OPERATIONS
    // ====================
    public Duck getDuck(int userId) {
        String sql = "SELECT * FROM ducks WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Duck duck = new Duck(userId);
                duck.setId(rs.getInt("id"));
                duck.setEnergy(rs.getDouble("energy"));
                duck.setHunger(rs.getDouble("hunger"));
                duck.setCleanliness(rs.getDouble("cleanliness"));
                duck.setHappiness(rs.getDouble("happiness"));
                duck.setState(rs.getString("state"));

                int equippedHatId = rs.getInt("equipped_hat_id");
                if (!rs.wasNull()) {
                    duck.setEquippedHatId(equippedHatId);
                }

                duck.setLastUpdatedTime(rs.getLong("last_updated_time"));
                duck.setLastSleepCheck(rs.getLong("last_sleep_check"));
                return duck;
            }
        } catch (SQLException e) {
            System.err.println("Error getting duck: " + e.getMessage());
        }
        return null;
    }

    public void createDuck(Duck duck) {
        String sql = "INSERT INTO ducks (user_id, energy, hunger, cleanliness, happiness, " +
                "state, equipped_hat_id, last_updated_time, last_sleep_check) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, duck.getUserId());
            pstmt.setDouble(2, duck.getEnergy());
            pstmt.setDouble(3, duck.getHunger());
            pstmt.setDouble(4, duck.getCleanliness());
            pstmt.setDouble(5, duck.getHappiness());
            pstmt.setString(6, duck.getState());

            if (duck.getEquippedHatId() != null) {
                pstmt.setInt(7, duck.getEquippedHatId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            pstmt.setLong(8, duck.getLastUpdatedTime());
            pstmt.setLong(9, duck.getLastSleepCheck());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                duck.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Error creating duck: " + e.getMessage());
        }
    }

    public void updateDuck(Duck duck) {
        String sql = "UPDATE ducks SET energy = ?, hunger = ?, cleanliness = ?, " +
                "happiness = ?, state = ?, equipped_hat_id = ?, " +
                "last_updated_time = ?, last_sleep_check = ? WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, duck.getEnergy());
            pstmt.setDouble(2, duck.getHunger());
            pstmt.setDouble(3, duck.getCleanliness());
            pstmt.setDouble(4, duck.getHappiness());
            pstmt.setString(5, duck.getState());

            if (duck.getEquippedHatId() != null) {
                pstmt.setInt(6, duck.getEquippedHatId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setLong(7, duck.getLastUpdatedTime());
            pstmt.setLong(8, duck.getLastSleepCheck());
            pstmt.setInt(9, duck.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating duck: " + e.getMessage());
        }
    }

    // ====================
    // SHOP OPERATIONS
    // ====================
    public List<Hat> getAllHats() {
        List<Hat> hats = new ArrayList<>();
        String sql = "SELECT * FROM hats ORDER BY required_level, price";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Hat hat = new Hat(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("price"),
                        "SPECIAL"  // Hardcoded rarity since it's not in the table
                );
                hats.add(hat);
            }
        } catch (SQLException e) {
            System.err.println("Error getting hats: " + e.getMessage());
        }
        return hats;
    }

    public List<Food> getAllFoods() {
        List<Food> foods = new ArrayList<>();
        String sql = "SELECT * FROM foods ORDER BY price";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Note: Food constructor expects ints, but we have doubles in database
                // We'll cast them to ints for now
                Food food = new Food(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("price"),
                        (int) rs.getDouble("hunger_restore"),  // Cast to int
                        (int) rs.getDouble("energy_restore"),  // Cast to int
                        (int) rs.getDouble("happiness_bonus")  // Cast to int
                );
                foods.add(food);
            }
        } catch (SQLException e) {
            System.err.println("Error getting foods: " + e.getMessage());
        }
        return foods;
    }

    public Hat getHatById(int hatId) {
        String sql = "SELECT * FROM hats WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, hatId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Hat hat = new Hat(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("price"),
                        "SPECIAL"  // Hardcoded rarity
                );
                return hat;
            }
        } catch (SQLException e) {
            System.err.println("Error getting hat: " + e.getMessage());
        }
        return null;
    }

    public Food getFoodById(int foodId) {
        String sql = "SELECT * FROM foods WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, foodId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Note: Cast doubles to ints to match Food constructor
                Food food = new Food(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("price"),
                        (int) rs.getDouble("hunger_restore"),  // Cast to int
                        (int) rs.getDouble("energy_restore"),  // Cast to int
                        (int) rs.getDouble("happiness_bonus")  // Cast to int
                );
                return food;
            }
        } catch (SQLException e) {
            System.err.println("Error getting food: " + e.getMessage());
        }
        return null;
    }

    // New method to get hat with all details including level requirement and stat multiplier
    public Map<String, Object> getHatDetails(int hatId) {
        String sql = "SELECT * FROM hats WHERE id = ?";
        Map<String, Object> hatDetails = new HashMap<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, hatId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                hatDetails.put("id", rs.getInt("id"));
                hatDetails.put("name", rs.getString("name"));
                hatDetails.put("description", rs.getString("description"));
                hatDetails.put("price", rs.getInt("price"));
                hatDetails.put("required_level", rs.getInt("required_level"));
                hatDetails.put("experience_reward", rs.getInt("experience_reward"));
                hatDetails.put("stat_multiplier", rs.getDouble("stat_multiplier"));
                return hatDetails;
            }
        } catch (SQLException e) {
            System.err.println("Error getting hat details: " + e.getMessage());
        }
        return null;
    }

    // New method to get food with all details including cleanliness effect
    public Map<String, Object> getFoodDetails(int foodId) {
        String sql = "SELECT * FROM foods WHERE id = ?";
        Map<String, Object> foodDetails = new HashMap<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, foodId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                foodDetails.put("id", rs.getInt("id"));
                foodDetails.put("name", rs.getString("name"));
                foodDetails.put("description", rs.getString("description"));
                foodDetails.put("price", rs.getInt("price"));
                foodDetails.put("hunger_restore", rs.getDouble("hunger_restore"));
                foodDetails.put("energy_restore", rs.getDouble("energy_restore"));
                foodDetails.put("cleanliness_effect", rs.getDouble("cleanliness_effect"));
                foodDetails.put("happiness_bonus", rs.getDouble("happiness_bonus"));
                return foodDetails;
            }
        } catch (SQLException e) {
            System.err.println("Error getting food details: " + e.getMessage());
        }
        return null;
    }

    // ====================
    // INVENTORY OPERATIONS
    // ====================
    public boolean useFoodFromInventory(int userId, int foodId, int quantity) {
        User user = getUserById(userId);
        if (user == null) return false;

        String inventory = user.getFoodInventory();
        if (inventory == null || inventory.isEmpty() || inventory.equals("{}")) {
            return false;
        }

        // Parse inventory string
        if (inventory.startsWith("{") && inventory.endsWith("}")) {
            inventory = inventory.substring(1, inventory.length() - 1);
        }

        Map<Integer, Integer> foodMap = new HashMap<>();
        if (!inventory.isEmpty()) {
            String[] items = inventory.split(",");

            for (String item : items) {
                if (item.isEmpty()) continue;
                String[] parts = item.split(":");
                if (parts.length == 2) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        int qty = Integer.parseInt(parts[1].trim());
                        foodMap.put(id, qty);
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
        }

        // Check if user has enough of this food
        if (!foodMap.containsKey(foodId) || foodMap.get(foodId) < quantity) {
            return false;
        }

        // Update quantity
        int newQty = foodMap.get(foodId) - quantity;
        if (newQty <= 0) {
            foodMap.remove(foodId);
        } else {
            foodMap.put(foodId, newQty);
        }

        // Rebuild inventory string
        StringBuilder newInventory = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : foodMap.entrySet()) {
            newInventory.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }

        // Remove trailing comma if present
        String updatedInventory = newInventory.length() > 0 ?
                newInventory.substring(0, newInventory.length() - 1) : "";

        // Update user in database
        user.setFoodInventory(updatedInventory);
        updateUser(user);
        return true;
    }

    // Helper method to get user by ID
    private User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                );

                user.setCoins(rs.getInt("coins"));
                user.setLevel(rs.getInt("level"));
                user.setExperience(rs.getInt("experience"));
                user.setFoodInventory(rs.getString("food_inventory"));
                user.setLastLoginTime(rs.getLong("last_login_time"));

                // Parse hat inventory
                String hatInventory = rs.getString("hat_inventory");
                if (hatInventory != null && !hatInventory.isEmpty()) {
                    List<Integer> ownedHatIds = new ArrayList<>();
                    String[] hatIds = hatInventory.split(",");
                    for (String hatId : hatIds) {
                        try {
                            ownedHatIds.add(Integer.parseInt(hatId.trim()));
                        } catch (NumberFormatException e) {
                            // Skip invalid entries
                        }
                    }
                    user.setOwnedHatIds(ownedHatIds);
                }

                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        return null;
    }

    // ====================
    // CLEANUP
    // ====================
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}