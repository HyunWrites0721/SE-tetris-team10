package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScoreUpdateEvent 테스트")
class ScoreUpdateEventTest {
    
    @Test
    @DisplayName("ScoreUpdateEvent 생성 테스트")
    void testScoreUpdateEventCreation() {
        ScoreUpdateEvent event = new ScoreUpdateEvent(1000);
        
        assertNotNull(event);
        assertEquals(1000, event.getNewScore());
        assertEquals("SCORE_UPDATE", event.getEventType());
        assertTrue(event.getTimestamp() > 0);
    }
    
    @Test
    @DisplayName("기본 생성자 테스트")
    void testDefaultConstructor() {
        ScoreUpdateEvent event = new ScoreUpdateEvent();
        
        assertNotNull(event);
        assertEquals("SCORE_UPDATE", event.getEventType());
    }
    
    @Test
    @DisplayName("직렬화 테스트")
    void testSerialization() {
        ScoreUpdateEvent event = new ScoreUpdateEvent(5000);
        
        byte[] serialized = event.serialize();
        
        assertNotNull(serialized);
        assertEquals(4, serialized.length); // int = 4 bytes
        
        // ByteBuffer로 검증
        int deserializedScore = ByteBuffer.wrap(serialized).getInt();
        assertEquals(5000, deserializedScore);
    }
    
    @Test
    @DisplayName("역직렬화 테스트")
    void testDeserialization() {
        int originalScore = 3000;
        byte[] data = ByteBuffer.allocate(4).putInt(originalScore).array();
        
        ScoreUpdateEvent event = new ScoreUpdateEvent();
        event.deserialize(data);
        
        assertEquals(originalScore, event.getNewScore());
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 왕복 테스트")
    void testSerializationRoundTrip() {
        ScoreUpdateEvent original = new ScoreUpdateEvent(12345);
        
        byte[] serialized = original.serialize();
        
        ScoreUpdateEvent deserialized = new ScoreUpdateEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getNewScore(), deserialized.getNewScore());
    }
    
    @Test
    @DisplayName("0점 테스트")
    void testZeroScore() {
        ScoreUpdateEvent event = new ScoreUpdateEvent(0);
        
        assertEquals(0, event.getNewScore());
        
        byte[] serialized = event.serialize();
        ScoreUpdateEvent deserialized = new ScoreUpdateEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(0, deserialized.getNewScore());
    }
    
    @Test
    @DisplayName("음수 점수 테스트")
    void testNegativeScore() {
        ScoreUpdateEvent event = new ScoreUpdateEvent(-100);
        
        assertEquals(-100, event.getNewScore());
    }
    
    @Test
    @DisplayName("최대 점수 테스트")
    void testMaxScore() {
        ScoreUpdateEvent event = new ScoreUpdateEvent(Integer.MAX_VALUE);
        
        assertEquals(Integer.MAX_VALUE, event.getNewScore());
        
        byte[] serialized = event.serialize();
        ScoreUpdateEvent deserialized = new ScoreUpdateEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(Integer.MAX_VALUE, deserialized.getNewScore());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        ScoreUpdateEvent event = new ScoreUpdateEvent(1000);
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("ScoreUpdateEvent"));
        assertTrue(str.contains("SCORE_UPDATE"));
    }
}
