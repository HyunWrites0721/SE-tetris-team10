package p2p;

import game.events.*;
import network.MessageSender;
import network.MessageReceiver;
import network.messages.GameEventMessage;
import network.messages.NetworkMessage;
import network.messages.MessageType;
import network.EventFilter;

import java.util.Set;
import network.messages.AttackMessage;
import game.events.AttackEvent;

/**
 * P2P 네트워크 대전에서 로컬/원격 게임 이벤트를 동기화하는 클래스
 * 로컬 이벤트를 네트워크로 전송하고, 수신한 이벤트를 원격 EventBus에 발행합니다.
 * 
 * Phase 6: EventFilter를 사용하여 불필요한 이벤트 전송을 차단하고 성능을 최적화합니다.
 */
public class EventSynchronizer implements MessageReceiver.MessageListener {
    
    private final EventBus localEventBus;      // 내 게임의 EventBus
    private final EventBus remoteEventBus;     // 상대방 게임의 EventBus (화면만 표시)
    private final MessageSender sender;
    private final int myPlayerId;              // 1 (서버) 또는 2 (클라이언트)
    
    // 성능 통계
    private long totalEventsSent = 0;          // 전송한 이벤트 수
    private long totalEventsFiltered = 0;      // 필터링된 이벤트 수
    private long lastStatsTime = System.currentTimeMillis();
    
    // 네트워크로 전송할 이벤트 타입 (실시간 동기화)
    // TICK 제외: 너무 빈번하여 네트워크 부하 발생
    // 대신 BlockMoved 이벤트로 자동 낙하 위치도 전송됨
    private static final Set<String> SYNC_EVENTS = Set.of(
        "BLOCK_SPAWNED",    // 블록 생성 (중요!)
        "BLOCK_MOVED",      // 블록 이동 (실시간) - TICK 낙하 포함
        "BLOCK_ROTATED",    // 블록 회전 (실시간)
        "BLOCK_PLACED",     // 블록 고정
        "LINE_CLEARED",     // 줄 삭제
            "ATTACK_APPLIED",
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
        
        // Debug: 어떤 EventBus 인스턴스에 구독했는지 출력
        try {
            System.out.println("EventSynchronizer 생성: PlayerId=" + myPlayerId
                + ", localEventBusId=" + System.identityHashCode(this.localEventBus)
                + ", remoteEventBusId=" + System.identityHashCode(this.remoteEventBus));
        } catch (Throwable __) {
            System.out.println("EventSynchronizer 생성: PlayerId=" + myPlayerId);
        }
    }
    
    /**
     * 로컬 게임의 이벤트를 구독하여 네트워크로 전송
     */
    private void subscribeToLocalEvents() {
        // 전송해야 할 각 이벤트 타입에 대해 리스너 등록
        // 우선순위 999: 다른 리스너보다 먼저 실행되어 네트워크로 즉시 전송
        // TICK은 제외 - BlockMoved로 자동 낙하 위치 전송
        localEventBus.subscribe(BlockSpawnedEvent.class, this::sendEvent, 999);    // 블록 생성
        localEventBus.subscribe(BlockMovedEvent.class, this::sendEvent, 999);      // 실시간 이동
        localEventBus.subscribe(BlockRotatedEvent.class, this::sendEvent, 999);    // 실시간 회전
        localEventBus.subscribe(BlockPlacedEvent.class, this::sendEvent, 999);     // 블록 고정
        localEventBus.subscribe(LineClearedEvent.class, this::sendEvent, 999);     // 줄 삭제
        localEventBus.subscribe(ScoreUpdateEvent.class, this::sendEvent, 999);     // 점수 업데이트
        localEventBus.subscribe(GameOverEvent.class, this::sendEvent, 999);        // 게임 오버
        localEventBus.subscribe(LevelUpEvent.class, this::sendEvent, 999);         // 레벨 업
        localEventBus.subscribe(ItemActivatedEvent.class, this::sendEvent, 999);   // 아이템
        localEventBus.subscribe(game.events.AttackAppliedEvent.class, this::sendEvent, 999); // 공격 적용 (시각적 동기화용)
    }
    
