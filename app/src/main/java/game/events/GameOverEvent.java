package game.events;

import java.nio.ByteBuffer;

/**
 * 게임이 종료되었을 때 발생하는 이벤트
 */
public class GameOverEvent extends GameEvent {
    private int finalScore;
    private int playerId;
    
    public GameOverEvent(int finalScore, int playerId) {
        super("GAME_OVER");
        this.finalScore = finalScore;
        this.playerId = playerId;
    }
    
    // 기본 생성자 (역직렬화용)
    public GameOverEvent() {
        super("GAME_OVER");
    }
    
    // Getters
    public int getFinalScore() { return finalScore; }
    public int getPlayerId() { return playerId; }
    
    @Override
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(getTimestamp());
        buffer.putInt(finalScore);
        buffer.putInt(playerId);
        return buffer.array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp skip
        this.finalScore = buffer.getInt();
        this.playerId = buffer.getInt();
    }
    
    @Override
    public String toString() {
        return "GameOverEvent{" +
                "finalScore=" + finalScore +
                ", playerId=" + playerId +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}