package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BlockSpawnedEvent 테스트")
class BlockSpawnedEventTest {
    
    @Test
    @DisplayName("BlockSpawnedEvent 생성 테스트 (다음 블록 없음)")
    void testBlockSpawnedEventCreation() {
        BlockSpawnedEvent event = new BlockSpawnedEvent("blocks.IBlock", 5, 0);
        
        assertNotNull(event);
        assertEquals("blocks.IBlock", event.getBlockClassName());
        assertEquals(5, event.getX());
        assertEquals(0, event.getY());
        assertNull(event.getNextBlockClassName());
        assertEquals("BLOCK_SPAWNED", event.getEventType());
    }
    
    @Test
    @DisplayName("BlockSpawnedEvent 생성 테스트 (다음 블록 포함)")
    void testBlockSpawnedEventWithNext() {
        BlockSpawnedEvent event = new BlockSpawnedEvent("blocks.IBlock", 5, 0, "blocks.TBlock");
        
        assertNotNull(event);
        assertEquals("blocks.IBlock", event.getBlockClassName());
        assertEquals(5, event.getX());
        assertEquals(0, event.getY());
        assertEquals("blocks.TBlock", event.getNextBlockClassName());
    }
    
    @Test
    @DisplayName("여러 블록 타입 테스트")
    void testDifferentBlockTypes() {
        String[] blockTypes = {
            "blocks.IBlock", "blocks.OBlock", "blocks.TBlock",
            "blocks.JBlock", "blocks.LBlock", "blocks.SBlock", "blocks.ZBlock"
        };
        
        for (String blockType : blockTypes) {
            BlockSpawnedEvent event = new BlockSpawnedEvent(blockType, 5, 0);
            assertEquals(blockType, event.getBlockClassName());
        }
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 테스트 (다음 블록 없음)")
    void testSerializationRoundTrip() {
        BlockSpawnedEvent original = new BlockSpawnedEvent("blocks.TBlock", 4, 1);
        
        byte[] serialized = original.serialize();
        
        BlockSpawnedEvent deserialized = new BlockSpawnedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getBlockClassName(), deserialized.getBlockClassName());
        assertEquals(original.getX(), deserialized.getX());
        assertEquals(original.getY(), deserialized.getY());
        assertNull(deserialized.getNextBlockClassName());
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 테스트 (다음 블록 포함)")
    void testSerializationRoundTripWithNext() {
        BlockSpawnedEvent original = new BlockSpawnedEvent("blocks.IBlock", 5, 0, "blocks.SBlock");
        
        byte[] serialized = original.serialize();
        
        BlockSpawnedEvent deserialized = new BlockSpawnedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getBlockClassName(), deserialized.getBlockClassName());
        assertEquals(original.getX(), deserialized.getX());
        assertEquals(original.getY(), deserialized.getY());
        assertEquals(original.getNextBlockClassName(), deserialized.getNextBlockClassName());
    }
    
    @Test
    @DisplayName("긴 클래스 이름 테스트")
    void testLongClassName() {
        String longName = "blocks.VeryLongBlockClassNameForTesting";
        BlockSpawnedEvent event = new BlockSpawnedEvent(longName, 5, 0);
        
        assertEquals(longName, event.getBlockClassName());
        
        byte[] serialized = event.serialize();
        BlockSpawnedEvent deserialized = new BlockSpawnedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(longName, deserialized.getBlockClassName());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        BlockSpawnedEvent event = new BlockSpawnedEvent("blocks.IBlock", 5, 0, "blocks.TBlock");
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("BlockSpawnedEvent"));
        assertTrue(str.contains("blocks.IBlock"));
        assertTrue(str.contains("x=5"));
        assertTrue(str.contains("y=0"));
        assertTrue(str.contains("blocks.TBlock"));
    }
}
