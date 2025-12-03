package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TickEvent 테스트")
class TickEventTest {
    
    @Test
    @DisplayName("TickEvent 생성 테스트")
    void testTickEventCreation() {
        TickEvent event = new TickEvent(1, 1, 16);
        
        assertNotNull(event);
        assertEquals(1, event.getCurrentLevel());
        assertEquals(1, event.getSpeedLevel());
        assertEquals(16, event.getDeltaTime());
        assertEquals("TICK", event.getEventType());
    }
    
    @Test
    @DisplayName("높은 레벨 테스트")
    void testHighLevel() {
        TickEvent event = new TickEvent(99, 10, 8);
        
        assertEquals(99, event.getCurrentLevel());
        assertEquals(10, event.getSpeedLevel());
        assertEquals(8, event.getDeltaTime());
    }
    
    @Test
    @DisplayName("다양한 deltaTime 테스트")
    void testDifferentDeltaTimes() {
        long[] deltaTimes = {0, 16, 32, 100, 1000};
        
        for (long deltaTime : deltaTimes) {
            TickEvent event = new TickEvent(1, 1, deltaTime);
            assertEquals(deltaTime, event.getDeltaTime());
        }
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 테스트")
    void testSerializationRoundTrip() {
        TickEvent original = new TickEvent(5, 7, 20);
        
        byte[] serialized = original.serialize();
        
        TickEvent deserialized = new TickEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getCurrentLevel(), deserialized.getCurrentLevel());
        assertEquals(original.getSpeedLevel(), deserialized.getSpeedLevel());
        assertEquals(original.getDeltaTime(), deserialized.getDeltaTime());
    }
    
    @Test
    @DisplayName("직렬화 크기 테스트")
    void testSerializationSize() {
        TickEvent event = new TickEvent(1, 1, 16);
        
        byte[] serialized = event.serialize();
        
        assertEquals(24, serialized.length); // 8 + 4 + 4 + 8
    }
    
    @Test
    @DisplayName("fromBytes 팩토리 메서드 테스트")
    void testFromBytes() {
        TickEvent original = new TickEvent(3, 5, 25);
        
        byte[] serialized = original.serialize();
        TickEvent deserialized = TickEvent.fromBytes(serialized);
        
        assertEquals(original.getCurrentLevel(), deserialized.getCurrentLevel());
        assertEquals(original.getSpeedLevel(), deserialized.getSpeedLevel());
        assertEquals(original.getDeltaTime(), deserialized.getDeltaTime());
    }
    
    @Test
    @DisplayName("기본 생성자 테스트")
    void testDefaultConstructor() {
        TickEvent event = new TickEvent();
        
        assertEquals(1, event.getCurrentLevel());
        assertEquals(1, event.getSpeedLevel());
        assertEquals(0, event.getDeltaTime());
    }
    
    @Test
    @DisplayName("레벨과 속도 레벨 독립성 테스트")
    void testLevelAndSpeedLevelIndependence() {
        TickEvent event1 = new TickEvent(5, 3, 16);
        TickEvent event2 = new TickEvent(3, 5, 16);
        
        assertNotEquals(event1.getCurrentLevel(), event2.getCurrentLevel());
        assertNotEquals(event1.getSpeedLevel(), event2.getSpeedLevel());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        TickEvent event = new TickEvent(5, 7, 20);
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("TickEvent"));
        assertTrue(str.contains("currentLevel=5"));
        assertTrue(str.contains("speedLevel=7"));
        assertTrue(str.contains("deltaTime=20"));
    }
}
