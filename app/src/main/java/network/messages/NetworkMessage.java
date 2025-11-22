package network.messages;

import java.io.Serializable;
import java.util.UUID;

/**
 * 네트워크로 전송될 모든 메시지의 기본 추상 클래스
 * 모든 네트워크 메시지는 이 클래스를 상속받아야 합니다.
 */
public abstract class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final long timestamp;        // 메시지 생성 시간
    private final String messageId;      // 고유 메시지 ID
    private final MessageType type;      // 메시지 타입
    
    /**
     * NetworkMessage 생성자
     * @param type 메시지 타입
     */
    protected NetworkMessage(MessageType type) {
        this.timestamp = System.currentTimeMillis();
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
    }
    
    // ===== Getter 메서드 =====
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public MessageType getType() {
        return type;
    }
    
    /**
     * 메시지의 경과 시간 계산 (밀리초)
     * @return 메시지가 생성된 후 경과한 시간
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("%s{id='%s', type=%s, timestamp=%d}", 
            getClass().getSimpleName(), 
            messageId.substring(0, 8), 
            type, 
            timestamp);
    }
}
