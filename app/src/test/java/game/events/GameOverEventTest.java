package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameOverEvent 테스트")
class GameOverEventTest {
    
    @Test
    @DisplayName("GameOverEvent 생성 테스트")
    void testGameOverEventCreation() {
        GameOverEvent event = new GameOverEvent(10000, 1);
        
        assertNotNull(event);
        assertEquals(10000, event.getFinalScore());
        assertEquals(1, event.getPlayerId());
        assertEquals("GAME_OVER", event.getEventType());
        assertTrue(event.getTimestamp() > 0);
    }
    
    @Test
    @DisplayName("기본 생성자 테스트")
    void testDefaultConstructor() {
        GameOverEvent event = new GameOverEvent();
        
        assertNotNull(event);
        assertEquals("GAME_OVER", event.getEventType());
    }
    
    @Test
    @DisplayName("직렬화 테스트")
    void testSerialization() {
        GameOverEvent event = new GameOverEvent(5000, 2);
        
        byte[] serialized = event.serialize();
        
        assertNotNull(serialized);
        assertEquals(16, serialized.length); // long(8) + int(4) + int(4)
    }
    
    @Test
    @DisplayName("역직렬화 테스트")
    void testDeserialization() {
        GameOverEvent original = new GameOverEvent(15000, 3);
        
        byte[] serialized = original.serialize();
        
        GameOverEvent deserialized = new GameOverEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(15000, deserialized.getFinalScore());
        assertEquals(3, deserialized.getPlayerId());
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 왕복 테스트")
    void testSerializationRoundTrip() {
        GameOverEvent original = new GameOverEvent(99999, 1);
        
        byte[] serialized = original.serialize();
        
        GameOverEvent deserialized = new GameOverEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getFinalScore(), deserialized.getFinalScore());
        assertEquals(original.getPlayerId(), deserialized.getPlayerId());
    }
    
    @Test
    @DisplayName("0점으로 게임 오버 테스트")
    void testZeroScore() {
        GameOverEvent event = new GameOverEvent(0, 1);
        
        assertEquals(0, event.getFinalScore());
        
        byte[] serialized = event.serialize();
        GameOverEvent deserialized = new GameOverEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(0, deserialized.getFinalScore());
    }
    
    @Test
    @DisplayName("최대 점수 테스트")
    void testMaxScore() {
        GameOverEvent event = new GameOverEvent(Integer.MAX_VALUE, 1);
        
        assertEquals(Integer.MAX_VALUE, event.getFinalScore());
        
        byte[] serialized = event.serialize();
        GameOverEvent deserialized = new GameOverEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(Integer.MAX_VALUE, deserialized.getFinalScore());
    }
    
    @Test
    @DisplayName("여러 플레이어 게임 오버 테스트")
    void testMultiplePlayers() {
        GameOverEvent event1 = new GameOverEvent(5000, 1);
        GameOverEvent event2 = new GameOverEvent(8000, 2);
        
        assertEquals(1, event1.getPlayerId());
        assertEquals(2, event2.getPlayerId());
        
        assertTrue(event2.getFinalScore() > event1.getFinalScore());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        GameOverEvent event = new GameOverEvent(12345, 1);
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("GameOverEvent"));
        assertTrue(str.contains("finalScore=12345"));
        assertTrue(str.contains("playerId=1"));
    }
}
