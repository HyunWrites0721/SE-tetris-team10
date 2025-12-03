package network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ConnectionState enum 테스트
 */
class ConnectionStateTest {

    @Test
    void testEnumValues() {
        ConnectionState[] states = ConnectionState.values();
        assertEquals(5, states.length, "ConnectionState는 5개의 값을 가져야 함");
        
        assertNotNull(ConnectionState.DISCONNECTED);
        assertNotNull(ConnectionState.CONNECTING);
        assertNotNull(ConnectionState.CONNECTED);
        assertNotNull(ConnectionState.LAGGING);
        assertNotNull(ConnectionState.TIMEOUT);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(ConnectionState.DISCONNECTED, ConnectionState.valueOf("DISCONNECTED"));
        assertEquals(ConnectionState.CONNECTING, ConnectionState.valueOf("CONNECTING"));
        assertEquals(ConnectionState.CONNECTED, ConnectionState.valueOf("CONNECTED"));
        assertEquals(ConnectionState.LAGGING, ConnectionState.valueOf("LAGGING"));
        assertEquals(ConnectionState.TIMEOUT, ConnectionState.valueOf("TIMEOUT"));
    }

    @Test
    void testEnumValueOf_InvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            ConnectionState.valueOf("INVALID");
        });
    }

    @Test
    void testEnumEquality() {
        ConnectionState connected1 = ConnectionState.CONNECTED;
        ConnectionState connected2 = ConnectionState.valueOf("CONNECTED");
        
        assertSame(connected1, connected2);
        assertEquals(connected1, connected2);
    }

    @Test
    void testEnumToString() {
        assertEquals("DISCONNECTED", ConnectionState.DISCONNECTED.toString());
        assertEquals("CONNECTING", ConnectionState.CONNECTING.toString());
        assertEquals("CONNECTED", ConnectionState.CONNECTED.toString());
        assertEquals("LAGGING", ConnectionState.LAGGING.toString());
        assertEquals("TIMEOUT", ConnectionState.TIMEOUT.toString());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, ConnectionState.DISCONNECTED.ordinal());
        assertEquals(1, ConnectionState.CONNECTING.ordinal());
        assertEquals(2, ConnectionState.CONNECTED.ordinal());
        assertEquals(3, ConnectionState.LAGGING.ordinal());
        assertEquals(4, ConnectionState.TIMEOUT.ordinal());
    }

    @Test
    void testStateTransitions() {
        // 정상적인 연결 상태 전이 시뮬레이션
        ConnectionState current = ConnectionState.DISCONNECTED;
        assertEquals(ConnectionState.DISCONNECTED, current);
        
        current = ConnectionState.CONNECTING;
        assertEquals(ConnectionState.CONNECTING, current);
        
        current = ConnectionState.CONNECTED;
        assertEquals(ConnectionState.CONNECTED, current);
    }

    @Test
    void testIsConnected() {
        assertTrue(isConnectedState(ConnectionState.CONNECTED));
        assertFalse(isConnectedState(ConnectionState.DISCONNECTED));
        assertFalse(isConnectedState(ConnectionState.CONNECTING));
        assertFalse(isConnectedState(ConnectionState.TIMEOUT));
    }

    @Test
    void testIsDisconnected() {
        assertTrue(isDisconnectedState(ConnectionState.DISCONNECTED));
        assertTrue(isDisconnectedState(ConnectionState.TIMEOUT));
        assertFalse(isDisconnectedState(ConnectionState.CONNECTED));
        assertFalse(isDisconnectedState(ConnectionState.LAGGING));
    }

    private boolean isConnectedState(ConnectionState state) {
        return state == ConnectionState.CONNECTED || state == ConnectionState.LAGGING;
    }

    private boolean isDisconnectedState(ConnectionState state) {
        return state == ConnectionState.DISCONNECTED || state == ConnectionState.TIMEOUT;
    }

    @Test
    void testAllStatesInSwitch() {
        for (ConnectionState state : ConnectionState.values()) {
            String description = getStateDescription(state);
            assertNotNull(description);
            assertFalse(description.isEmpty());
        }
    }

    private String getStateDescription(ConnectionState state) {
        switch (state) {
            case DISCONNECTED:
                return "연결 안됨";
            case CONNECTING:
                return "연결 중";
            case CONNECTED:
                return "정상 연결";
            case LAGGING:
                return "지연 중";
            case TIMEOUT:
                return "타임아웃";
            default:
                return "알 수 없음";
        }
    }
}
