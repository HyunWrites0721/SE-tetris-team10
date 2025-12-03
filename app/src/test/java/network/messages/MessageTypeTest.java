package network.messages;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageType enum 테스트
 */
class MessageTypeTest {

    @Test
    void testEnumValues() {
        MessageType[] types = MessageType.values();
        assertEquals(5, types.length, "MessageType은 5개의 값을 가져야 함");
        
        assertNotNull(MessageType.GAME_EVENT);
        assertNotNull(MessageType.CONNECTION);
        assertNotNull(MessageType.GAME_CONTROL);
        assertNotNull(MessageType.HEARTBEAT);
        assertNotNull(MessageType.ATTACK);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(MessageType.GAME_EVENT, MessageType.valueOf("GAME_EVENT"));
        assertEquals(MessageType.CONNECTION, MessageType.valueOf("CONNECTION"));
        assertEquals(MessageType.GAME_CONTROL, MessageType.valueOf("GAME_CONTROL"));
        assertEquals(MessageType.HEARTBEAT, MessageType.valueOf("HEARTBEAT"));
        assertEquals(MessageType.ATTACK, MessageType.valueOf("ATTACK"));
    }

    @Test
    void testEnumValueOf_InvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            MessageType.valueOf("INVALID");
        });
    }

    @Test
    void testEnumEquality() {
        MessageType event1 = MessageType.GAME_EVENT;
        MessageType event2 = MessageType.valueOf("GAME_EVENT");
        
        assertSame(event1, event2);
        assertEquals(event1, event2);
    }

    @Test
    void testEnumToString() {
        assertEquals("GAME_EVENT", MessageType.GAME_EVENT.toString());
        assertEquals("CONNECTION", MessageType.CONNECTION.toString());
        assertEquals("GAME_CONTROL", MessageType.GAME_CONTROL.toString());
        assertEquals("HEARTBEAT", MessageType.HEARTBEAT.toString());
        assertEquals("ATTACK", MessageType.ATTACK.toString());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, MessageType.GAME_EVENT.ordinal());
        assertEquals(1, MessageType.CONNECTION.ordinal());
        assertEquals(2, MessageType.GAME_CONTROL.ordinal());
        assertEquals(3, MessageType.HEARTBEAT.ordinal());
        assertEquals(4, MessageType.ATTACK.ordinal());
    }

    @Test
    void testAllTypesInSwitch() {
        for (MessageType type : MessageType.values()) {
            String description = getTypeDescription(type);
            assertNotNull(description);
            assertFalse(description.isEmpty());
        }
    }

    private String getTypeDescription(MessageType type) {
        switch (type) {
            case GAME_EVENT:
                return "게임 이벤트 메시지";
            case CONNECTION:
                return "연결 관련 메시지";
            case GAME_CONTROL:
                return "게임 제어 메시지";
            case HEARTBEAT:
                return "연결 확인 메시지";
            case ATTACK:
                return "공격 메시지";
            default:
                return "알 수 없는 타입";
        }
    }

    @Test
    void testMessageTypePriority() {
        // 메시지 타입 별 우선순위 테스트 (예시)
        assertTrue(getPriority(MessageType.HEARTBEAT) > getPriority(MessageType.GAME_EVENT));
        assertTrue(getPriority(MessageType.CONNECTION) > getPriority(MessageType.ATTACK));
    }

    private int getPriority(MessageType type) {
        switch (type) {
            case HEARTBEAT:
                return 5;
            case CONNECTION:
                return 4;
            case GAME_CONTROL:
                return 3;
            case ATTACK:
                return 2;
            case GAME_EVENT:
                return 1;
            default:
                return 0;
        }
    }

    @Test
    void testEnumInCollection() {
        java.util.Set<MessageType> types = java.util.EnumSet.allOf(MessageType.class);
        assertEquals(5, types.size());
        
        assertTrue(types.contains(MessageType.GAME_EVENT));
        assertTrue(types.contains(MessageType.CONNECTION));
        assertTrue(types.contains(MessageType.GAME_CONTROL));
        assertTrue(types.contains(MessageType.HEARTBEAT));
        assertTrue(types.contains(MessageType.ATTACK));
    }
}
