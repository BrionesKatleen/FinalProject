package database;

import backend.models.Duck;
import backend.models.User;
import backend.models.Hat;
import backend.models.Food;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:quackmate.db";
    private Connection conn;

    public Database() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Database connected!");
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String users = """
            CREATE TABLE IF NOT EXISTS Players (
                player_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT,
                coins INTEGER DEFAULT 100,
                level INTEGER DEFAULT 1,
                experience INTEGER DEFAULT 0,
                owned_hats TEXT DEFAULT '{}',
                food_inventory TEXT DEFAULT '{}',
                last_login INTEGER
            );
        """;

        String ducks = """
            CREATE TABLE IF NOT EXISTS Ducks (
                duck_id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_id INTEGER UNIQUE,
                name TEXT DEFAULT 'Quacky',
                hunger REAL DEFAULT 100,
                energy REAL DEFAULT 100,
                cleanliness REAL DEFAULT 100,
                happiness REAL DEFAULT 100,
                state TEXT DEFAULT 'IDLE',
                last_updated INTEGER DEFAULT (strftime('%s','now')),
                FOREIGN KEY(player_id) REFERENCES Players(player_id)
            );
        """;

        String hats = """
            CREATE TABLE IF NOT EXISTS Hats (
                hat_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                description TEXT,
                price INTEGER,
                rarity TEXT
            );
        """;

        String foods = """
            CREATE TABLE IF NOT EXISTS Food (
                food_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                description TEXT,
                price INTEGER,
                hunger_restore INTEGER,
                energy_restore INTEGER,
                happiness_bonus INTEGER
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(users);
            stmt.execute(ducks);
            stmt.execute(hats);
            stmt.execute(foods);
        }
        System.out.println("All tables created!");
    }

    // ----------------------
    // USER METHODS
    // ----------------------
    public int createUser(String username, String passwordHash) {
        String sql = "INSERT INTO Players(username, password, last_login) VALUES(?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setLong(3, System.currentTimeMillis());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM Players WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User getUser(String username, String passwordHash) {
        String sql = "SELECT * FROM Players WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("player_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password"));
                    user.setCoins(rs.getInt("coins"));
                    user.setLevel(rs.getInt("level"));
                    user.setExperience(rs.getInt("experience"));
                    user.setOwnedHatIds(stringToList(rs.getString("owned_hats")));
                    user.setFoodInventory(rs.getString("food_inventory"));
                    user.setLastLoginTime(rs.getLong("last_login"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateUser(User user) {
        String sql = "UPDATE Players SET coins = ?, level = ?, experience = ?, owned_hats = ?, food_inventory = ?, last_login = ? WHERE player_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getCoins());
            pstmt.setInt(2, user.getLevel());
            pstmt.setInt(3, user.getExperience());
            pstmt.setString(4, listToString(user.getOwnedHatIds()));
            pstmt.setString(5, user.getFoodInventory());
            pstmt.setLong(6, user.getLastLoginTime());
            pstmt.setInt(7, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------------------
    // DUCK METHODS
    // ----------------------
    public void createDuck(Duck duck) {
        String sql = """
            INSERT INTO Ducks(player_id, name, hunger, energy, cleanliness, happiness, state, last_updated)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, duck.getUserId());
            pstmt.setString(2, "Quacky");
            pstmt.setDouble(3, duck.getHunger());
            pstmt.setDouble(4, duck.getEnergy());
            pstmt.setDouble(5, duck.getCleanliness());
            pstmt.setDouble(6, duck.getHappiness());
            pstmt.setString(7, duck.getState());
            pstmt.setLong(8, System.currentTimeMillis());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) duck.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Duck getDuck(int userId) {
        String sql = "SELECT * FROM Ducks WHERE player_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Duck duck = new Duck(userId);
                    duck.setId(rs.getInt("duck_id"));
                    duck.setHunger(rs.getDouble("hunger"));
                    duck.setEnergy(rs.getDouble("energy"));
                    duck.setCleanliness(rs.getDouble("cleanliness"));
                    duck.setHappiness(rs.getDouble("happiness"));
                    duck.setState(rs.getString("state"));
                    duck.setLastUpdatedTime(rs.getLong("last_updated"));
                    return duck;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateDuck(Duck duck) {
        String sql = """
            UPDATE Ducks
            SET hunger = ?, energy = ?, cleanliness = ?, happiness = ?, state = ?, last_updated = ?
            WHERE duck_id = ?
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, duck.getHunger());
            pstmt.setDouble(2, duck.getEnergy());
            pstmt.setDouble(3, duck.getCleanliness());
            pstmt.setDouble(4, duck.getHappiness());
            pstmt.setString(5, duck.getState());
            pstmt.setLong(6, System.currentTimeMillis());
            pstmt.setInt(7, duck.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------------------
    // HAT METHODS
    // ----------------------
    public void createHat(Hat hat) {
        String sql = "INSERT INTO Hats(name, description, price, rarity) VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, hat.getName());
            pstmt.setString(2, hat.getDescription());
            pstmt.setInt(3, hat.getPrice());
            pstmt.setString(4, hat.getRarity());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) hat.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Hat> getAllHats() {
        List<Hat> hats = new ArrayList<>();
        String sql = "SELECT * FROM Hats";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                hats.add(new Hat(
                        rs.getInt("hat_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("price"),
                        rs.getString("rarity")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hats;
    }

    // ----------------------
    // FOOD METHODS
    // ----------------------
    public void createFood(Food food) {
        String sql = """
            INSERT INTO Food(name, description, price, hunger_restore, energy_restore, happiness_bonus)
            VALUES(?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, food.getName());
            pstmt.setString(2, food.getDescription());
            pstmt.setInt(3, food.getPrice());
            pstmt.setInt(4, food.getHungerRestore());
            pstmt.setInt(5, food.getEnergyRestore());
            pstmt.setInt(6, food.getHappinessBonus());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) food.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Food> getAllFoods() {
        List<Food> foods = new ArrayList<>();
        String sql = "SELECT * FROM Food";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                foods.add(new Food(
                        rs.getInt("food_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("price"),
                        rs.getInt("hunger_restore"),
                        rs.getInt("energy_restore"),
                        rs.getInt("happiness_bonus")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foods;
    }

    // ----------------------
    // FOOD INVENTORY METHODS
    // ----------------------
    public boolean useFoodFromInventory(int userId, int foodId, int quantity) {
        User user = getUserById(userId);
        if (user == null) return false;

        String inv = user.getFoodInventory(); // format: "foodId:qty,foodId:qty"
        String[] items = inv.split(",");
        StringBuilder newInv = new StringBuilder();
        boolean found = false;

        for (String item : items) {
            if (item.isEmpty()) continue;
            String[] parts = item.split(":");
            int id = Integer.parseInt(parts[0]);
            int qty = Integer.parseInt(parts[1]);

            if (id == foodId) {
                if (qty >= quantity) {
                    qty -= quantity;
                    found = true;
                } else return false;
            }

            if (qty > 0) {
                if (newInv.length() > 0) newInv.append(",");
                newInv.append(id).append(":").append(qty);
            }
        }

        if (!found) return false;

        user.setFoodInventory(newInv.toString());
        updateUser(user);
        return true;
    }

    // ----------------------
    // HELPER METHODS
    // ----------------------
    private List<Integer> stringToList(String str) {
        List<Integer> list = new ArrayList<>();
        if (str == null || str.isEmpty() || str.equals("{}")) return list;
        str = str.replace("{", "").replace("}", "");
        for (String s : str.split(",")) {
            list.add(Integer.parseInt(s.trim()));
        }
        return list;
    }

    private String listToString(List<Integer> list) {
        return list.toString(); // produces "{1, 2, 3}" format
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM Players WHERE player_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("player_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password"));
                    user.setCoins(rs.getInt("coins"));
                    user.setLevel(rs.getInt("level"));
                    user.setExperience(rs.getInt("experience"));
                    user.setOwnedHatIds(stringToList(rs.getString("owned_hats")));
                    user.setFoodInventory(rs.getString("food_inventory"));
                    user.setLastLoginTime(rs.getLong("last_login"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Close connection
    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


