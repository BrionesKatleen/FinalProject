package database;

import java.io.File;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:quackmate.db";
    private static Connection conn;

    //-----------------------------------------
    // DATABASE CONNECTION
    //-----------------------------------------
    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            // Enable foreign key support
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.close();
        }
        return conn;
    }

    //-----------------------------------------
    // CLOSE DATABASE CONNECTION
    //-----------------------------------------
    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------
    // INITIALIZE DATABASE (DROP OLD TABLES)
    //-----------------------------------------
    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");

            // Delete old database file
            File dbFile = new File("quackmate.db");
            if (dbFile.exists()) {
                closeConnection();
                if (dbFile.delete()) {
                    System.out.println("Old database deleted successfully.");
                } else {
                    System.out.println("Failed to delete old database.");
                }
            }

            Statement stmt = getConnection().createStatement();

            // Create tables
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Player (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Duck (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_id INTEGER NOT NULL,
                    hunger REAL,
                    energy REAL,
                    cleanliness REAL,
                    happiness REAL,
                    state TEXT DEFAULT 'idle',
                    FOREIGN KEY(player_id) REFERENCES Player(id)
                )
            """);

            stmt.close();
            System.out.println("Tables are ready!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------
    // ADD PLAYER
    //-----------------------------------------
    public static int addPlayer(String name) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO Player(name) VALUES(?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int id = rs.next() ? rs.getInt(1) : -1;
            rs.close();
            ps.close();
            System.out.println("Added Player ID: " + id);
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    //-----------------------------------------
    // ADD DUCK
    //-----------------------------------------
    public static int addDuck(int playerId) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO Duck(player_id, hunger, energy, cleanliness, happiness, state) VALUES(?, 100, 100, 100, 100, 'idle')",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, playerId);

            int rows = ps.executeUpdate();
            System.out.println("Rows inserted into Duck: " + rows);

            ResultSet rs = ps.getGeneratedKeys();
            int id = rs.next() ? rs.getInt(1) : -1;
            rs.close();
            ps.close();
            System.out.println("Added Duck ID: " + id);
            return id;
        } catch (SQLException e) {
            System.out.println("Error adding duck: " + e.getMessage());
            return -1;
        }
    }

    //-----------------------------------------
    // CHANGE DUCK STATE
    //-----------------------------------------
    public static void setDuckState(int duckId, String state) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "UPDATE Duck SET state=? WHERE id=?"
            );
            ps.setString(1, state);
            ps.setInt(2, duckId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------
    // GET DUCK
    //-----------------------------------------
    public static Duck getDuck(int duckId) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT * FROM Duck WHERE id=?"
            );
            ps.setInt(1, duckId);
            ResultSet rs = ps.executeQuery();

            Duck duck = null;
            if (rs.next()) {
                duck = new Duck(
                        rs.getInt("id"),
                        rs.getDouble("hunger"),
                        rs.getDouble("energy"),
                        rs.getDouble("cleanliness"),
                        rs.getDouble("happiness"),
                        rs.getString("state")
                );
            }
            rs.close();
            ps.close();
            return duck;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //-----------------------------------------
    // UPDATE STATS
    //-----------------------------------------
    public static void updateStats(int duckId, double hunger, double energy, double cleanliness, double happiness) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "UPDATE Duck SET hunger=?, energy=?, cleanliness=?, happiness=? WHERE id=?"
            );
            ps.setDouble(1, clamp(hunger));
            ps.setDouble(2, clamp(energy));
            ps.setDouble(3, clamp(cleanliness));
            ps.setDouble(4, clamp(happiness));
            ps.setInt(5, duckId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------
    // CLAMP VALUES 0–100
    //-----------------------------------------
    private static double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }

    //-----------------------------------------
    // AUTO STAT UPDATE EVERY 5 SECONDS
    //-----------------------------------------
    public static void startStatTimer(int duckId) {
        Timer timer = new Timer(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Duck d = getDuck(duckId);
                if (d == null) return;

                double hunger = d.hunger;
                double energy = d.energy;
                double cleanliness = d.cleanliness;
                double happiness = d.happiness;

                switch (d.state) {
                    case "playing":
                        energy += 0.4;
                        hunger -= 0.03;
                        cleanliness -= 0.025;
                        happiness += 0.3;
                        break;
                    case "eating":
                        energy += 0.2;
                        hunger += 0.5;
                        happiness += 0.2;
                        break;
                    case "sleeping":
                        energy += 0.09;
                        break;
                    case "bathing":
                        cleanliness += 0.6;
                        happiness += 0.2;
                        break;
                    default:
                        energy -= 0.025;
                        hunger -= 0.02;
                        cleanliness -= 0.02;
                        happiness -= 0.02;
                        break;
                }

                boolean isNight = java.time.LocalTime.now().getHour() >= 18 ||
                        java.time.LocalTime.now().getHour() < 6;
                if (isNight) energy -= 0.1;

                updateStats(duckId, hunger, energy, cleanliness, happiness);

                // PRINT STATS
                System.out.println("=== DUCK STATS ===");
                System.out.printf("Hunger: %.2f\n", hunger);
                System.out.printf("Energy: %.2f\n", energy);
                System.out.printf("Cleanliness: %.2f\n", cleanliness);
                System.out.printf("Happiness: %.2f\n", happiness);
                System.out.println("State: " + d.state);
                System.out.println("=================\n");
            }
        };

        task.run(); // first immediate print
        timer.scheduleAtFixedRate(task, 5_000, 5_000); // every 5 seconds
    }

    //-----------------------------------------
    // DUCK CLASS
    //-----------------------------------------
    public static class Duck {
        public int id;
        public double hunger, energy, cleanliness, happiness;
        public String state;

        public Duck(int id, double hunger, double energy, double cleanliness, double happiness, String state) {
            this.id = id;
            this.hunger = hunger;
            this.energy = energy;
            this.cleanliness = cleanliness;
            this.happiness = happiness;
            this.state = state;
        }
    }
}
