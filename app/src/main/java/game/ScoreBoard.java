package game;

public class ScoreBoard {
    private int highScore;

    public ScoreBoard() {
        highScore = 0;
    }
    
    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }
    
    public int getHighScore() {
        return highScore;
    }
}