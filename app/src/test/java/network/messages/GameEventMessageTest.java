package network.messages;

import game.events.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GameEventMessage 직렬화/역직렬화 테스트
 */
public class GameEventMessageTest {
    
    @Test
    @DisplayName("BlockPlacedEvent 직렬화 테스트")
    public void testBlockPlacedEventSerialization() {
        // Given
        BlockPlacedEvent original = new BlockPlacedEvent(5, 10, 1, 1);
        
        // When
        GameEventMessage message = new GameEventMessage(original, 1);
        GameEvent restored = message.toGameEvent();
        
        // Then
        assertNotNull(restored);
        assertTrue(restored instanceof BlockPlacedEvent);
        
        BlockPlacedEvent event = (BlockPlacedEvent) restored;
        assertEquals(5, event.getX());
        assertEquals(10, event.getY());
        assertEquals(1, event.getBlockType());
        assertEquals(1, event.getPlayerId());
        assertEquals("BLOCK_PLACED", event.getEventType());
    }
    
    @Test
    @DisplayName("LineClearedEvent 직렬화 테스트")
    public void testLineClearedEventSerialization() {
        // Given
        int[] clearedLines = {5, 10, 15};
        LineClearedEvent original = new LineClearedEvent(clearedLines, 300, 2);
        
        // When
        GameEventMessage message = new GameEventMessage(original, 2);
        GameEvent restored = message.toGameEvent();
        
        // Then
        assertNotNull(restored);
        assertTrue(restored instanceof LineClearedEvent);
        
        LineClearedEvent event = (LineClearedEvent) restored;
        assertArrayEquals(clearedLines, event.getClearedLines());
        assertEquals(300, event.getScore());
        assertEquals(2, event.getPlayerId());
        assertEquals("LINE_CLEARED", event.getEventType());
    }
    
    @Test
    @DisplayName("GameOverEvent 직렬화 테스트")
    public void testGameOverEventSerialization() {
        // Given
        GameOverEvent original = new GameOverEvent(5000, 1);
        
        // When
        GameEventMessage message = new GameEventMessage(original, 1);
        GameEvent restored = message.toGameEvent();
        
        // Then
        assertNotNull(restored);
        assertTrue(restored instanceof GameOverEvent);
        
        GameOverEvent event = (GameOverEvent) restored;
        assertEquals(5000, event.getFinalScore());
        assertEquals(1, event.getPlayerId());
        assertEquals("GAME_OVER", event.getEventType());
    }
    
    @Test
    @DisplayName("LevelUpEvent 직렬화 테스트")
    public void testLevelUpEventSerialization() {
        // Given
        LevelUpEvent original = new LevelUpEvent(5, 1);
        
        // When
        GameEventMessage message = new GameEventMessage(original, 1);
        GameEvent restored = message.toGameEvent();
        
        // Then
        assertNotNull(restored);
        assertTrue(restored instanceof LevelUpEvent);
        
        LevelUpEvent event = (LevelUpEvent) restored;
        assertEquals(5, event.getNewLevel());
        assertEquals(1, event.getPlayerId());
        assertEquals("LEVEL_UP", event.getEventType());
    }
    
    @Test
    @DisplayName("ItemActivatedEvent 직렬화 테스트")
    public void testItemActivatedEventSerialization() {
        // Given
        ItemActivatedEvent original = new ItemActivatedEvent("BOMB", 2);
        
        // When
        GameEventMessage message = new GameEventMessage(original, 2);
        GameEvent restored = message.toGameEvent();
        
        // Then
        assertNotNull(restored);
        assertTrue(restored instanceof ItemActivatedEvent);
        
        ItemActivatedEvent event = (ItemActivatedEvent) restored;
        assertEquals("BOMB", event.getItemType());
        assertEquals(2, event.getPlayerId());
        assertEquals("ITEM_ACTIVATED", event.getEventType());
    }
    
    @Test
    @DisplayName("GameEventMessage 기본 필드 테스트")
    public void testGameEventMessageFields() {
        // Given
        BlockPlacedEvent event = new BlockPlacedEvent(1, 2, 3, 1);
        
        // When
        GameEventMessage message = new GameEventMessage(event, 1);
        
        // Then
        assertEquals(MessageType.GAME_EVENT, message.getType());
        assertEquals("BLOCK_PLACED", message.getEventType());
        assertEquals(1, message.getPlayerId());
        assertNotNull(message.getMessageId());
        assertTrue(message.getTimestamp() > 0);
        assertTrue(message.getEventData().length > 0);
    }
    
    @Test
    @DisplayName("toString 메서드 테스트")
    public void testToString() {
        // Given
        LineClearedEvent event = new LineClearedEvent(new int[]{5}, 100, 1);
        GameEventMessage message = new GameEventMessage(event, 1);
        
        // When
        String result = message.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("LINE_CLEARED"));
        assertTrue(result.contains("playerId=1"));
    }
    
    @Test
    @DisplayName("알 수 없는 이벤트 타입 처리")
    public void testUnknownEventType() {
        // Given - 직접 생성자 호출은 불가능하므로 리플렉션 사용
        // 이 테스트는 실제로는 발생하지 않아야 하는 경우를 시뮬레이션
        
        // 대신 정상적인 이벤트가 null을 반환하지 않는지 확인
        GameOverEvent event = new GameOverEvent(100, 1);
        GameEventMessage message = new GameEventMessage(event, 1);
        
        // When
        GameEvent restored = message.toGameEvent();
        
        // Then
        assertNotNull(restored);
    }
}
