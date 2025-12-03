package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BlockMovedEvent 테스트")
class BlockMovedEventTest {
    
    @Test
    @DisplayName("BlockMovedEvent 생성 테스트")
    void testBlockMovedEventCreation() {
        BlockMovedEvent event = new BlockMovedEvent(5, 10, 1, 0);
        
        assertNotNull(event);
        assertEquals(5, event.getX());
        assertEquals(10, event.getY());
        assertEquals(1, event.getBlockType());
        assertEquals(0, event.getRotation());
        assertEquals("BLOCK_MOVED", event.getEventType());
    }
    
    @Test
    @DisplayName("회전 상태 테스트")
    void testRotationStates() {
        for (int rotation = 0; rotation < 4; rotation++) {
            BlockMovedEvent event = new BlockMovedEvent(5, 10, 1, rotation);
            assertEquals(rotation, event.getRotation());
        }
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 테스트")
    void testSerializationRoundTrip() {
        BlockMovedEvent original = new BlockMovedEvent(7, 15, 3, 2);
        
        byte[] serialized = original.serialize();
        
        BlockMovedEvent deserialized = new BlockMovedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getX(), deserialized.getX());
        assertEquals(original.getY(), deserialized.getY());
        assertEquals(original.getBlockType(), deserialized.getBlockType());
        assertEquals(original.getRotation(), deserialized.getRotation());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        BlockMovedEvent event = new BlockMovedEvent(5, 10, 2, 1);
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("BlockMovedEvent"));
        assertTrue(str.contains("x=5"));
        assertTrue(str.contains("y=10"));
    }
}
