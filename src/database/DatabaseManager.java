//Create a new player
public int createPlayer(String username, String password) {
    String sql = "INSERT INTO players(username, password) VALUES(?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, username);
        stmt.setString(2, password);
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) return rs.getInt(1);

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}

//Get player by Username
public ResultSet getPlayer(String username) {
    String sql = "SELECT * FROM players WHERE username = ?";
    try {
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        return stmt.executeQuery();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

//Create a duck for a player
public int createDuck(int playerId) {
    String sql = "INSERT INTO ducks(player_id, hunger, cleanliness, happiness) VALUES(?, 100, 100, 100)";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, playerId);
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) return rs.getInt(1);

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}

//Get duck by player ID
public ResultSet getDuckByPlayer(int playerId) {
    String sql = "SELECT * FROM ducks WHERE player_id = ?";
    try {
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, playerId);
        return stmt.executeQuery();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

//Update Duck Stats
public boolean updateDuckStats(int duckId, double hunger, double cleanliness, double happiness) {
    String sql = "UPDATE ducks SET hunger = ?, cleanliness = ?, happiness = ? WHERE duck_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setDouble(1, hunger);
        stmt.setDouble(2, cleanliness);
        stmt.setDouble(3, happiness);
        stmt.setInt(4, duckId);

        return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}
