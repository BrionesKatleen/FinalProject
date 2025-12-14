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
                            "cleanliness_reduction REAL NOT NULL, " +  // Negative effect on cleanliness
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
                "INSERT INTO foods (name, description, price, hunger_restore, energy_restore, cleanliness_reduction, happiness_bonus) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            // Peas - 5 credits
            pstmt.setString(1, "Peas");
            pstmt.setString(2, "Simple peas that ducks love to eat");
            pstmt.setInt(3, 5);
            pstmt.setDouble(4, 0.5);    // Hunger +0.5
            pstmt.setDouble(5, 0.3);    // Energy +0.5
            pstmt.setDouble(6, -0.05);  // Cleanliness -0.05
            pstmt.setDouble(7, 0.05);   // Happiness +0.05
            pstmt.executeUpdate();

            // Bird Seeds - 5 credits
            pstmt.setString(1, "Bird Seeds");
            pstmt.setString(2, "Nutritious bird seeds for your duck");
            pstmt.setInt(3, 5);
            pstmt.setDouble(4, 0.5);    // Hunger +0.5
            pstmt.setDouble(5, 0.3);    // Energy +0.5
            pstmt.setDouble(6, -0.05);  // Cleanliness -0.05
            pstmt.setDouble(7, 0.05);   // Happiness +0.05
            pstmt.executeUpdate();

            // Oats - 10 credits
            pstmt.setString(1, "Oats");
            pstmt.setString(2, "Healthy oats for energy");
            pstmt.setInt(3, 10);
            pstmt.setDouble(4, 0.8);    // Hunger +0.8
            pstmt.setDouble(5, 0.4);    // Energy +0.8
            pstmt.setDouble(6, -0.05);  // Cleanliness -0.05
            pstmt.setDouble(7, 0.08);   // Happiness +0.08
            pstmt.executeUpdate();

            // Corn - 10 credits
            pstmt.setString(1, "Corn");
            pstmt.setString(2, "Sweet corn kernels");
            pstmt.setInt(3, 10);
            pstmt.setDouble(4, 0.8);    // Hunger +0.8
            pstmt.setDouble(5, 0.4);    // Energy +0.8
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
                        rs.getInt("required_level") //
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
                        (double) rs.getDouble("hunger_restore"),
                        (double) rs.getDouble("energy_restore"),
                        (double) rs.getDouble("cleanliness_reduction"),
                        (double) rs.getDouble("happiness_bonus")
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
                        rs.getInt("required_level")
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
                        (double) rs.getDouble("hunger_restore"),
                        (double) rs.getDouble("energy_restore"),
                        (double) rs.getDouble("cleanliness_reduction"),
                        (double) rs.getDouble("happiness_bonus")
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
                foodDetails.put("cleanliness_reduction", rs.getDouble("cleanliness_reduction"));
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

