package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LevelUpEvent 테스트")
class LevelUpEventTest {
    
    @Test
    @DisplayName("LevelUpEvent 생성 테스트")
    void testLevelUpEventCreation() {
        LevelUpEvent event = new LevelUpEvent(5, 1);
        
        assertNotNull(event);
        assertEquals(5, event.getNewLevel());
        assertEquals(1, event.getPlayerId());
        assertEquals("LEVEL_UP", event.getEventType());
        assertTrue(event.getTimestamp() > 0);
    }
    
    @Test
    @DisplayName("기본 생성자 테스트")
    void testDefaultConstructor() {
        LevelUpEvent event = new LevelUpEvent();
        
        assertNotNull(event);
        assertEquals("LEVEL_UP", event.getEventType());
    }
    
    @Test
    @DisplayName("직렬화 테스트")
    void testSerialization() {
        LevelUpEvent event = new LevelUpEvent(3, 2);
        
        byte[] serialized = event.serialize();
        
        assertNotNull(serialized);
        assertEquals(16, serialized.length); // long(8) + int(4) + int(4)
    }
    
    @Test
    @DisplayName("역직렬화 테스트")
    void testDeserialization() {
        LevelUpEvent original = new LevelUpEvent(7, 3);
        
        byte[] serialized = original.serialize();
        
        LevelUpEvent deserialized = new LevelUpEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(7, deserialized.getNewLevel());
        assertEquals(3, deserialized.getPlayerId());
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 왕복 테스트")
    void testSerializationRoundTrip() {
        LevelUpEvent original = new LevelUpEvent(10, 5);
        
        byte[] serialized = original.serialize();
        
        LevelUpEvent deserialized = new LevelUpEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getNewLevel(), deserialized.getNewLevel());
        assertEquals(original.getPlayerId(), deserialized.getPlayerId());
    }
    
    @Test
    @DisplayName("레벨 1 테스트")
    void testLevel1() {
        LevelUpEvent event = new LevelUpEvent(1, 1);
        
        assertEquals(1, event.getNewLevel());
    }
    
    @Test
    @DisplayName("높은 레벨 테스트")
    void testHighLevel() {
        LevelUpEvent event = new LevelUpEvent(99, 1);
        
        assertEquals(99, event.getNewLevel());
        
        byte[] serialized = event.serialize();
        LevelUpEvent deserialized = new LevelUpEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(99, deserialized.getNewLevel());
    }
    
    @Test
    @DisplayName("여러 플레이어 테스트")
    void testMultiplePlayers() {
        LevelUpEvent event1 = new LevelUpEvent(2, 1);
        LevelUpEvent event2 = new LevelUpEvent(3, 2);
        
        assertEquals(1, event1.getPlayerId());
        assertEquals(2, event2.getPlayerId());
        
        assertNotEquals(event1.getPlayerId(), event2.getPlayerId());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        LevelUpEvent event = new LevelUpEvent(5, 1);
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("LevelUpEvent"));
        assertTrue(str.contains("newLevel=5"));
        assertTrue(str.contains("playerId=1"));
    }
}
