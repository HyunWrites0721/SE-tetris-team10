package network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import game.events.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * EventFilter 테스트
 */
class EventFilterTest {
    
    @BeforeEach
    void setUp() {
        // EventFilter는 static 메서드만 사용하므로 초기화 불필요
    }
    
    @Test
    void testBlockSpawnedEvent_ShouldSync() {
        BlockSpawnedEvent event = new BlockSpawnedEvent();
        assertTrue(EventFilter.shouldSync(event), "BlockSpawnedEvent는 동기화 필요");
    }
    
    @Test
    void testBlockMovedEvent_ShouldSync() {
        BlockMovedEvent event = new BlockMovedEvent(0, 0, 0, 0);
        assertTrue(EventFilter.shouldSync(event), "BlockMovedEvent는 동기화 필요");
    }
    
    @Test
    void testBlockRotatedEvent_ShouldSync() {
        BlockRotatedEvent event = new BlockRotatedEvent();
        assertTrue(EventFilter.shouldSync(event), "BlockRotatedEvent는 동기화 필요");
    }
    
    @Test
    void testBlockPlacedEvent_ShouldSync() {
        BlockPlacedEvent event = new BlockPlacedEvent();
        assertTrue(EventFilter.shouldSync(event), "BlockPlacedEvent는 동기화 필요");
    }
    
    @Test
    void testLineClearedEvent_ShouldSync() {
        LineClearedEvent event = new LineClearedEvent();
        assertTrue(EventFilter.shouldSync(event), "LineClearedEvent는 동기화 필요");
    }
    
    @Test
    void testScoreUpdateEvent_ShouldSync() {
        ScoreUpdateEvent event = new ScoreUpdateEvent();
        assertTrue(EventFilter.shouldSync(event), "ScoreUpdateEvent는 동기화 필요");
    }
    
    @Test
    void testGameOverEvent_ShouldSync() {
        GameOverEvent event = new GameOverEvent();
        assertTrue(EventFilter.shouldSync(event), "GameOverEvent는 동기화 필요");
    }
    
    @Test
    void testTickEvent_ShouldNotSync() {
        TickEvent event = new TickEvent();
        assertFalse(EventFilter.shouldSync(event), "TickEvent는 동기화 불필요 (너무 빈번)");
    }
    
    @Test
    void testNullEvent_ShouldNotSync() {
        assertFalse(EventFilter.shouldSync(null), "null 이벤트는 동기화 불필요");
    }
    
    @Test
    void testIsSyncEvent() {
        assertTrue(EventFilter.isSyncEvent(BlockSpawnedEvent.class));
        assertTrue(EventFilter.isSyncEvent(LineClearedEvent.class));
        assertFalse(EventFilter.isSyncEvent(TickEvent.class));
    }
    
    @Test
    void testGetSyncEventCount() {
        int count = EventFilter.getSyncEventCount();
        assertTrue(count >= 7, "최소 7개 이상의 동기화 이벤트 타입 존재");
    }
    
    @Test
    void testGetSyncEvents() {
        var syncEvents = EventFilter.getSyncEvents();
        assertNotNull(syncEvents);
        assertTrue(syncEvents.contains(BlockSpawnedEvent.class));
        assertTrue(syncEvents.contains(GameOverEvent.class));
        assertFalse(syncEvents.contains(TickEvent.class));
    }
    
    @Test
    void testGetSyncEvents_Immutable() {
        var syncEvents = EventFilter.getSyncEvents();
        int originalSize = syncEvents.size();
        
        // 반환된 Set 수정 시도
        syncEvents.add(TickEvent.class);
        
        // 원본은 변경되지 않아야 함
        assertEquals(originalSize, EventFilter.getSyncEventCount());
    }
    
    @Test
    void testPrintSyncEvents() {
        // 예외 없이 실행되는지 확인
        assertDoesNotThrow(() -> EventFilter.printSyncEvents());
    }
    
    @Test
    void testMultipleEvents() {
        // 여러 이벤트 동시 테스트
        GameEvent[] shouldSync = {
            new BlockSpawnedEvent(),
            new BlockMovedEvent(0, 0, 0, 0),
            new LineClearedEvent(),
            new GameOverEvent()
        };
        
        for (GameEvent event : shouldSync) {
            assertTrue(EventFilter.shouldSync(event), 
                event.getClass().getSimpleName() + "는 동기화 필요");
        }
        
        // 동기화 불필요한 이벤트
        assertFalse(EventFilter.shouldSync(new TickEvent()));
    }
}
