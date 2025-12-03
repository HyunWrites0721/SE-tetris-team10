package game.player;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PlayerId enum 테스트
 */
class PlayerIdTest {

    @Test
    void testEnumValues() {
        PlayerId[] values = PlayerId.values();
        assertEquals(3, values.length, "PlayerId는 3개의 값을 가져야 함");
        
        assertNotNull(PlayerId.PLAYER1);
        assertNotNull(PlayerId.PLAYER2);
        assertNotNull(PlayerId.SPECTATOR);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(PlayerId.PLAYER1, PlayerId.valueOf("PLAYER1"));
        assertEquals(PlayerId.PLAYER2, PlayerId.valueOf("PLAYER2"));
        assertEquals(PlayerId.SPECTATOR, PlayerId.valueOf("SPECTATOR"));
    }

    @Test
    void testEnumValueOf_InvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            PlayerId.valueOf("INVALID");
        });
    }

    @Test
    void testEnumEquality() {
        PlayerId p1 = PlayerId.PLAYER1;
        PlayerId p2 = PlayerId.valueOf("PLAYER1");
        
        assertSame(p1, p2, "같은 enum 값은 동일한 인스턴스여야 함");
        assertEquals(p1, p2);
    }

    @Test
    void testEnumToString() {
        assertEquals("PLAYER1", PlayerId.PLAYER1.toString());
        assertEquals("PLAYER2", PlayerId.PLAYER2.toString());
        assertEquals("SPECTATOR", PlayerId.SPECTATOR.toString());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, PlayerId.PLAYER1.ordinal());
        assertEquals(1, PlayerId.PLAYER2.ordinal());
        assertEquals(2, PlayerId.SPECTATOR.ordinal());
    }
}
