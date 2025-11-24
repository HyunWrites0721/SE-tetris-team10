package game.events;

import java.nio.ByteBuffer;

/**
 * 게임 틱 이벤트
 * 게임 루프의 각 틱마다 발생하는 이벤트
 */
public class TickEvent extends GameEvent {
    private int currentLevel;
    private int speedLevel;
    private long deltaTime; // 이전 틱과의 시간 차이 (밀리초)
    
    public TickEvent(int currentLevel, int speedLevel, long deltaTime) {
        super("TICK");
        this.currentLevel = currentLevel;
        this.speedLevel = speedLevel;
        this.deltaTime = deltaTime;
    }
    
    // 기본 생성자 (역직렬화용)
    public TickEvent() {
        super("TICK");
        this.currentLevel = 1;
        this.speedLevel = 1;
        this.deltaTime = 0;
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public int getSpeedLevel() {
        return speedLevel;
    }
    
    public long getDeltaTime() {
        return deltaTime;
    }
    
    @Override
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(24); // 8 + 4 + 4 + 8
        buffer.putLong(getTimestamp());
        buffer.putInt(currentLevel);
        buffer.putInt(speedLevel);
        buffer.putLong(deltaTime);
        return buffer.array();
    }
    
    @Override
    public void deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.getLong(); // timestamp skip
        this.currentLevel = buffer.getInt();
        this.speedLevel = buffer.getInt();
        this.deltaTime = buffer.getLong();
    }
    
    public static TickEvent fromBytes(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        long timestamp = buffer.getLong();
        int currentLevel = buffer.getInt();
        int speedLevel = buffer.getInt();
        long deltaTime = buffer.getLong();
        
        return new TickEvent(currentLevel, speedLevel, deltaTime);
    }
    
    @Override
    public String toString() {
        return "TickEvent{" +
                "currentLevel=" + currentLevel +
                ", speedLevel=" + speedLevel +
                ", deltaTime=" + deltaTime +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}