package p2p;

import game.events.*;
import network.MessageSender;
import network.MessageReceiver;
import network.messages.GameEventMessage;
import network.messages.NetworkMessage;
import network.messages.MessageType;

import java.util.Set;

/**
 * P2P 네트워크 대전에서 로컬/원격 게임 이벤트를 동기화하는 클래스
 * 로컬 이벤트를 네트워크로 전송하고, 수신한 이벤트를 원격 EventBus에 발행합니다.
 */
public class EventSynchronizer implements MessageReceiver.MessageListener {
    
    private final EventBus localEventBus;      // 내 게임의 EventBus
    private final EventBus remoteEventBus;     // 상대방 게임의 EventBus (화면만 표시)
    private final MessageSender sender;
    private final int myPlayerId;              // 1 (서버) 또는 2 (클라이언트)
    
    // 네트워크로 전송할 이벤트 타입 (실시간 동기화)
    // TICK만 제외 (각자 처리, 너무 빈번함)
    private static final Set<String> SYNC_EVENTS = Set.of(
        "BLOCK_MOVED",      // 블록 이동 (실시간)
        "BLOCK_ROTATED",    // 블록 회전 (실시간)
        "BLOCK_PLACED",     // 블록 고정
        "LINE_CLEARED",     // 줄 삭제
        "SCORE_UPDATE",     // 점수 업데이트 (실시간)
        "GAME_OVER",        // 게임 오버
        "LEVEL_UP",         // 레벨 업
        "ITEM_ACTIVATED"    // 아이템 사용
    );
    
    /**
     * EventSynchronizer 생성자
     * 
     * @param localEventBus 내 게임의 EventBus
     * @param remoteEventBus 상대방 게임의 EventBus
     * @param sender 메시지 전송 객체
     * @param myPlayerId 내 플레이어 ID (1=서버, 2=클라이언트)
     */
    public EventSynchronizer(
        EventBus localEventBus,
        EventBus remoteEventBus,
        MessageSender sender,
        int myPlayerId
    ) {
        this.localEventBus = localEventBus;
        this.remoteEventBus = remoteEventBus;
        this.sender = sender;
        this.myPlayerId = myPlayerId;
        
        // 내 게임의 모든 이벤트 구독
        subscribeToLocalEvents();
        
        System.out.println("EventSynchronizer 생성: PlayerId=" + myPlayerId);
    }
    
    /**
     * 로컬 게임의 이벤트를 구독하여 네트워크로 전송
     */
    private void subscribeToLocalEvents() {
        // 전송해야 할 각 이벤트 타입에 대해 리스너 등록
        // 우선순위 999: 다른 리스너보다 먼저 실행되어 네트워크로 즉시 전송
        localEventBus.subscribe(BlockMovedEvent.class, this::sendEvent, 999);      // 실시간 이동
        localEventBus.subscribe(BlockRotatedEvent.class, this::sendEvent, 999);    // 실시간 회전
        localEventBus.subscribe(BlockPlacedEvent.class, this::sendEvent, 999);     // 블록 고정
        localEventBus.subscribe(LineClearedEvent.class, this::sendEvent, 999);     // 줄 삭제
        localEventBus.subscribe(ScoreUpdateEvent.class, this::sendEvent, 999);     // 점수 업데이트
        localEventBus.subscribe(GameOverEvent.class, this::sendEvent, 999);        // 게임 오버
        localEventBus.subscribe(LevelUpEvent.class, this::sendEvent, 999);         // 레벨 업
        localEventBus.subscribe(ItemActivatedEvent.class, this::sendEvent, 999);   // 아이템
    }
    
    /**
     * 이벤트를 네트워크로 전송
     * 
     * @param event 전송할 게임 이벤트
     */
    private void sendEvent(GameEvent event) {
        if (!SYNC_EVENTS.contains(event.getEventType())) {
            return;  // 전송 불필요한 이벤트
        }
        
        try {
            GameEventMessage message = new GameEventMessage(event, myPlayerId);
            boolean sent = sender.sendMessage(message);
            
            if (sent) {
                System.out.println("이벤트 전송: " + event.getEventType() + " (Player " + myPlayerId + ")");
            } else {
                System.err.println("이벤트 전송 실패: " + event);
            }
        } catch (Exception e) {
            System.err.println("이벤트 전송 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 네트워크에서 수신한 메시지 처리 (MessageReceiver.MessageListener 구현)
     * 
     * @param message 수신한 네트워크 메시지
     */
    @Override
    public void onMessageReceived(NetworkMessage message) {
        if (message.getType() != MessageType.GAME_EVENT) {
            return;  // 게임 이벤트만 처리
        }
        
        try {
            GameEventMessage eventMsg = (GameEventMessage) message;
            GameEvent event = eventMsg.toGameEvent();
            
            if (event != null) {
                // 상대방 EventBus에 발행 (상대방 화면 업데이트)
                remoteEventBus.publish(event);
                
                System.out.println("원격 이벤트 수신: " + event.getEventType() + 
                                 " (Player " + eventMsg.getPlayerId() + ")");
                
                // 게임 오버 이벤트는 특별 처리
                if (event instanceof GameOverEvent) {
                    handleRemoteGameOver((GameOverEvent) event);
                }
            }
        } catch (Exception e) {
            System.err.println("이벤트 수신 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 상대방이 게임 오버되었을 때 처리
     * 
     * @param event 게임 오버 이벤트
     */
    private void handleRemoteGameOver(GameOverEvent event) {
        System.out.println("상대방 게임 오버! 최종 점수: " + event.getFinalScore());
        // TODO: 승리 화면 표시 또는 게임 종료 처리
    }
    
    /**
     * 네트워크 연결이 끊겼을 때 처리 (MessageReceiver.MessageListener 구현)
     */
    @Override
    public void onConnectionLost() {
        System.err.println("⚠️ 네트워크 연결 끊김! 게임을 종료합니다.");
        // TODO: 연결 끊김 UI 표시 및 게임 중단 처리
    }
    
    /**
     * 동기화 종료 (리스너 해제)
     */
    public void shutdown() {
        // EventBus에서 리스너를 자동으로 관리하므로 별도 해제 불필요
        // 필요 시 명시적으로 해제 가능
        System.out.println("EventSynchronizer 종료");
    }
}
