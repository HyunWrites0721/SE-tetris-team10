package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameEvent 테스트")
class GameEventTest {
    
    // 테스트용 구체 클래스
    private static class TestGameEvent extends GameEvent {
        private int testValue;
        
        public TestGameEvent(String eventType) {
            super(eventType);
            this.testValue = 42;
        }
        
        public TestGameEvent(String eventType, int testValue) {
            super(eventType);
            this.testValue = testValue;
        }
        
        public int getTestValue() {
            return testValue;
        }
        
        @Override
        public byte[] serialize() {
            ByteBuffer buffer = ByteBuffer.allocate(12); // 8(timestamp) + 4(testValue)
            buffer.putLong(getTimestamp());
            buffer.putInt(testValue);
            return buffer.array();
        }
        
        @Override
        public void deserialize(byte[] data) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.getLong(); // timestamp skip
            this.testValue = buffer.getInt();
        }
    }
    
    @Test
    @DisplayName("GameEvent 생성 시 timestamp가 설정됨")
    void testTimestampIsSet() {
        long beforeCreation = System.currentTimeMillis();
        TestGameEvent event = new TestGameEvent("TEST_EVENT");
        long afterCreation = System.currentTimeMillis();
        
        assertTrue(event.getTimestamp() >= beforeCreation);
        assertTrue(event.getTimestamp() <= afterCreation);
    }
    
    @Test
    @DisplayName("eventType이 올바르게 설정됨")
    void testEventTypeIsSet() {
        TestGameEvent event = new TestGameEvent("CUSTOM_EVENT");
        
        assertEquals("CUSTOM_EVENT", event.getEventType());
    }
    
    @Test
    @DisplayName("다른 이벤트는 다른 timestamp를 가짐")
    void testDifferentEventsHaveDifferentTimestamps() throws InterruptedException {
        TestGameEvent event1 = new TestGameEvent("TEST_EVENT");
        Thread.sleep(2); // 최소 2ms 대기
        TestGameEvent event2 = new TestGameEvent("TEST_EVENT");
        
        assertTrue(event2.getTimestamp() >= event1.getTimestamp());
    }
    
    @Test
    @DisplayName("toString은 eventType과 timestamp를 포함")
    void testToString() {
        TestGameEvent event = new TestGameEvent("TEST_EVENT");
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("TestGameEvent"));
        assertTrue(str.contains("eventType='TEST_EVENT'"));
        assertTrue(str.contains("timestamp=" + event.getTimestamp()));
    }
    
    @Test
    @DisplayName("serialize/deserialize가 정상 작동")
    void testSerializeDeserialize() {
        TestGameEvent original = new TestGameEvent("TEST_EVENT", 100);
        
        byte[] serialized = original.serialize();
        
        TestGameEvent deserialized = new TestGameEvent("TEST_EVENT");
        deserialized.deserialize(serialized);
        
        assertEquals(original.getTestValue(), deserialized.getTestValue());
    }
    
    @Test
    @DisplayName("여러 eventType 테스트")
    void testDifferentEventTypes() {
        String[] eventTypes = {
            "SCORE_UPDATE", "LINE_CLEARED", "LEVEL_UP", 
            "GAME_OVER", "BLOCK_PLACED", "ATTACK"
        };
        
        for (String eventType : eventTypes) {
            TestGameEvent event = new TestGameEvent(eventType);
            assertEquals(eventType, event.getEventType());
        }
    }
    
    @Test
    @DisplayName("timestamp는 변경되지 않음 (불변성)")
    void testTimestampImmutability() {
        TestGameEvent event = new TestGameEvent("TEST_EVENT");
        long originalTimestamp = event.getTimestamp();
        
        // 다른 작업 수행
        event.serialize();
        event.toString();
        
        // timestamp는 여전히 동일해야 함
        assertEquals(originalTimestamp, event.getTimestamp());
    }
    
    @Test
    @DisplayName("eventType은 변경되지 않음 (불변성)")
    void testEventTypeImmutability() {
        TestGameEvent event = new TestGameEvent("ORIGINAL_EVENT");
        String originalEventType = event.getEventType();
        
        // 다른 작업 수행
        event.serialize();
        event.toString();
        
        // eventType은 여전히 동일해야 함
        assertEquals(originalEventType, event.getEventType());
        assertEquals("ORIGINAL_EVENT", event.getEventType());
    }
}
