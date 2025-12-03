package network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * NetworkRole enum 테스트
 */
class NetworkRoleTest {

    @Test
    void testEnumValues() {
        NetworkRole[] roles = NetworkRole.values();
        assertEquals(2, roles.length, "NetworkRole은 2개의 값을 가져야 함");
        
        assertNotNull(NetworkRole.SERVER);
        assertNotNull(NetworkRole.CLIENT);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(NetworkRole.SERVER, NetworkRole.valueOf("SERVER"));
        assertEquals(NetworkRole.CLIENT, NetworkRole.valueOf("CLIENT"));
    }

    @Test
    void testEnumValueOf_InvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            NetworkRole.valueOf("INVALID");
        });
    }

    @Test
    void testEnumEquality() {
        NetworkRole server1 = NetworkRole.SERVER;
        NetworkRole server2 = NetworkRole.valueOf("SERVER");
        
        assertSame(server1, server2, "같은 enum 값은 동일한 인스턴스여야 함");
        assertEquals(server1, server2);
    }

    @Test
    void testEnumToString() {
        assertEquals("SERVER", NetworkRole.SERVER.toString());
        assertEquals("CLIENT", NetworkRole.CLIENT.toString());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, NetworkRole.SERVER.ordinal());
        assertEquals(1, NetworkRole.CLIENT.ordinal());
    }

    @Test
    void testEnumInSwitchStatement() {
        NetworkRole role = NetworkRole.SERVER;
        String result = "";
        
        switch (role) {
            case SERVER:
                result = "서버 역할";
                break;
            case CLIENT:
                result = "클라이언트 역할";
                break;
            default:
                fail("알 수 없는 역할");
        }
        
        assertEquals("서버 역할", result);
    }

    @Test
    void testAllRolesInSwitch() {
        for (NetworkRole role : NetworkRole.values()) {
            String description = getRoleDescription(role);
            assertNotNull(description);
            assertFalse(description.isEmpty());
        }
    }

    private String getRoleDescription(NetworkRole role) {
        switch (role) {
            case SERVER:
                return "호스트 역할";
            case CLIENT:
                return "접속자 역할";
            default:
                return "알 수 없음";
        }
    }

    @Test
    void testEnumComparison() {
        assertTrue(NetworkRole.SERVER.ordinal() < NetworkRole.CLIENT.ordinal());
    }
}
