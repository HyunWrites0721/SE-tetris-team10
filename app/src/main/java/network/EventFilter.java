package network;

import game.events.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 네트워크 전송이 필요한 이벤트만 필터링하는 클래스
 * 
 * 불필요한 이벤트(예: TickEvent, 내부 상태 업데이트)를 차단하여
 * 네트워크 트래픽을 줄이고 성능을 향상시킵니다.
 */
public class EventFilter {
    
    /**
     * 네트워크 동기화가 필요한 이벤트 타입 목록
     * 
     * 포함된 이벤트만 네트워크로 전송됩니다:
     * - BlockSpawnedEvent: 새 블록 생성 (상대방 화면에 표시 필요)
     * - BlockMovedEvent: 블록 이동 (실시간 동기화 필요)
     * - BlockRotatedEvent: 블록 회전 (실시간 동기화 필요)
     * - BlockPlacedEvent: 블록 배치 (상대방 화면 업데이트 필요)
     * - LineClearedEvent: 라인 삭제 (공격 판정 필요)
     * - ScoreUpdateEvent: 점수 업데이트 (상대방 화면에 표시 필요)
     * - GameOverEvent: 게임 오버 (승패 판정 필요)
     */
    private static final Set<Class<? extends GameEvent>> SYNC_EVENTS = new HashSet<>();
    
    static {
        // 블록 관련 이벤트 (필수 동기화)
        SYNC_EVENTS.add(BlockSpawnedEvent.class);
        SYNC_EVENTS.add(BlockMovedEvent.class);
        SYNC_EVENTS.add(BlockRotatedEvent.class);
        SYNC_EVENTS.add(BlockPlacedEvent.class);
        
        // 게임 진행 이벤트 (필수 동기화)
        SYNC_EVENTS.add(LineClearedEvent.class);
        SYNC_EVENTS.add(ScoreUpdateEvent.class);
        SYNC_EVENTS.add(GameOverEvent.class);
        
        // 아이템 관련 이벤트 (있는 경우)
        try {
            Class<?> itemEventClass = Class.forName("game.events.ItemUsedEvent");
            if (GameEvent.class.isAssignableFrom(itemEventClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends GameEvent> itemEvent = (Class<? extends GameEvent>) itemEventClass;
                SYNC_EVENTS.add(itemEvent);
            }
        } catch (ClassNotFoundException e) {
            // ItemUsedEvent가 없으면 무시
        }
    }
    
    /**
     * 이벤트가 네트워크 동기화가 필요한지 확인
     * 
     * @param event 확인할 이벤트
     * @return 동기화가 필요하면 true, 아니면 false
     */
    public static boolean shouldSync(GameEvent event) {
        if (event == null) {
            return false;
        }
        
        // 이벤트 클래스가 SYNC_EVENTS에 포함되어 있는지 확인
        boolean shouldSync = SYNC_EVENTS.contains(event.getClass());
        
        // 디버그 로깅 (필터링된 이벤트는 로그 출력 안 함)
        if (!shouldSync) {
            // TickEvent 등 빈번한 이벤트는 로그 스팸 방지
            String eventName = event.getClass().getSimpleName();
            if (!eventName.contains("Tick") && !eventName.contains("Update")) {
                System.out.println("🚫 [EventFilter] 필터링됨: " + eventName);
            }
        }
        
        return shouldSync;
    }
    
    /**
     * 동기화 대상 이벤트 타입 목록 반환 (읽기 전용)
     * 
     * @return 동기화 대상 이벤트 클래스 Set
     */
    public static Set<Class<? extends GameEvent>> getSyncEvents() {
        return new HashSet<>(SYNC_EVENTS);
    }
    
    /**
     * 특정 이벤트 타입이 동기화 대상인지 확인
     * 
     * @param eventClass 확인할 이벤트 클래스
     * @return 동기화 대상이면 true
     */
    public static boolean isSyncEvent(Class<? extends GameEvent> eventClass) {
        return SYNC_EVENTS.contains(eventClass);
    }
    
    /**
     * 통계: 동기화 대상 이벤트 타입 수
     * 
     * @return 동기화 대상 이벤트 타입 개수
     */
    public static int getSyncEventCount() {
        return SYNC_EVENTS.size();
    }
    
    /**
     * 디버그: 모든 동기화 대상 이벤트 출력
     */
    public static void printSyncEvents() {
        System.out.println("📋 동기화 대상 이벤트 목록:");
        for (Class<? extends GameEvent> eventClass : SYNC_EVENTS) {
            System.out.println("  - " + eventClass.getSimpleName());
        }
        System.out.println("총 " + SYNC_EVENTS.size() + "개 이벤트 타입");
    }
}
