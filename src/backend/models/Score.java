package backend.models;

import java.time.LocalDateTime;

public class Score {
    private int id;
    private int userId;
    private String username;
    private String gameType;
    private int score;
    private int coinsEarned;
    private int experienceEarned;
    private LocalDateTime playedAt;

    public Score() {}

    public Score(int userId, String gameType, int score) {
        this.userId = userId;
        this.gameType = gameType;
        this.score = score;
        this.playedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getCoinsEarned() { return coinsEarned; }
    public void setCoinsEarned(int coinsEarned) { this.coinsEarned = coinsEarned; }

    public int getExperienceEarned() { return experienceEarned; }
    public void setExperienceEarned(int experienceEarned) { this.experienceEarned = experienceEarned; }

    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }

    @Override
    public String toString() {
        return username + ": " + score + " points (" + playedAt.toLocalDate() + ")";
    }
}
