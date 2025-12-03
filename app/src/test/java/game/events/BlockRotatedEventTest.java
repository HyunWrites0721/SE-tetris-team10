package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BlockRotatedEvent 테스트")
class BlockRotatedEventTest {
    
    @Test
    @DisplayName("BlockRotatedEvent 생성 테스트")
    void testBlockRotatedEventCreation() {
        BlockRotatedEvent event = new BlockRotatedEvent(5, 10, 1, 1);
        
        assertNotNull(event);
        assertEquals(5, event.getX());
        assertEquals(10, event.getY());
        assertEquals(1, event.getBlockType());
        assertEquals(1, event.getRotation());
        assertEquals("BLOCK_ROTATED", event.getEventType());
    }
    
    @Test
    @DisplayName("회전 상태 변화 테스트")
    void testRotationStates() {
        for (int rotation = 0; rotation < 4; rotation++) {
            BlockRotatedEvent event = new BlockRotatedEvent(5, 10, 1, rotation);
            assertEquals(rotation, event.getRotation());
        }
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 테스트")
    void testSerializationRoundTrip() {
        BlockRotatedEvent original = new BlockRotatedEvent(8, 12, 4, 3);
        
        byte[] serialized = original.serialize();
        
        BlockRotatedEvent deserialized = new BlockRotatedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getX(), deserialized.getX());
        assertEquals(original.getY(), deserialized.getY());
        assertEquals(original.getBlockType(), deserialized.getBlockType());
        assertEquals(original.getRotation(), deserialized.getRotation());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        BlockRotatedEvent event = new BlockRotatedEvent(5, 10, 2, 2);
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("BlockRotatedEvent"));
        assertTrue(str.contains("rotation=2"));
    }
}
