package game.events;

import java.nio.ByteBuffer;

/**
 * 게임 틱 이벤트
 * 게임 루프의 각 틱마다 발생하는 이벤트
 */
public class TickEvent extends GameEvent {
    private final int currentLevel;
    private final int speedLevel;
    private final long deltaTime; // 이전 틱과의 시간 차이 (밀리초)
    
    public TickEvent(int currentLevel, int speedLevel, long deltaTime) {
        super("TICK");
        this.currentLevel = currentLevel;
        this.speedLevel = speedLevel;
        this.deltaTime = deltaTime;
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
        // 불변 객체이므로 역직렬화는 생성자에서 처리
        throw new UnsupportedOperationException("TickEvent는 불변 객체입니다. 팩토리 메서드를 사용하세요.");
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