package game.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AttackAppliedEvent 테스트")
class AttackAppliedEventTest {
    
    @Test
    @DisplayName("AttackAppliedEvent 생성 테스트")
    void testAttackAppliedEventCreation() {
        int[][] pattern = {{1, 0, 1}, {0, 1, 0}};
        AttackAppliedEvent event = new AttackAppliedEvent(2, pattern, 3);
        
        assertNotNull(event);
        assertEquals(2, event.getAttackLines());
        assertEquals(3, event.getBlockX());
        assertEquals("ATTACK_APPLIED", event.getEventType());
        
        int[][] retrievedPattern = event.getBlockPattern();
        assertNotNull(retrievedPattern);
        assertEquals(2, retrievedPattern.length);
        assertEquals(3, retrievedPattern[0].length);
    }
    
    @Test
    @DisplayName("방어적 복사 테스트")
    void testDefensiveCopy() {
        int[][] pattern = {{1, 0}, {0, 1}};
        AttackAppliedEvent event = new AttackAppliedEvent(2, pattern, 2);
        
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
    @DisplayName("직렬화/역직렬화 테스트 (패턴 있음)")
    void testSerializationWithPattern() {
        int[][] pattern = {{1, 0, 1, 0}, {0, 1, 0, 1}, {1, 1, 0, 0}};
        AttackAppliedEvent original = new AttackAppliedEvent(3, pattern, 5);
        
        byte[] serialized = original.serialize();
        
        AttackAppliedEvent deserialized = new AttackAppliedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getAttackLines(), deserialized.getAttackLines());
        assertEquals(original.getBlockX(), deserialized.getBlockX());
        
        int[][] deserializedPattern = deserialized.getBlockPattern();
        assertNotNull(deserializedPattern);
        assertEquals(3, deserializedPattern.length);
        assertEquals(4, deserializedPattern[0].length);
        
        for (int i = 0; i < pattern.length; i++) {
            assertArrayEquals(pattern[i], deserializedPattern[i]);
        }
    }
    
    @Test
    @DisplayName("직렬화/역직렬화 테스트 (null 패턴)")
    void testSerializationWithNullPattern() {
        AttackAppliedEvent original = new AttackAppliedEvent(2, null, 0);
        
        byte[] serialized = original.serialize();
        
        AttackAppliedEvent deserialized = new AttackAppliedEvent();
        deserialized.deserialize(serialized);
        
        assertEquals(original.getAttackLines(), deserialized.getAttackLines());
        assertNull(deserialized.getBlockPattern());
    }
    
    @Test
    @DisplayName("큰 패턴 테스트")
    void testLargePattern() {
        int[][] largePattern = new int[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                largePattern[i][j] = (i + j) % 2;
            }
        }
        
        AttackAppliedEvent event = new AttackAppliedEvent(10, largePattern, 0);
        
        byte[] serialized = event.serialize();
        AttackAppliedEvent deserialized = new AttackAppliedEvent();
        deserialized.deserialize(serialized);
        
        int[][] deserializedPattern = deserialized.getBlockPattern();
        assertEquals(10, deserializedPattern.length);
        assertEquals(10, deserializedPattern[0].length);
        
        for (int i = 0; i < 10; i++) {
            assertArrayEquals(largePattern[i], deserializedPattern[i]);
        }
    }
    
    @Test
    @DisplayName("여러 공격 라인 수 테스트")
    void testDifferentAttackLines() {
        int[][] pattern = {{1, 0}, {0, 1}};
        
        for (int lines = 1; lines <= 4; lines++) {
            AttackAppliedEvent event = new AttackAppliedEvent(lines, pattern, 0);
            assertEquals(lines, event.getAttackLines());
        }
    }
}
