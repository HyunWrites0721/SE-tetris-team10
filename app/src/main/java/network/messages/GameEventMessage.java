package network.messages;

import game.events.*;

/**
 * GameEvent를 네트워크로 전송하기 위한 래퍼 클래스
 * 기존 GameEvent의 serialize/deserialize 기능을 활용합니다.
 */
public class GameEventMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String eventType;  // 이벤트 타입 
    private final byte[] eventData;  // 직렬화된 이벤트 데이터
    private final int playerId;
    
    /**
     * GameEvent를 NetworkMessage로 변환
     * @param event 전송할 게임 이벤트
     * @param playerId 이벤트를 발생시킨 플레이어 ID (1 or 2)
     */
    public GameEventMessage(GameEvent event, int playerId) {
        super(MessageType.GAME_EVENT);
        this.eventType = event.getEventType();
        this.eventData = event.serialize();  // GameEvent -> byte[] 보내기
        this.playerId = playerId;
    }
    
    public String getEventType() { 
        return eventType; 
    }
    
    public byte[] getEventData() { 
        return eventData; 
    }
    
    public int getPlayerId() { 
        return playerId; 
    }
    
    /**
     * byte[] 데이터를 GameEvent 객체로 역직렬화
     * @return 역직렬화된 GameEvent 객체, 실패 시 null
     */
    public GameEvent toGameEvent() {
        GameEvent event = createEventInstance(eventType);   
        if (event != null) {
            try {
                event.deserialize(eventData);
            } catch (Exception e) {
                System.err.println("이벤트 역직렬화 실패: " + eventType);
                e.printStackTrace();
                return null;
            }
        }
        return event;
    }
    
    /**
     * eventType에 따라 적절한 GameEvent 인스턴스 생성 (Factory Pattern)
     */
    private GameEvent createEventInstance(String type) {
        switch (type) {
            case "BLOCK_MOVED":
                return new BlockMovedEvent();
            case "BLOCK_ROTATED":
                return new BlockRotatedEvent();
            case "BLOCK_PLACED":
                return new BlockPlacedEvent();
            case "LINE_CLEARED":
                return new LineClearedEvent();
            case "SCORE_UPDATE":
                return new ScoreUpdateEvent();
            case "GAME_OVER":
                return new GameOverEvent();
            case "LEVEL_UP":
                return new LevelUpEvent();
            case "ITEM_ACTIVATED":
                return new ItemActivatedEvent();
            default:
                System.err.println("알 수 없는 이벤트 타입: " + type);
                return null;
        }
    }
    
    @Override
    public String toString() {
        return "GameEventMessage{" +
                "eventType='" + eventType + '\'' +
                ", playerId=" + playerId +
                ", dataSize=" + eventData.length +
                ", messageId=" + getMessageId() +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
