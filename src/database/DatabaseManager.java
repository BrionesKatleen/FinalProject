package database;

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
        }
        return conn;
    }

    //-----------------------------------------
    // CREATE TABLES
    //-----------------------------------------
    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            Statement stmt = getConnection().createStatement();

            // Player
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Player (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT
                )
            """);

            // Duck
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Duck (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_id INTEGER,
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
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int id = rs.next() ? rs.getInt(1) : -1;

            rs.close();
            ps.close();
            return id;

        } catch (SQLException e) {
            e.printStackTrace();
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
    // AUTO STAT DECAY EVERY MINUTE
    //-----------------------------------------
    public static void startStatTimer(int duckId) {
        Timer timer = new Timer(true);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Duck d = getDuck(duckId);
                if (d == null) return;

                double hunger = d.hunger;
                double energy = d.energy;
                double cleanliness = d.cleanliness;
                double happiness = d.happiness;

                //---------------------------------
                // APPLY EFFECTS BASED ON STATE
                //---------------------------------

                switch (d.state) {

                    case "playing":
                        energy += +0.4;
                        hunger += -0.03;
                        cleanliness += -0.025;
                        happiness += +0.3;
                        break;

                    case "eating":
                        energy += +0.2;
                        hunger += +0.5;
                        happiness += +0.2;
                        break;

                    case "sleeping":
                        energy += +0.09;
                        break;

                    case "bathing":
                        cleanliness += +0.6;
                        happiness += +0.2;
                        break;

                    default: // idle
                        energy += -0.025;
                        hunger += -0.02;
                        cleanliness += -0.02;
                        happiness += -0.02;
                        break;
                }

                //---------------------------------
                // NIGHT TIME ENERGY DRAIN
                //---------------------------------
                boolean isNight = java.time.LocalTime.now().getHour() >= 18 ||
                        java.time.LocalTime.now().getHour() < 6;

                if (isNight) {
                    energy += -0.1;
                }

                //---------------------------------
                // SAVE UPDATED STATS
                //---------------------------------
                updateStats(duckId, hunger, energy, cleanliness, happiness);
            }
        }, 0, 60_000); // run every 1 minute
    }

    //-----------------------------------------
    // DUCK CLASS (DATA HOLDER)
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
