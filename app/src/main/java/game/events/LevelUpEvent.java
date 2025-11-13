package game.events;

import java.nio.ByteBuffer;

/**
 * 레벨이 상승했을 때 발생하는 이벤트
 */
public class LevelUpEvent extends GameEvent {
    private int newLevel;
    private int playerId;
    
    public LevelUpEvent(int newLevel, int playerId) {
        super("LEVEL_UP");
        this.newLevel = newLevel;
        this.playerId = playerId;
    }
    
    // 기본 생성자 (역직렬화용)
    public LevelUpEvent() {
        super("LEVEL_UP");
    }
    
    // Getters
    public int getNewLevel() { return newLevel; }
    public int getPlayerId() { return playerId; }
    
    @Override
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(getTimestamp());
        buffer.putInt(newLevel);
        buffer.putInt(playerId);
        return buffer.array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp skip
        this.newLevel = buffer.getInt();
        this.playerId = buffer.getInt();
    }
    
    @Override
    public String toString() {
        return "LevelUpEvent{" +
                "newLevel=" + newLevel +
                ", playerId=" + playerId +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}