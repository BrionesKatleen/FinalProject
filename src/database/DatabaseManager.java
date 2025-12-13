package database;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:quackmate.db";
    private Connection conn;

    // ================== CONSTRUCTOR ===================
    public DatabaseManager() {
        connect();
        createTables();
        insertDefaultFoods();
        insertDefaultHats();
    }

    // ================== CONNECT ==================
    private void connect() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            System.out.println("Database connected!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================== CREATE TABLES ==================
    private void createTables() {

        String players = """
            CREATE TABLE IF NOT EXISTS Players (
                player_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                credits INTEGER DEFAULT 100,
                experience INTEGER DEFAULT 0,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String ducks = """
            CREATE TABLE IF NOT EXISTS Ducks (
                duck_id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_id INTEGER,
                name TEXT,
                hunger REAL DEFAULT 100,
                energy REAL DEFAULT 100,
                cleanliness REAL DEFAULT 100,
                happiness REAL DEFAULT 100,
                last_updated TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (player_id) REFERENCES Players(player_id)
            );
        """;

        String hats = """
            CREATE TABLE IF NOT EXISTS Hats (
                hat_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                level_required INTEGER,
                price INTEGER,
                stat_multiplier REAL,
                experience_bonus INTEGER
            );
        """;

        String playerHats = """
            CREATE TABLE IF NOT EXISTS Player_Hats (
                player_id INTEGER,
                hat_id INTEGER,
                is_equipped INTEGER DEFAULT 0,
                PRIMARY KEY (player_id, hat_id),
                FOREIGN KEY (player_id) REFERENCES Players(player_id),
                FOREIGN KEY (hat_id) REFERENCES Hats(hat_id)
            );
        """;

        String foods = """
            CREATE TABLE IF NOT EXISTS Foods (
                food_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                price INTEGER,
                hunger REAL,
                energy REAL,
                happiness REAL,
                cleanliness REAL
            );
        """;

        String playerFoods = """
            CREATE TABLE IF NOT EXISTS Player_Foods (
                player_id INTEGER,
                food_id INTEGER,
                quantity INTEGER DEFAULT 0,
                PRIMARY KEY (player_id, food_id),
                FOREIGN KEY (player_id) REFERENCES Players(player_id),
                FOREIGN KEY (food_id) REFERENCES Foods(food_id)
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(players);
            stmt.execute(ducks);
            stmt.execute(hats);
            stmt.execute(playerHats);
            stmt.execute(foods);
            stmt.execute(playerFoods);
            System.out.println("All tables created!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================== DEFAULT FOODS ==================
    private void insertDefaultFoods() {
        String sql = """
            INSERT OR IGNORE INTO Foods
            (name, price, hunger, energy, happiness, cleanliness)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Peas & Bird Seeds (5 credits)
            insertFood(stmt, "Peas", 5, 0.5, 0.5, 0.05, -0.05);
            insertFood(stmt, "Bird Seeds", 5, 0.5, 0.5, 0.05, -0.05);

            // Oats & Corn (10 credits)
            insertFood(stmt, "Oats", 10, 0.8, 0.8, 0.08, -0.05);
            insertFood(stmt, "Corn", 10, 0.8, 0.8, 0.08, -0.05);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertFood(PreparedStatement stmt, String name, int price,
                            double hunger, double energy, double happiness, double cleanliness)
            throws SQLException {

        stmt.setString(1, name);
        stmt.setInt(2, price);
        stmt.setDouble(3, hunger);
        stmt.setDouble(4, energy);
        stmt.setDouble(5, happiness);
        stmt.setDouble(6, cleanliness);
        stmt.executeUpdate();
    }

    // ================== DEFAULT HATS ==================
    private void insertDefaultHats() {
        String sql = """
            INSERT OR IGNORE INTO Hats
            (name, level_required, price, stat_multiplier, experience_bonus)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            insertHat(stmt, "Frog", 5, 300, 0.02, 50);
            insertHat(stmt, "Cat", 10, 500, 0.03, 100);
            insertHat(stmt, "Stitches", 20, 1000, 0.05, 200);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertHat(PreparedStatement stmt, String name, int level,
                           int price, double multiplier, int exp)
            throws SQLException {

        stmt.setString(1, name);
        stmt.setInt(2, level);
        stmt.setInt(3, price);
        stmt.setDouble(4, multiplier);
        stmt.setInt(5, exp);
        stmt.executeUpdate();
    }

    // ================== PLAYER CRUD ==================
    public void createPlayer(String username) {
        String sql = "INSERT INTO Players(username) VALUES(?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerId(String username) {
        String sql = "SELECT player_id FROM Players WHERE username=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("player_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ================== DUCK CRUD ==================
    public void createDuck(int playerId, String name) {
        String sql = """
            INSERT INTO Ducks(player_id, name)
            VALUES (?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateDuckStats(int duckId, double hunger, double energy,
                                double cleanliness, double happiness) {

        String sql = """
            UPDATE Ducks
            SET hunger=?, energy=?, cleanliness=?, happiness=?, last_updated=CURRENT_TIMESTAMP
            WHERE duck_id=?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, hunger);
            stmt.setDouble(2, energy);
            stmt.setDouble(3, cleanliness);
            stmt.setDouble(4, happiness);
            stmt.setInt(5, duckId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================== FOOD INVENTORY ==================
    public void addFoodToPlayer(int playerId, int foodId, int qty) {
        String sql = """
            INSERT INTO Player_Foods(player_id, food_id, quantity)
            VALUES (?, ?, ?)
            ON CONFLICT(player_id, food_id)
            DO UPDATE SET quantity = quantity + ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, foodId);
            stmt.setInt(3, qty);
            stmt.setInt(4, qty);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================== HAT INVENTORY ==================
    public void giveHatToPlayer(int playerId, int hatId) {
        String sql = "INSERT INTO Player_Hats(player_id, hat_id) VALUES(?,?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, hatId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void equipHat(int playerId, int hatId) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE Player_Hats SET is_equipped=0 WHERE player_id=" + playerId);

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Player_Hats SET is_equipped=1 WHERE player_id=? AND hat_id=?"
            );
            ps.setInt(1, playerId);
            ps.setInt(2, hatId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
