package versus;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * VersusMode enum 테스트
 */
class VersusModeTest {

    @Test
    void testEnumValues() {
        VersusMode[] modes = VersusMode.values();
        assertEquals(3, modes.length, "VersusMode는 3개의 값을 가져야 함");
        
        assertNotNull(VersusMode.NORMAL);
        assertNotNull(VersusMode.ITEM);
        assertNotNull(VersusMode.TIME_LIMIT);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(VersusMode.NORMAL, VersusMode.valueOf("NORMAL"));
        assertEquals(VersusMode.ITEM, VersusMode.valueOf("ITEM"));
        assertEquals(VersusMode.TIME_LIMIT, VersusMode.valueOf("TIME_LIMIT"));
    }

    @Test
    void testEnumValueOf_InvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            VersusMode.valueOf("INVALID");
        });
    }

    @Test
    void testEnumEquality() {
        VersusMode normal1 = VersusMode.NORMAL;
        VersusMode normal2 = VersusMode.valueOf("NORMAL");
        
        assertSame(normal1, normal2, "같은 enum 값은 동일한 인스턴스여야 함");
        assertEquals(normal1, normal2);
    }

    @Test
    void testEnumToString() {
        assertEquals("NORMAL", VersusMode.NORMAL.toString());
        assertEquals("ITEM", VersusMode.ITEM.toString());
        assertEquals("TIME_LIMIT", VersusMode.TIME_LIMIT.toString());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, VersusMode.NORMAL.ordinal());
        assertEquals(1, VersusMode.ITEM.ordinal());
        assertEquals(2, VersusMode.TIME_LIMIT.ordinal());
    }

    @Test
    void testEnumInSwitchStatement() {
        VersusMode mode = VersusMode.NORMAL;
        String result = "";
        
        switch (mode) {
            case NORMAL:
                result = "일반 모드";
                break;
            case ITEM:
                result = "아이템 모드";
                break;
            case TIME_LIMIT:
                result = "시간 제한 모드";
                break;
            default:
                fail("알 수 없는 모드");
        }
        
        assertEquals("일반 모드", result);
    }

    @Test
    void testAllModesInSwitch() {
        for (VersusMode mode : VersusMode.values()) {
            String result = getModeDescription(mode);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    private String getModeDescription(VersusMode mode) {
        switch (mode) {
            case NORMAL:
                return "일반 대전 모드";
            case ITEM:
                return "아이템을 사용하는 대전 모드";
            case TIME_LIMIT:
                return "시간 제한이 있는 대전 모드";
            default:
                return "알 수 없는 모드";
        }
    }

    @Test
    void testEnumComparison() {
        assertTrue(VersusMode.NORMAL.ordinal() < VersusMode.ITEM.ordinal());
        assertTrue(VersusMode.ITEM.ordinal() < VersusMode.TIME_LIMIT.ordinal());
    }

    @Test
    void testEnumIteration() {
        int count = 0;
        for (VersusMode mode : VersusMode.values()) {
            assertNotNull(mode);
            count++;
        }
        assertEquals(3, count);
    }
}
