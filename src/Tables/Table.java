//////Players Table
////CREATE TABLE IF NOT EXISTS Players (
////        player_id INTEGER PRIMARY KEY AUTOINCREMENT,
////        username TEXT UNIQUE NOT NULL,
////        created_at TEXT DEFAULT CURRENT_TIMESTAMP
////);
////
//////Duck Table
////CREATE TABLE IF NOT EXISTS Ducks (
////        duck_id INTEGER PRIMARY KEY AUTOINCREMENT,
////        player_id INTEGER NOT NULL,
////        name TEXT,
////        hunger REAL DEFAULT 100,
////        energy REAL DEFAULT 100,
////        cleanliness REAL DEFAULT 100,
////        happiness REAL DEFAULT 100,
////        last_updated TEXT DEFAULT CURRENT_TIMESTAMP,
////
////        FOREIGN KEY (player_id) REFERENCES Players(player_id)
////        );
////
//////Hats Table
////CREATE TABLE IF NOT EXISTS Hats (
////        hat_id INTEGER PRIMARY KEY AUTOINCREMENT,
////        name TEXT NOT NULL,
////        image_path TEXT,
////        price INTEGER
////);
////
//////Player_Hats Table(Inventory)
////CREATE TABLE IF NOT EXISTS Player_Hats (
////        player_id INTEGER,
////        hat_id INTEGER,
////        is_equipped INTEGER DEFAULT 0,
////
////        PRIMARY KEY (player_id, hat_id),
////FOREIGN KEY (player_id) REFERENCES Players(player_id),
////FOREIGN KEY (hat_id) REFERENCES Hats(hat_id)
////        );
////
//////Foods Table
////CREATE TABLE IF NOT EXISTS Foods (
////        food_id INTEGER PRIMARY KEY AUTOINCREMENT,
////        name TEXT NOT NULL,
////        hunger_restore REAL,
////        happiness_restore REAL,
////        price INTEGER
////);
////
//////Player_Foods Table(Inventory)
////CREATE TABLE IF NOT EXISTS Player_Foods (
////        player_id INTEGER,
////        food_id INTEGER,
////        quantity INTEGER DEFAULT 0,
////
////        PRIMARY KEY (player_id, food_id),
////FOREIGN KEY (player_id) REFERENCES Players(player_id),
////FOREIGN KEY (food_id) REFERENCES Foods(food_id)
////        );
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//public class DatabaseManager {
//
//    private Connection conn;
//
//    public DatabaseManager() {
//        connect();
//        createTables();
//    }
//
//    private void connect() {
//        try {
//            conn = DriverManager.getConnection("jdbc:sqlite:quackmate.db");
//            System.out.println("Database connected!");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void createTables() {
//
//        String playersTable = """
//            CREATE TABLE IF NOT EXISTS Players (
//                player_id INTEGER PRIMARY KEY AUTOINCREMENT,
//                username TEXT UNIQUE NOT NULL,
//                created_at TEXT DEFAULT CURRENT_TIMESTAMP
//            );
//        """;
//
//        String ducksTable = """
//            CREATE TABLE IF NOT EXISTS Ducks (
//                duck_id INTEGER PRIMARY KEY AUTOINCREMENT,
//                player_id INTEGER NOT NULL,
//                name TEXT,
//                hunger REAL DEFAULT 100,
//                energy REAL DEFAULT 100,
//                cleanliness REAL DEFAULT 100,
//                happiness REAL DEFAULT 100,
//                last_updated TEXT DEFAULT CURRENT_TIMESTAMP,
//                FOREIGN KEY (player_id) REFERENCES Players(player_id)
//            );
//        """;
//
//        String hatsTable = """
//            CREATE TABLE IF NOT EXISTS Hats (
//                hat_id INTEGER PRIMARY KEY AUTOINCREMENT,
//                name TEXT NOT NULL,
//                image_path TEXT,
//                price INTEGER
//            );
//        """;
//
//        String playerHatsTable = """
//            CREATE TABLE IF NOT EXISTS Player_Hats (
//                player_id INTEGER,
//                hat_id INTEGER,
//                is_equipped INTEGER DEFAULT 0,
//                PRIMARY KEY (player_id, hat_id),
//                FOREIGN KEY (player_id) REFERENCES Players(player_id),
//                FOREIGN KEY (hat_id) REFERENCES Hats(hat_id)
//            );
//        """;
//
//        String foodsTable = """
//            CREATE TABLE IF NOT EXISTS Foods (
//                food_id INTEGER PRIMARY KEY AUTOINCREMENT,
//                name TEXT NOT NULL,
//                hunger_restore REAL,
//                happiness_restore REAL,
//                price INTEGER
//            );
//        """;
//
//        String playerFoodsTable = """
//            CREATE TABLE IF NOT EXISTS Player_Foods (
//                player_id INTEGER,
//                food_id INTEGER,
//                quantity INTEGER DEFAULT 0,
//                PRIMARY KEY (player_id, food_id),
//                FOREIGN KEY (player_id) REFERENCES Players(player_id),
//                FOREIGN KEY (food_id) REFERENCES Foods(food_id)
//            );
//        """;
//
//        try (Statement stmt = conn.createStatement()) {
//            stmt.execute(playersTable);
//            stmt.execute(ducksTable);
//            stmt.execute(hatsTable);
//            stmt.execute(playerHatsTable);
//            stmt.execute(foodsTable);
//            stmt.execute(playerFoodsTable);
//            System.out.println("All tables created!");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//}