//package database;
//
//import java.sql.*;
//import java.util.*;
//
//public class Database {
//    private static final String DB_URL = "jdbc:sqlite:quackmate.db";
//    private Connection conn;
//
//    public Database() {
//        try {
//            Class.forName("org.sqlite.JDBC");
//            conn = DriverManager.getConnection(DB_URL);
//            createTables();
//            System.out.println("Database initialized!");
//        } catch (Exception e) {
//            System.err.println("Database error: " + e.getMessage());
//        }
//    }
//
//    private void createTables() throws SQLException {
//        Statement stmt = conn.createStatement();
//
//        // Users table
//        stmt.execute("""
//            CREATE TABLE IF NOT EXISTS users (
//                id INTEGER PRIMARY KEY AUTOINCREMENT,
//                username TEXT UNIQUE NOT NULL,
//                password TEXT NOT NULL,
//                credits INTEGER DEFAULT 100,
//                level INTEGER DEFAULT 1,
//                experience INTEGER DEFAULT 0,
//                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
//            )
//        """);
//
//        // Duck stats
//        stmt.execute("""
//            CREATE TABLE IF NOT EXISTS duck_stats (
//                user_id INTEGER UNIQUE,
//                happiness REAL DEFAULT 0.60,
//                hunger REAL DEFAULT 0.70,
//                energy REAL DEFAULT 0.10,
//                cleanliness REAL DEFAULT 0.20,
//                outfit_index INTEGER DEFAULT 0,
//                is_night_mode BOOLEAN DEFAULT FALSE,
//                last_updated INTEGER DEFAULT 0,
//                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
//            )
//        """);
//
//        // Food inventory
//        stmt.execute("""
//            CREATE TABLE IF NOT EXISTS food_inventory (
//                user_id INTEGER,
//                food_type INTEGER,
//                quantity INTEGER DEFAULT 0,
//                PRIMARY KEY (user_id, food_type),
//                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
//            )
//        """);
//
//        // Memory game scores
//        stmt.execute("""
//            CREATE TABLE IF NOT EXISTS memory_scores (
//                user_id INTEGER,
//                difficulty TEXT,
//                score INTEGER,
//                credits_earned INTEGER,
//                mistakes INTEGER,
//                played_at DATETIME DEFAULT CURRENT_TIMESTAMP,
//                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
//            )
//        """);
//
//        stmt.close();
//    }
//
//    // === USER OPERATIONS ===
//    public boolean createUser(String username, String password) {
//        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            pstmt.setString(2, password);
//            pstmt.executeUpdate();
//            return true;
//        } catch (SQLException e) {
//            return false;
//        }
//    }
//
//    public boolean authenticate(String username, String password) {
//        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            pstmt.setString(2, password);
//            return pstmt.executeQuery().next();
//        } catch (SQLException e) {
//            return false;
//        }
//    }
//
//    public int getUserId(String username) {
//        String sql = "SELECT id FROM users WHERE username = ?";
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            ResultSet rs = pstmt.executeQuery();
//            return rs.next() ? rs.getInt("id") : -1;
//        } catch (SQLException e) {
//            return -1;
//        }
//    }
//
//    public int getUserCredits(String username) {
//        String sql = "SELECT credits FROM users WHERE username = ?";
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            ResultSet rs = pstmt.executeQuery();
//            return rs.next() ? rs.getInt("credits") : 100;
//        } catch (SQLException e) {
//            return 100;
//        }
//    }
//
//    public int getUserLevel(String username) {
//        String sql = "SELECT level FROM users WHERE username = ?";
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            ResultSet rs = pstmt.executeQuery();
//            return rs.next() ? rs.getInt("level") : 1;
//        } catch (SQLException e) {
//            return 1;
//        }
//    }
//
//    public void updateUserCredits(String username, int credits) {
//        String sql = "UPDATE users SET credits = ? WHERE username = ?";
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, credits);
//            pstmt.setString(2, username);
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("Update credits error: " + e.getMessage());
//        }
//    }
//
//    public void updateUserLevel(String username, int level) {
//        String sql = "UPDATE users SET level = ? WHERE username = ?";
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, level);
//            pstmt.setString(2, username);
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("Update level error: " + e.getMessage());
//        }
//    }
//
//    // === DUCK OPERATIONS ===
//    public void createDuck(int userId) {
//        String sql = """
//                        INSERT INTO duck_stats (user_id, happiness, hunger, energy, cleanliness, last_updated) VALUES (?, ?, ?, ?, ?, ?)
//                        """;
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, userId);
//            pstmt.setDouble(2, 1.0); // happiness
//            pstmt.setDouble(3, 0.0); // hunger
//            pstmt.setDouble(4, 1.0); // energy
//            pstmt.setDouble(5, 1.0); // cleanliness
//            pstmt.setLong(6, System.currentTimeMillis());
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("Create duck error: " + e.getMessage());
//        }
//    }
//
//    public void initFood(int userId) {
//        int[] quantities = {4, 3, 5, 2};
//        String sql = "INSERT INTO food_inventory (user_id, food_type, quantity) VALUES (?, ?, ?)";
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            for (int i = 0; i < 4; i++) {
//                pstmt.setInt(1, userId);
//                pstmt.setInt(2, i);
//                pstmt.setInt(3, quantities[i]);
//                pstmt.addBatch();
//            }
//            pstmt.executeBatch();
//        } catch (SQLException e) {
//            System.err.println("Init food error: " + e.getMessage());
//        }
//    }
//
//    public Map<String, Double> getDuckStats(String username) {
//        Map<String, Double> stats = new HashMap<>();
//        String sql = "SELECT * FROM duck_stats WHERE user_id = (SELECT id FROM users WHERE username = ?)";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            ResultSet rs = pstmt.executeQuery();
//
//            if (rs.next()) {
//                stats.put("happiness", rs.getDouble("happiness"));
//                stats.put("hunger", rs.getDouble("hunger"));
//                stats.put("energy", rs.getDouble("energy"));
//                stats.put("cleanliness", rs.getDouble("cleanliness"));
//                stats.put("outfit_index", (double) rs.getInt("outfit_index"));
//                stats.put("is_night_mode", rs.getBoolean("is_night_mode") ? 1.0 : 0.0);
//                stats.put("last_updated", (double) rs.getLong("last_updated"));
//            }
//        } catch (SQLException e) {
//            System.err.println("Get duck stats error: " + e.getMessage());
//        }
//        return stats;
//    }
//
//    public void updateDuckStats(String username, Map<String, Double> stats) {
//        String sql = """
//            UPDATE duck_stats
//            SET happiness = ?, hunger = ?, energy = ?, cleanliness = ?,
//                outfit_index = ?, is_night_mode = ?, last_updated = ?
//            WHERE user_id = (SELECT id FROM users WHERE username = ?)
//        """;
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setDouble(1, stats.get("happiness"));
//            pstmt.setDouble(2, stats.get("hunger"));
//            pstmt.setDouble(3, stats.get("energy"));
//            pstmt.setDouble(4, stats.get("cleanliness"));
//            pstmt.setInt(5, stats.get("outfit_index").intValue());
//            pstmt.setBoolean(6, stats.get("is_night_mode") == 1.0);
//            pstmt.setLong(7, System.currentTimeMillis());
//            pstmt.setString(8, username);
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("Update duck stats error: " + e.getMessage());
//        }
//    }
//
//    // === FOOD OPERATIONS ===
//    public int[] getFoodQuantities(String username) {
//        int[] quantities = {4, 3, 5, 2};
//        String sql = "SELECT food_type, quantity FROM food_inventory WHERE user_id = (SELECT id FROM users WHERE username = ?)";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            ResultSet rs = pstmt.executeQuery();
//
//            while (rs.next()) {
//                int type = rs.getInt("food_type");
//                if (type >= 0 && type < 4) {
//                    quantities[type] = rs.getInt("quantity");
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Get food quantities error: " + e.getMessage());
//        }
//        return quantities;
//    }
//
//    public void updateFoodQuantity(String username, int foodType, int quantity) {
//        String sql = "UPDATE food_inventory SET quantity = ? WHERE user_id = (SELECT id FROM users WHERE username = ?) AND food_type = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, quantity);
//            pstmt.setString(2, username);
//            pstmt.setInt(3, foodType);
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("Update food error: " + e.getMessage());
//        }
//    }
//
//    // === GAME SCORES ===
//    public void saveMemoryScore(String username, String difficulty, int score, int creditsEarned, int mistakes) {
//        String sql = "INSERT INTO memory_scores (user_id, difficulty, score, credits_earned, mistakes) VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, ?)";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            pstmt.setString(2, difficulty);
//            pstmt.setInt(3, score);
//            pstmt.setInt(4, creditsEarned);
//            pstmt.setInt(5, mistakes);
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("Save score error: " + e.getMessage());
//        }
//    }
//
//    public void close() {
//        try {
//            if (conn != null && !conn.isClosed()) {
//                conn.close();
//            }
//        } catch (SQLException e) {}
//    }
//}

//package database;
//
//import backend.models.*;
//import java.sql.*;
//import java.util.*;
//
//public class Database {
//    private static final String DB_URL = "jdbc:sqlite:quackmate.db";
//    private Connection conn;
//
//    public Database() {
//        try {
//            Class.forName("org.sqlite.JDBC");
//            initialize();
//        } catch (ClassNotFoundException e) {
//            System.err.println("SQLite JDBC driver not found!");
//            e.printStackTrace();
//        }
//    }
//
//    private void initialize() {
//        try {
//            conn = DriverManager.getConnection(DB_URL);
//            createTables();
//            insertDefaultItems();
//            System.out.println("✅ Database initialized successfully!");
//        } catch (SQLException e) {
//            System.err.println("❌ Failed to initialize database: " + e.getMessage());
//        }
//    }
//
//    private void createTables() throws SQLException {
//        Statement stmt = conn.createStatement();
//
//        // Enable foreign keys
//        stmt.execute("PRAGMA foreign_keys = ON");
//
//        // Users table
//        stmt.execute("""
//            CREATE TABLE IF NOT EXISTS users (
//                id INTEGER PRIMARY KEY AUTOINCREMENT,
//                username TEXT UNIQUE NOT NULL,
//                password_hash TEXT NOT NULL,
//                coins INTEGER DEFAULT 100,
//                level INTEGER DEFAULT 1,
//                experience INTEGER DEFAULT 0,
//                owned_hats TEXT DEFAULT '[]',
//                food_inventory TEXT DEFAULT '{}',
//                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
//                last_login INTEGER DEFAULT 0
//            )
//        """);
//
//        // Ducks table (1 duck per user)
//        stmt.execute("""
//        CREATE TABLE IF NOT EXISTS ducks (
//            id INTEGER PRIMARY KEY AUTOINCREMENT,
//            user_id INTEGER UNIQUE NOT NULL,
//            hunger REAL DEFAULT 100.0,      -- CHANGED: INTEGER → REAL
//            energy REAL DEFAULT 100.0,      -- CHANGED: INTEGER → REAL
//            cleanliness REAL DEFAULT 100.0, -- CHANGED: INTEGER → REAL
//            happiness REAL DEFAULT 100.0,   -- CHANGED: INTEGER → REAL
//            state TEXT DEFAULT 'IDLE',
//            equipped_hat_id INTEGER,
//            last_updated INTEGER DEFAULT 0,
//            last_sleep_check INTEGER DEFAULT 0, -- NEW: Added for auto-sleep
//            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
//    )
//""");
//
//        // Hats table (shop items)
//        stmt.execute("""
//            CREATE TABLE IF NOT EXISTS hats (
//                id INTEGER PRIMARY KEY AUTOINCREMENT,
//                name TEXT UNIQUE NOT NULL,
//                description TEXT,
//                price INTEGER NOT NULL,
//                rarity TEXT DEFAULT 'common'
//            )
//        """);
//
//        // Foods table (shop items)
//        stmt.execute("""
//            CREATE TABLE IF NOT EXISTS foods (
//                id INTEGER PRIMARY KEY AUTOINCREMENT,
//                name TEXT UNIQUE NOT NULL,
//                description TEXT,
//                price INTEGER NOT NULL,
//                hunger_restore INTEGER NOT NULL,
//                energy_restore INTEGER DEFAULT 0,
//                happiness_bonus INTEGER DEFAULT 0
//            )
//        """);
//
//        // Scores table (minigame scores)
//        stmt.execute("""
//            CREATE TABLE IF NOT EXISTS scores (
//                id INTEGER PRIMARY KEY AUTOINCREMENT,
//                user_id INTEGER NOT NULL,
//                game_type TEXT NOT NULL,
//                score INTEGER NOT NULL,
//                coins_earned INTEGER DEFAULT 0,
//                experience_earned INTEGER DEFAULT 0,
//                played_at DATETIME DEFAULT CURRENT_TIMESTAMP,
//                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
//            )
//        """);
//
//        // High scores table
//        stmt.execute("""
//            CREATE TABLE IF NOT EXISTS high_scores (
//                user_id INTEGER NOT NULL,
//                game_type TEXT NOT NULL,
//                highest_score INTEGER DEFAULT 0,
//                play_count INTEGER DEFAULT 0,
//                last_played DATETIME DEFAULT CURRENT_TIMESTAMP,
//                PRIMARY KEY (user_id, game_type),
//                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
//            )
//        """);
//
//        // Food inventory table
//        stmt.execute("""
//            CREATE TABLE IF NOT EXISTS food_inventory (
//                user_id INTEGER NOT NULL,
//                food_id INTEGER NOT NULL,
//                quantity INTEGER DEFAULT 0,
//                PRIMARY KEY (user_id, food_id),
//                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
//                FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
//            )
//        """);
//
//        // Create indexes
//        stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");
//        stmt.execute("CREATE INDEX IF NOT EXISTS idx_ducks_user_id ON ducks(user_id)");
//        stmt.execute("CREATE INDEX IF NOT EXISTS idx_scores_user_game ON scores(user_id, game_type)");
//        stmt.execute("CREATE INDEX IF NOT EXISTS idx_scores_game_score ON scores(game_type, score DESC)");
//        stmt.execute("CREATE INDEX IF NOT EXISTS idx_high_scores_game ON high_scores(game_type, highest_score DESC)");
//
//        stmt.close();
//    }
//
//    private void insertDefaultItems() throws SQLException {
//        Statement stmt = conn.createStatement();
//
//        // Default hats
//        String[] hats = {
//                "INSERT OR IGNORE INTO hats (id, name, description, price, rarity) VALUES " +
//                        "(1, 'Baseball Cap', 'A cool baseball cap', 50, 'common')",
//
//                "INSERT OR IGNORE INTO hats (id, name, description, price, rarity) VALUES " +
//                        "(2, 'Top Hat', 'A fancy top hat', 200, 'rare')",
//
//                "INSERT OR IGNORE INTO hats (id, name, description, price, rarity) VALUES " +
//                        "(3, 'Party Hat', 'Perfect for celebrations', 100, 'uncommon')",
//
//                "INSERT OR IGNORE INTO hats (id, name, description, price, rarity) VALUES " +
//                        "(4, 'Crown', 'Fit for duck royalty', 500, 'legendary')"
//        };
//
//        // Default foods
//        String[] foods = {
//                "INSERT OR IGNORE INTO foods (id, name, description, price, hunger_restore, energy_restore, happiness_bonus) VALUES " +
//                        "(1, 'Bread', 'Basic duck food', 10, 20, 5, 2)",
//
//                "INSERT OR IGNORE INTO foods (id, name, description, price, hunger_restore, energy_restore, happiness_bonus) VALUES " +
//                        "(2, 'Fish', 'A delicious fish meal', 30, 50, 20, 5)",
//
//                "INSERT OR IGNORE INTO foods (id, name, description, price, hunger_restore, energy_restore, happiness_bonus) VALUES " +
//                        "(3, 'Super Seeds', 'Nutritious special seeds', 100, 100, 50, 10)",
//
//                "INSERT OR IGNORE INTO foods (id, name, description, price, hunger_restore, energy_restore, happiness_bonus) VALUES " +
//                        "(4, 'Energy Drink', 'Gives your duck a boost', 150, 10, 80, -5)"
//        };
//
//        for (String sql : hats) stmt.execute(sql);
//        for (String sql : foods) stmt.execute(sql);
//
//        stmt.close();
//    }
//
//    // === USER OPERATIONS ===
//    public int createUser(String username, String passwordHash) {
//        String sql = "INSERT INTO users (username, password_hash, last_login) VALUES (?, ?, ?)";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            long currentTime = System.currentTimeMillis();
//            pstmt.setString(1, username);
//            pstmt.setString(2, passwordHash);
//            pstmt.setLong(3, currentTime);
//            pstmt.executeUpdate();
//
//            ResultSet rs = pstmt.getGeneratedKeys();
//            if (rs.next()) {
//                int userId = rs.getInt(1);
//                createDuck(userId);
//                initializeFoodInventory(userId);
//                return userId;
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error creating user: " + e.getMessage());
//        }
//        return -1;
//    }
//
//    public User getUser(String username, String passwordHash) {
//        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            pstmt.setString(2, passwordHash);
//
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                User user = new User();
//                user.setId(rs.getInt("id"));
//                user.setUsername(rs.getString("username"));
//                user.setPasswordHash(rs.getString("password_hash"));
//                user.setCoins(rs.getInt("coins"));
//                user.setLevel(rs.getInt("level"));
//                user.setExperience(rs.getInt("experience"));
//                user.setLastLoginTime(rs.getLong("last_login"));
//
//                String hatsJson = rs.getString("owned_hats");
//                if (hatsJson != null && !hatsJson.equals("[]")) {
//                    String[] hatIds = hatsJson.replace("[", "").replace("]", "").split(",");
//                    List<Integer> ownedHats = new ArrayList<>();
//                    for (String id : hatIds) {
//                        if (!id.trim().isEmpty()) {
//                            ownedHats.add(Integer.parseInt(id.trim()));
//                        }
//                    }
//                    user.setOwnedHatIds(ownedHats);
//                }
//
//                user.setFoodInventory(rs.getString("food_inventory"));
//                updateLastLogin(user.getId());
//                return user;
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting user: " + e.getMessage());
//        }
//        return null;
//    }
//
//    public boolean userExists(String username) {
//        String sql = "SELECT id FROM users WHERE username = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, username);
//            ResultSet rs = pstmt.executeQuery();
//            return rs.next();
//        } catch (SQLException e) {
//            System.err.println("❌ Error checking user: " + e.getMessage());
//        }
//        return false;
//    }
//
//    public void updateUser(User user) {
//        String sql = """
//            UPDATE users
//            SET coins = ?, level = ?, experience = ?, owned_hats = ?,
//                food_inventory = ?, last_login = ?
//            WHERE id = ?
//        """;
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, user.getCoins());
//            pstmt.setInt(2, user.getLevel());
//            pstmt.setInt(3, user.getExperience());
//            pstmt.setString(4, user.getOwnedHatIds().toString());
//            pstmt.setString(5, user.getFoodInventory());
//            pstmt.setLong(6, user.getLastLoginTime());
//            pstmt.setInt(7, user.getId());
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("❌ Error updating user: " + e.getMessage());
//        }
//    }
//
//    public void updateLastLogin(int userId) {
//        String sql = "UPDATE users SET last_login = ? WHERE id = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setLong(1, System.currentTimeMillis());
//            pstmt.setInt(2, userId);
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("❌ Error updating last login: " + e.getMessage());
//        }
//    }
//
//    // === DUCK OPERATIONS ===
//    public void createDuck(int userId) {
//        // NEW: Include last_sleep_check field
//        String sql = "INSERT INTO ducks (user_id, last_updated, last_sleep_check) VALUES (?, ?, ?)";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            long currentTime = System.currentTimeMillis();
//            pstmt.setInt(1, userId);
//            pstmt.setLong(2, currentTime);
//            pstmt.setLong(3, currentTime); // NEW: Set last_sleep_check too
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("❌ Error creating duck: " + e.getMessage());
//        }
//    }
//
//    public Duck getDuck(int userId) {
//        String sql = "SELECT * FROM ducks WHERE user_id = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, userId);
//
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                Duck duck = new Duck();
//                duck.setId(rs.getInt("id"));
//                duck.setUserId(rs.getInt("user_id"));
//
//                // CHANGED: Use getDouble() instead of getInt()
//                duck.setHunger(rs.getDouble("hunger"));
//                duck.setEnergy(rs.getDouble("energy"));
//                duck.setCleanliness(rs.getDouble("cleanliness"));
//                duck.setHappiness(rs.getDouble("happiness"));
//
//                duck.setState(rs.getString("state"));
//                duck.setLastUpdatedTime(rs.getLong("last_updated"));
//
//                // NEW: Get last_sleep_check
//                duck.setLastSleepCheck(rs.getLong("last_sleep_check"));
//
//                int hatId = rs.getInt("equipped_hat_id");
//                if (!rs.wasNull()) {
//                    duck.setEquippedHatId(hatId);
//                }
//
//                return duck;
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting duck: " + e.getMessage());
//        }
//        return null;
//    }
//
//    public void updateDuck(Duck duck) {
//        String sql = """
//        UPDATE ducks
//        SET hunger = ?, energy = ?, cleanliness = ?, happiness = ?,
//            state = ?, equipped_hat_id = ?, last_updated = ?, last_sleep_check = ?
//        WHERE user_id = ?
//    """;
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            // CHANGED: Use setDouble() instead of setInt()
//            pstmt.setDouble(1, duck.getHunger());
//            pstmt.setDouble(2, duck.getEnergy());
//            pstmt.setDouble(3, duck.getCleanliness());
//            pstmt.setDouble(4, duck.getHappiness());
//            pstmt.setString(5, duck.getState());
//
//            if (duck.getEquippedHatId() != null) {
//                pstmt.setInt(6, duck.getEquippedHatId());
//            } else {
//                pstmt.setNull(6, Types.INTEGER);
//            }
//
//            pstmt.setLong(7, System.currentTimeMillis());
//            pstmt.setLong(8, duck.getLastSleepCheck()); // NEW: Add last_sleep_check
//            pstmt.setInt(9, duck.getUserId());
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("❌ Error updating duck: " + e.getMessage());
//        }
//    }
//
//    public long getDuckLastUpdated(int userId) {
//        String sql = "SELECT last_updated FROM ducks WHERE user_id = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, userId);
//
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                return rs.getLong("last_updated");
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting duck last updated: " + e.getMessage());
//        }
//        return 0;
//    }
//
//    // === SHOP OPERATIONS ===
//    public List<Hat> getAllHats() {
//        List<Hat> hats = new ArrayList<>();
//        String sql = "SELECT * FROM hats ORDER BY price";
//
//        try (Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(sql)) {
//
//            while (rs.next()) {
//                Hat hat = new Hat(
//                        rs.getInt("id"),
//                        rs.getString("name"),
//                        rs.getString("description"),
//                        rs.getInt("price"),
//                        rs.getString("rarity")
//                );
//                hats.add(hat);
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting hats: " + e.getMessage());
//        }
//        return hats;
//    }
//
//    public List<Food> getAllFoods() {
//        List<Food> foods = new ArrayList<>();
//        String sql = "SELECT * FROM foods ORDER BY price";
//
//        try (Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(sql)) {
//
//            while (rs.next()) {
//                Food food = new Food(
//                        rs.getInt("id"),
//                        rs.getString("name"),
//                        rs.getString("description"),
//                        rs.getInt("price"),
//                        rs.getInt("hunger_restore"),
//                        rs.getInt("energy_restore"),
//                        rs.getInt("happiness_bonus")
//                );
//                foods.add(food);
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting foods: " + e.getMessage());
//        }
//        return foods;
//    }
//
//    public Hat getHat(int hatId) {
//        String sql = "SELECT * FROM hats WHERE id = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, hatId);
//
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                return new Hat(
//                        rs.getInt("id"),
//                        rs.getString("name"),
//                        rs.getString("description"),
//                        rs.getInt("price"),
//                        rs.getString("rarity")
//                );
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting hat: " + e.getMessage());
//        }
//        return null;
//    }
//
//    public Food getFood(int foodId) {
//        String sql = "SELECT * FROM foods WHERE id = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, foodId);
//
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                return new Food(
//                        rs.getInt("id"),
//                        rs.getString("name"),
//                        rs.getString("description"),
//                        rs.getInt("price"),
//                        rs.getInt("hunger_restore"),
//                        rs.getInt("energy_restore"),
//                        rs.getInt("happiness_bonus")
//                );
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting food: " + e.getMessage());
//        }
//        return null;
//    }
//
//    // === INVENTORY OPERATIONS ===
//    private void initializeFoodInventory(int userId) {
//        String sql = "INSERT OR IGNORE INTO food_inventory (user_id, food_id, quantity) VALUES (?, 1, 5)";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, userId);
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.err.println("❌ Error initializing food inventory: " + e.getMessage());
//        }
//    }
//
//    public int getFoodQuantity(int userId, int foodId) {
//        String sql = "SELECT quantity FROM food_inventory WHERE user_id = ? AND food_id = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, userId);
//            pstmt.setInt(2, foodId);
//
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                return rs.getInt("quantity");
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting food quantity: " + e.getMessage());
//        }
//        return 0;
//    }
//
//    public boolean addFoodToInventory(int userId, int foodId, int quantity) {
//        String sql = """
//            INSERT INTO food_inventory (user_id, food_id, quantity)
//            VALUES (?, ?, ?)
//            ON CONFLICT(user_id, food_id)
//            DO UPDATE SET quantity = quantity + excluded.quantity
//        """;
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, userId);
//            pstmt.setInt(2, foodId);
//            pstmt.setInt(3, quantity);
//            pstmt.executeUpdate();
//            return true;
//        } catch (SQLException e) {
//            System.err.println("❌ Error adding food to inventory: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public boolean useFoodFromInventory(int userId, int foodId, int quantity) {
//        int currentQty = getFoodQuantity(userId, foodId);
//        if (currentQty < quantity) return false;
//
//        String sql = "UPDATE food_inventory SET quantity = quantity - ? WHERE user_id = ? AND food_id = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, quantity);
//            pstmt.setInt(2, userId);
//            pstmt.setInt(3, foodId);
//            pstmt.executeUpdate();
//
//            String cleanupSql = "DELETE FROM food_inventory WHERE user_id = ? AND food_id = ? AND quantity <= 0";
//            try (PreparedStatement cleanupStmt = conn.prepareStatement(cleanupSql)) {
//                cleanupStmt.setInt(1, userId);
//                cleanupStmt.setInt(2, foodId);
//                cleanupStmt.executeUpdate();
//            }
//            return true;
//        } catch (SQLException e) {
//            System.err.println("❌ Error using food from inventory: " + e.getMessage());
//            return false;
//        }
//    }
//
//    // === SCORE OPERATIONS ===
//    public void saveScore(int userId, String gameType, int score, int coinsEarned, int experienceEarned) {
//        try {
//            conn.setAutoCommit(false);
//
//            String sql = """
//                INSERT INTO scores (user_id, game_type, score, coins_earned, experience_earned)
//                VALUES (?, ?, ?, ?, ?)
//            """;
//
//            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//                pstmt.setInt(1, userId);
//                pstmt.setString(2, gameType);
//                pstmt.setInt(3, score);
//                pstmt.setInt(4, coinsEarned);
//                pstmt.setInt(5, experienceEarned);
//                pstmt.executeUpdate();
//            }
//
//            updateHighScore(userId, gameType, score);
//            conn.commit();
//
//        } catch (SQLException e) {
//            try {
//                conn.rollback();
//                System.err.println("❌ Error saving score, transaction rolled back: " + e.getMessage());
//            } catch (SQLException rollbackEx) {
//                System.err.println("❌ Rollback failed: " + rollbackEx.getMessage());
//            }
//        } finally {
//            try {
//                conn.setAutoCommit(true);
//            } catch (SQLException e) {
//                System.err.println("❌ Error resetting auto-commit: " + e.getMessage());
//            }
//        }
//    }
//
//    private void updateHighScore(int userId, String gameType, int score) {
//        String checkSql = "SELECT highest_score FROM high_scores WHERE user_id = ? AND game_type = ?";
//        String updateSql;
//
//        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
//            checkStmt.setInt(1, userId);
//            checkStmt.setString(2, gameType);
//
//            ResultSet rs = checkStmt.executeQuery();
//
//            if (rs.next()) {
//                int currentHighScore = rs.getInt("highest_score");
//
//                if (score > currentHighScore) {
//                    updateSql = """
//                        UPDATE high_scores
//                        SET highest_score = ?, play_count = play_count + 1, last_played = CURRENT_TIMESTAMP
//                        WHERE user_id = ? AND game_type = ?
//                    """;
//
//                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
//                        updateStmt.setInt(1, score);
//                        updateStmt.setInt(2, userId);
//                        updateStmt.setString(3, gameType);
//                        updateStmt.executeUpdate();
//                    }
//                } else {
//                    updateSql = "UPDATE high_scores SET play_count = play_count + 1 WHERE user_id = ? AND game_type = ?";
//
//                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
//                        updateStmt.setInt(1, userId);
//                        updateStmt.setString(2, gameType);
//                        updateStmt.executeUpdate();
//                    }
//                }
//            } else {
//                updateSql = """
//                    INSERT INTO high_scores (user_id, game_type, highest_score, play_count)
//                    VALUES (?, ?, ?, 1)
//                """;
//
//                try (PreparedStatement insertStmt = conn.prepareStatement(updateSql)) {
//                    insertStmt.setInt(1, userId);
//                    insertStmt.setString(2, gameType);
//                    insertStmt.setInt(3, score);
//                    insertStmt.executeUpdate();
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error updating high score: " + e.getMessage());
//        }
//    }
//
//    public List<Score> getHighScores(String gameType, int limit) {
//        List<Score> scores = new ArrayList<>();
//        String sql = """
//            SELECT hs.*, u.username
//            FROM high_scores hs
//            JOIN users u ON hs.user_id = u.id
//            WHERE hs.game_type = ?
//            ORDER BY hs.highest_score DESC
//            LIMIT ?
//        """;
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, gameType);
//            pstmt.setInt(2, limit);
//
//            ResultSet rs = pstmt.executeQuery();
//            int rank = 1;
//            while (rs.next()) {
//                Score score = new Score();
//                score.setId(rank);
//                score.setUserId(rs.getInt("user_id"));
//                score.setUsername(rs.getString("username"));
//                score.setGameType(rs.getString("game_type"));
//                score.setScore(rs.getInt("highest_score"));
//                score.setPlayedAt(rs.getTimestamp("last_played").toLocalDateTime());
//                scores.add(score);
//                rank++;
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting high scores: " + e.getMessage());
//        }
//        return scores;
//    }
//
//    public int getUserHighScore(int userId, String gameType) {
//        String sql = "SELECT highest_score FROM high_scores WHERE user_id = ? AND game_type = ?";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, userId);
//            pstmt.setString(2, gameType);
//
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                return rs.getInt("highest_score");
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting user high score: " + e.getMessage());
//        }
//        return 0;
//    }
//
//    public int getUserRank(int userId, String gameType) {
//        String sql = """
//            SELECT rank FROM (
//                SELECT user_id, ROW_NUMBER() OVER (ORDER BY highest_score DESC) as rank
//                FROM high_scores
//                WHERE game_type = ?
//            ) ranked
//            WHERE user_id = ?
//        """;
//
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, gameType);
//            pstmt.setInt(2, userId);
//
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                return rs.getInt("rank");
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error getting user rank: " + e.getMessage());
//        }
//        return -1;
//    }
//
//    // === UTILITY METHODS ===
//    public void testConnection() {
//        try {
//            if (conn != null && !conn.isClosed()) {
//                try (Statement stmt = conn.createStatement()) {
//                    stmt.execute("SELECT 1");
//                    System.out.println("✅ Database connection test: SUCCESS");
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Database connection test: FAILED - " + e.getMessage());
//        }
//    }
//
//    public void close() {
//        try {
//            if (conn != null && !conn.isClosed()) {
//                conn.close();
//                System.out.println("✅ Database connection closed.");
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Error closing database: " + e.getMessage());
//        }
//    }
//
//    public static class DatabaseManager {
//    }
//}