package backend.models;

public class Score {
    private int userId;
    private String username;
    private int score;

    public Score(int userId, String username, int score) {
        this.userId = userId;
        this.username = username;
        this.score = score;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getScore() { return score; }

    @Override
    public String toString() {
        return username + ": " + score;
    }
}
