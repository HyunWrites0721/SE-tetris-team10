package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AttackEvent 테스트")
class AttackEventTest {
    
    @Test
    @DisplayName("AttackEvent 생성 테스트 (패턴 없음)")
    void testAttackEventCreation() {
        AttackEvent event = new AttackEvent(2, 1);
        
        assertNotNull(event);
        assertEquals(2, event.getAttackLines());
        assertEquals(1, event.getPlayerId());
        assertNull(event.getBlockPattern());
        assertEquals(0, event.getBlockX());
        assertEquals("ATTACK", event.getEventType());
    }
    
    @Test
    @DisplayName("AttackEvent 생성 테스트 (패턴 포함)")
    void testAttackEventWithPattern() {
        int[][] pattern = {{1, 0, 1}, {0, 1, 0}};
        AttackEvent event = new AttackEvent(2, 1, pattern, 3);
        
        assertNotNull(event);
        assertEquals(2, event.getAttackLines());
        assertEquals(1, event.getPlayerId());
        assertEquals(3, event.getBlockX());
        
        int[][] retrievedPattern = event.getBlockPattern();
        assertNotNull(retrievedPattern);
        assertEquals(2, retrievedPattern.length);
        assertEquals(3, retrievedPattern[0].length);
        assertArrayEquals(pattern[0], retrievedPattern[0]);
        assertArrayEquals(pattern[1], retrievedPattern[1]);
    }
    
    @Test
    @DisplayName("방어적 복사 테스트")
    void testDefensiveCopy() {
        int[][] pattern = {{1, 0}, {0, 1}};
        AttackEvent event = new AttackEvent(2, 1, pattern, 2);
        
        // 원본 배열 수정
        pattern[0][0] = 99;
        
        // 이벤트에 저장된 패턴은 영향받지 않아야 함
        int[][] retrievedPattern = event.getBlockPattern();
        assertEquals(1, retrievedPattern[0][0]);
        
        // 반환된 배열 수정
        retrievedPattern[0][0] = 88;
        
        // 이벤트 내부 데이터는 영향받지 않아야 함
        int[][] retrievedPattern2 = event.getBlockPattern();
        assertEquals(1, retrievedPattern2[0][0]);
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 테스트 (패턴 없음)")
    void testSerializationWithoutPattern() {
        AttackEvent original = new AttackEvent(3, 2);
        
        byte[] serialized = original.serialize();
        
        AttackEvent deserialized = new AttackEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getAttackLines(), deserialized.getAttackLines());
        assertEquals(original.getPlayerId(), deserialized.getPlayerId());
        assertNull(deserialized.getBlockPattern());
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 테스트 (패턴 포함)")
    void testSerializationWithPattern() {
        int[][] pattern = {{1, 0}, {0, 1}};
        AttackEvent original = new AttackEvent(2, 1, pattern, 3);
        
        byte[] serialized = original.serialize();
        
        AttackEvent deserialized = new AttackEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getAttackLines(), deserialized.getAttackLines());
        assertEquals(original.getPlayerId(), deserialized.getPlayerId());
        assertEquals(original.getBlockX(), deserialized.getBlockX());
        
        int[][] deserializedPattern = deserialized.getBlockPattern();
        assertNotNull(deserializedPattern);
        assertEquals(2, deserializedPattern.length);
        assertEquals(2, deserializedPattern[0].length);
        
        for (int i = 0; i < pattern.length; i++) {
            assertArrayEquals(pattern[i], deserializedPattern[i]);
        }
    }
    
    @Test
    @DisplayName("여러 공격 라인 수 테스트")
    void testDifferentAttackLines() {
        for (int lines = 1; lines <= 4; lines++) {
            AttackEvent event = new AttackEvent(lines, 1);
            assertEquals(lines, event.getAttackLines());
        }
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        int[][] pattern = {{1, 0}, {0, 1}};
        AttackEvent event = new AttackEvent(2, 1, pattern, 3);
        
        String str = event.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("AttackEvent"));
        assertTrue(str.contains("playerId=1"));
        assertTrue(str.contains("attackLines=2"));
        assertTrue(str.contains("blockX=3"));
        assertTrue(str.contains("2x2"));
    }
}
