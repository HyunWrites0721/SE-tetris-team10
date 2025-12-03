package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LineClearedEvent 테스트")
class LineClearedEventTest {
    
    @Test
    @DisplayName("LineClearedEvent 생성 테스트")
    void testLineClearedEventCreation() {
        int[] clearedLines = {5, 10, 15};
        LineClearedEvent event = new LineClearedEvent(clearedLines, 400, 1);
        
        assertNotNull(event);
        assertArrayEquals(clearedLines, event.getClearedLines());
        assertEquals(400, event.getScore());
        assertEquals(1, event.getPlayerId());
        assertEquals("LINE_CLEARED", event.getEventType());
    }
    
    @Test
    @DisplayName("블록 패턴 포함 생성 테스트")
    void testLineClearedEventWithBlockPattern() {
        int[] clearedLines = {10};
        int[][] blockPattern = {{1, 1}, {1, 1}};
        LineClearedEvent event = new LineClearedEvent(clearedLines, 100, 1, blockPattern, 5);
        
        assertNotNull(event.getLastBlockPattern());
        assertEquals(2, event.getLastBlockPattern().length);
        assertEquals(5, event.getLastBlockX());
    }
    
    @Test
    @DisplayName("null 배열 방어적 복사 테스트")
    void testNullArrayDefensiveCopy() {
        LineClearedEvent event = new LineClearedEvent(null, 100, 1);
        
        assertNotNull(event.getClearedLines());
        assertEquals(0, event.getClearedLines().length);
    }
    
    @Test
    @DisplayName("배열 불변성 테스트")
    void testArrayImmutability() {
        int[] clearedLines = {5, 10};
        LineClearedEvent event = new LineClearedEvent(clearedLines, 200, 1);
        
        // 원본 배열 수정
        clearedLines[0] = 999;
        
        // 이벤트의 배열은 변경되지 않음
        assertEquals(5, event.getClearedLines()[0]);
    }
    
    @Test
    @DisplayName("블록 패턴 불변성 테스트")
    void testBlockPatternImmutability() {
        int[][] blockPattern = {{1, 1}, {1, 1}};
        LineClearedEvent event = new LineClearedEvent(new int[]{10}, 100, 1, blockPattern, 5);
        
        // 원본 패턴 수정
        blockPattern[0][0] = 999;
        
        // 이벤트의 패턴은 변경되지 않음
        assertEquals(1, event.getLastBlockPattern()[0][0]);
    }
    
    @Test
    @DisplayName("직렬화 테스트 - 기본")
    void testSerialization() {
        int[] clearedLines = {5, 10};
        LineClearedEvent event = new LineClearedEvent(clearedLines, 200, 1);
        
        byte[] serialized = event.serialize();
        
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }
    
    @Test
    @DisplayName("역직렬화 테스트")
    void testDeserialization() {
        int[] clearedLines = {3, 7, 11};
        LineClearedEvent original = new LineClearedEvent(clearedLines, 300, 2);
        
        byte[] serialized = original.serialize();
        
        LineClearedEvent deserialized = new LineClearedEvent();
        deserialized.deserialize(serialized);
        
        assertArrayEquals(clearedLines, deserialized.getClearedLines());
        assertEquals(300, deserialized.getScore());
        assertEquals(2, deserialized.getPlayerId());
    }
    
    @Test
    @DisplayName("블록 패턴 직렬화/역직렬화 테스트")
    void testBlockPatternSerialization() {
        int[] clearedLines = {10};
        int[][] blockPattern = {{1, 0}, {1, 1}};
        LineClearedEvent original = new LineClearedEvent(clearedLines, 100, 1, blockPattern, 7);
        
        byte[] serialized = original.serialize();
        
        LineClearedEvent deserialized = new LineClearedEvent();
        deserialized.deserialize(serialized);
        
        assertArrayEquals(clearedLines, deserialized.getClearedLines());
        assertEquals(100, deserialized.getScore());
        assertEquals(1, deserialized.getPlayerId());
        assertEquals(7, deserialized.getLastBlockX());
        
        int[][] deserializedPattern = deserialized.getLastBlockPattern();
        assertNotNull(deserializedPattern);
        assertEquals(2, deserializedPattern.length);
        assertEquals(2, deserializedPattern[0].length);
    }
    
    @Test
    @DisplayName("null 블록 패턴 직렬화 테스트")
    void testNullBlockPatternSerialization() {
        int[] clearedLines = {5};
        LineClearedEvent original = new LineClearedEvent(clearedLines, 100, 1, null, 0);
        
        byte[] serialized = original.serialize();
        
        LineClearedEvent deserialized = new LineClearedEvent();
        deserialized.deserialize(serialized);
        
        assertNull(deserialized.getLastBlockPattern());
        assertEquals(0, deserialized.getLastBlockX());
    }
    
    @Test
    @DisplayName("빈 clearedLines 배열 테스트")
    void testEmptyClearedLines() {
        int[] clearedLines = {};
        LineClearedEvent event = new LineClearedEvent(clearedLines, 0, 1);
        
        assertEquals(0, event.getClearedLines().length);
        
        byte[] serialized = event.serialize();
        LineClearedEvent deserialized = new LineClearedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(0, deserialized.getClearedLines().length);
    }
    
    @Test
    @DisplayName("여러 라인 클리어 테스트")
    void testMultipleLineClears() {
        int[] clearedLines = {1, 2, 3, 4};
        LineClearedEvent event = new LineClearedEvent(clearedLines, 800, 1);
        
        assertEquals(4, event.getClearedLines().length);
        assertArrayEquals(new int[]{1, 2, 3, 4}, event.getClearedLines());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        int[] clearedLines = {5, 10};
        LineClearedEvent event = new LineClearedEvent(clearedLines, 200, 1);
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("LineClearedEvent"));
        assertTrue(str.contains("200"));
        assertTrue(str.contains("playerId=1"));
    }
    
    @Test
    @DisplayName("기본 생성자 테스트")
    void testDefaultConstructor() {
        LineClearedEvent event = new LineClearedEvent();
        
        assertNotNull(event);
        assertEquals("LINE_CLEARED", event.getEventType());
    }
}
