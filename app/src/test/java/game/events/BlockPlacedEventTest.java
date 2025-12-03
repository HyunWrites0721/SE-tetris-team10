package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BlockPlacedEvent 테스트")
class BlockPlacedEventTest {
    
    @Test
    @DisplayName("BlockPlacedEvent 생성 테스트")
    void testBlockPlacedEventCreation() {
        BlockPlacedEvent event = new BlockPlacedEvent(5, 10, 1, 1);
        
        assertNotNull(event);
        assertEquals(5, event.getX());
        assertEquals(10, event.getY());
        assertEquals(1, event.getBlockType());
        assertEquals(1, event.getPlayerId());
        assertEquals("BLOCK_PLACED", event.getEventType());
        assertTrue(event.getTimestamp() > 0);
    }
    
    @Test
    @DisplayName("기본 생성자 테스트")
    void testDefaultConstructor() {
        BlockPlacedEvent event = new BlockPlacedEvent();
        
        assertNotNull(event);
        assertEquals("BLOCK_PLACED", event.getEventType());
    }
    
    @Test
    @DisplayName("직렬화 테스트")
    void testSerialization() {
        BlockPlacedEvent event = new BlockPlacedEvent(7, 15, 2, 1);
        
        byte[] serialized = event.serialize();
        
        assertNotNull(serialized);
        assertEquals(24, serialized.length); // long(8) + int(4)*4
    }
    
    @Test
    @DisplayName("역직렬화 테스트")
    void testDeserialization() {
        BlockPlacedEvent original = new BlockPlacedEvent(3, 8, 4, 2);
        
        byte[] serialized = original.serialize();
        
        BlockPlacedEvent deserialized = new BlockPlacedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(3, deserialized.getX());
        assertEquals(8, deserialized.getY());
        assertEquals(4, deserialized.getBlockType());
        assertEquals(2, deserialized.getPlayerId());
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 왕복 테스트")
    void testSerializationRoundTrip() {
        BlockPlacedEvent original = new BlockPlacedEvent(10, 20, 7, 1);
        
        byte[] serialized = original.serialize();
        
        BlockPlacedEvent deserialized = new BlockPlacedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getX(), deserialized.getX());
        assertEquals(original.getY(), deserialized.getY());
        assertEquals(original.getBlockType(), deserialized.getBlockType());
        assertEquals(original.getPlayerId(), deserialized.getPlayerId());
    }
    
    @Test
    @DisplayName("0,0 위치 테스트")
    void testZeroPosition() {
        BlockPlacedEvent event = new BlockPlacedEvent(0, 0, 1, 1);
        
        assertEquals(0, event.getX());
        assertEquals(0, event.getY());
    }
    
    @Test
    @DisplayName("여러 블록 타입 테스트")
    void testDifferentBlockTypes() {
        for (int blockType = 0; blockType <= 7; blockType++) {
            BlockPlacedEvent event = new BlockPlacedEvent(5, 10, blockType, 1);
            
            assertEquals(blockType, event.getBlockType());
            
            byte[] serialized = event.serialize();
            BlockPlacedEvent deserialized = new BlockPlacedEvent();
            deserialized.deserialize(serialized);
            
            assertEquals(blockType, deserialized.getBlockType());
        }
    }
    
    @Test
    @DisplayName("최대 좌표 테스트")
    void testMaxCoordinates() {
        BlockPlacedEvent event = new BlockPlacedEvent(Integer.MAX_VALUE, Integer.MAX_VALUE, 1, 1);
        
        assertEquals(Integer.MAX_VALUE, event.getX());
        assertEquals(Integer.MAX_VALUE, event.getY());
        
        byte[] serialized = event.serialize();
        BlockPlacedEvent deserialized = new BlockPlacedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(Integer.MAX_VALUE, deserialized.getX());
        assertEquals(Integer.MAX_VALUE, deserialized.getY());
    }
    
    @Test
    @DisplayName("여러 플레이어 테스트")
    void testMultiplePlayers() {
        BlockPlacedEvent event1 = new BlockPlacedEvent(5, 10, 1, 1);
        BlockPlacedEvent event2 = new BlockPlacedEvent(7, 12, 2, 2);
        
        assertEquals(1, event1.getPlayerId());
        assertEquals(2, event2.getPlayerId());
    }
    
    @Test
    @DisplayName("음수 좌표 테스트")
    void testNegativeCoordinates() {
        BlockPlacedEvent event = new BlockPlacedEvent(-5, -10, 1, 1);
        
        assertEquals(-5, event.getX());
        assertEquals(-10, event.getY());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        BlockPlacedEvent event = new BlockPlacedEvent(5, 10, 2, 1);
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("BlockPlacedEvent"));
        assertTrue(str.contains("x=5"));
        assertTrue(str.contains("y=10"));
        assertTrue(str.contains("blockType=2"));
        assertTrue(str.contains("playerId=1"));
    }
}