    /**
     * 이벤트를 네트워크로 전송
     * Phase 6: EventFilter를 사용하여 필터링 적용
     * 
     * @param event 전송할 게임 이벤트
     */
    private void sendEvent(GameEvent event) {
        String eventType = event.getEventType();
        
        // Phase 6: EventFilter로 1차 필터링
        if (!EventFilter.shouldSync(event)) {
            totalEventsFiltered++;
            // 디버그 로그 (필터링된 이벤트는 조용히 차단)
            return;
        }
        
        // 2차 필터링: SYNC_EVENTS 체크 (기존 로직 유지)
        if (!SYNC_EVENTS.contains(eventType)) {
            totalEventsFiltered++;
            return;
        }
        
        try {
            GameEventMessage message = new GameEventMessage(event, myPlayerId);
            boolean sent = sender.sendMessage(message);
            
            if (sent) {
                totalEventsSent++;
                System.out.println("📤 [SEND] " + eventType + " (Player " + myPlayerId + ")");
                
                // 10초마다 성능 통계 출력
                printStatsIfNeeded();
            } else {
                System.err.println("❌ [SEND] 전송 실패: " + eventType);
            }
        } catch (Exception e) {
            System.err.println("❌ [SEND] 전송 중 오류: " + e.getMessage());
            e.printStackTrace();
        }

        // 추가: LineClearedEvent인 경우 공격 계산 후 AttackMessage로 전송
        try {
            if (event instanceof LineClearedEvent) {
                LineClearedEvent le = (LineClearedEvent) event;
                int lines = le.getClearedLines().length;
                int attackLines = 0;
                if (lines >= 2) attackLines = lines; // 간단 규칙: 2줄 이상이면 같은 수만큼 공격
                if (attackLines > 0) {
                    // include cleared line pattern so opponent can reproduce holes
                    int[][] pattern = le.getLastBlockPattern();
                    int blockX = le.getLastBlockX();
                    
                    // 패턴 디버그 로그
                    game.util.GameLogger.debug("EventSynchronizer 전송 전: pattern=" + 
                        (pattern!=null?(pattern.length+"x"+(pattern.length>0?pattern[0].length:0)):"null"));
                    if (pattern != null && pattern.length > 0 && pattern[0].length > 0) {
                        StringBuilder sb = new StringBuilder("  pattern[0]=");
                        for (int j = 0; j < Math.min(pattern[0].length, 10); j++) {
                            sb.append(pattern[0][j]);
                        }
                        game.util.GameLogger.debug(sb.toString());
                    }
                    
                    AttackMessage am = new AttackMessage(attackLines, myPlayerId, pattern, blockX);
                    boolean asent = sender.sendMessage(am);
                    if (asent) {
                        totalEventsSent++;
                        System.out.println("📤 [SEND] AttackMessage attackLines=" + attackLines + " (Player " + myPlayerId + ") pattern=" + (pattern!=null?(pattern.length+"x"+(pattern.length>0?pattern[0].length:0)):"<none>"));
                    } else {
                        System.err.println("❌ [SEND] AttackMessage 전송 실패");
                    }
                }
            }
        } catch (Throwable __) {
            // don't let attack message failure affect main flow
            System.err.println("[EventSynchronizer] AttackMessage 전송 중 예외: " + __.getMessage());
        }
    }
    
    /**
     * 성능 통계 출력 (10초마다)
     */
    private void printStatsIfNeeded() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastStatsTime;
        
        if (elapsed >= 10000) {  // 10초마다
            long totalProcessed = totalEventsSent + totalEventsFiltered;
            double filterRate = totalProcessed > 0 ? 
                (totalEventsFiltered * 100.0 / totalProcessed) : 0;
            
            System.out.println("📊 [성능 통계] Player " + myPlayerId);
            System.out.println("   전송: " + totalEventsSent + " 이벤트");
            System.out.println("   필터링: " + totalEventsFiltered + " 이벤트");
            System.out.println("   필터율: " + String.format("%.1f", filterRate) + "%");
            System.out.println("   기간: " + (elapsed / 1000) + "초");
            
            // 통계 리셋
            totalEventsSent = 0;
            totalEventsFiltered = 0;
            lastStatsTime = now;
        }
    }
    
    /**
     * 네트워크에서 수신한 메시지 처리 (MessageReceiver.MessageListener 구현)
     * 
     * @param message 수신한 네트워크 메시지
     */
    @Override
    public void onMessageReceived(NetworkMessage message) {
        System.out.println("📨 [EventSynchronizer] 메시지 수신: type=" + message.getType());
        // 분기: GAME_EVENT / ATTACK 각각 처리
        if (message.getType() == MessageType.GAME_EVENT) {
            try {
                GameEventMessage eventMsg = (GameEventMessage) message;
                GameEvent event = eventMsg.toGameEvent();

                if (event != null) {
                    System.out.println("📥 [NETWORK] 이벤트 수신: " + event.getEventType() + 
                                     " (Player " + eventMsg.getPlayerId() + ")");

                    // 상대방 EventBus에 발행 (상대방 화면 업데이트)
                    System.out.println("   🔄 remoteEventBus.publish() 호출...");
                    remoteEventBus.publish(event);

                    System.out.println("✅ [NETWORK] remoteEventBus에 발행 완료: " + event.getEventType());

                    // 게임 오버 이벤트는 특별 처리
                    if (event instanceof GameOverEvent) {
                        handleRemoteGameOver((GameOverEvent) event);
                    }
                } else {
                    System.err.println("❌ [NETWORK] 이벤트 역직렬화 실패!");
                }
            } catch (Exception e) {
                System.err.println("❌ [NETWORK] 이벤트 수신 중 오류: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        if (message.getType() == MessageType.ATTACK) {
            try {
                AttackMessage am = (AttackMessage) message;
                System.out.println("📥 [NETWORK] AttackMessage 수신: lines=" + am.getAttackLines() + " from=" + am.getPlayerId()
                    + " pattern=" + (am.getBlockPattern()!=null?(am.getBlockPattern().length+"x"+(am.getBlockPattern().length>0?am.getBlockPattern()[0].length:0)) : "<none>"));
                // 원격 EventBus에 AttackEvent로 발행 (include pattern)
                AttackEvent ae = new AttackEvent(am.getAttackLines(), am.getPlayerId(), am.getBlockPattern(), am.getBlockX());
                System.out.println("   🔄 remoteEventBus.publish() 호출... (AttackEvent)");
                remoteEventBus.publish(ae);
                System.out.println("✅ [NETWORK] remoteEventBus에 AttackEvent 발행 완료");
            } catch (Exception ex) {
                System.err.println("❌ [NETWORK] AttackMessage 처리 실패: " + ex.getMessage());
                ex.printStackTrace();
            }
            return;
        }

        // 그 외 메시지 타입은 처리 대상 아님
        System.out.println("   ⏭️  처리 대상이 아닌 메시지 타입: " + message.getType());
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
