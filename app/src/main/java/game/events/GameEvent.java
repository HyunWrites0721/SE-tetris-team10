package game.events;

import java.nio.ByteBuffer;

/**
 * 모든 게임 이벤트의 기본 클래스
 * 네트워크 전송을 위한 직렬화 기능 포함
 */
public abstract class GameEvent {
    private final long timestamp;
    private final String eventType;
    
    protected GameEvent(String eventType) {
        this.timestamp = System.currentTimeMillis();
        this.eventType = eventType;
    }
    
    public long getTimestamp() { 
        return timestamp; 
    }
    
    public String getEventType() { 
        return eventType; 
    }
    
    // 네트워크 전송을 위한 직렬화 메서드
    public abstract byte[] serialize();
    public abstract void deserialize(byte[] data);
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}