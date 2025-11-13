package game.listeners;

import game.events.*;

/**
 * 네트워크 전송을 담당하는 이벤트 리스너
 * 멀티플레이어 모드에서 이벤트를 다른 플레이어에게 전송
 */
public class NetworkListener implements EventListener<GameEvent> {
    private boolean isNetworkEnabled;
    private String playerName;
    
    public NetworkListener(boolean isNetworkEnabled, String playerName) {
        this.isNetworkEnabled = isNetworkEnabled;
        this.playerName = playerName != null ? playerName : "Player";
    }
    
    public void setNetworkEnabled(boolean enabled) {
        this.isNetworkEnabled = enabled;
    }
    
    @Override
    public void onEvent(GameEvent event) {
        if (!isNetworkEnabled) return;
        
        try {
            // 네트워크로 이벤트 전송 (실제 네트워크 구현은 추후)
            sendEventOverNetwork(event);
        } catch (Exception e) {
            System.err.println("Failed to send event over network: " + e.getMessage());
        }
    }
    
    private void sendEventOverNetwork(GameEvent event) {
        // 직렬화하여 네트워크로 전송
        byte[] data = event.serialize();
        
        // 실제 네트워크 전송 로직 (NetworkManager 구현 후 추가 예정)
        System.out.println("Network: Sending " + event.getEventType() + 
                         " event (" + data.length + " bytes) from " + playerName);
        
        // TODO: 실제 네트워크 전송
        // networkManager.sendEventData(event.getEventType(), data);
    }
    
    /**
     * 다른 플레이어로부터 받은 이벤트 데이터를 처리
     * @param eventType 이벤트 타입
     * @param data 직렬화된 이벤트 데이터
     * @return 역직렬화된 이벤트 객체
     */
    public GameEvent receiveEventFromNetwork(String eventType, byte[] data) {
        try {
            GameEvent event = createEventFromType(eventType);
            if (event != null) {
                event.deserialize(data);
                System.out.println("Network: Received " + eventType + " event");
                return event;
            }
        } catch (Exception e) {
            System.err.println("Failed to deserialize network event: " + e.getMessage());
        }
        return null;
    }
    
    private GameEvent createEventFromType(String eventType) {
        switch (eventType) {
            case "BLOCK_PLACED":
                return new BlockPlacedEvent();
            case "LINE_CLEARED":
                return new LineClearedEvent();
            case "LEVEL_UP":
                return new LevelUpEvent();
            case "GAME_OVER":
                return new GameOverEvent();
            case "ITEM_ACTIVATED":
                return new ItemActivatedEvent();
            default:
                System.err.println("Unknown event type: " + eventType);
                return null;
        }
    }
}