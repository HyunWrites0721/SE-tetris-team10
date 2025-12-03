package game.player;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GameCommand 클래스 테스트
 */
class GameCommandTest {

    private long testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = System.currentTimeMillis();
    }

    @Test
    void testConstructor_WithTypeOnly() {
        GameCommand cmd = new GameCommand(GameCommand.Type.LEFT);
        
        assertEquals(GameCommand.Type.LEFT, cmd.getType());
        assertTrue(cmd.getTimestamp() > 0);
        assertTrue(cmd.getTimestamp() <= System.currentTimeMillis());
    }

    @Test
    void testConstructor_WithTypeAndTimestamp() {
        GameCommand cmd = new GameCommand(GameCommand.Type.RIGHT, testTimestamp);
        
        assertEquals(GameCommand.Type.RIGHT, cmd.getType());
        assertEquals(testTimestamp, cmd.getTimestamp());
    }

    @Test
    void testConstructor_NullType_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            new GameCommand(null);
        });
    }

    @Test
    void testAllCommandTypes() {
        GameCommand.Type[] types = {
            GameCommand.Type.LEFT,
            GameCommand.Type.RIGHT,
            GameCommand.Type.ROTATE,
            GameCommand.Type.SOFT_DROP,
            GameCommand.Type.HARD_DROP,
            GameCommand.Type.PAUSE,
            GameCommand.Type.RESET
        };

        for (GameCommand.Type type : types) {
            GameCommand cmd = new GameCommand(type);
            assertEquals(type, cmd.getType());
        }
    }

    @Test
    void testGetType() {
        GameCommand cmd = new GameCommand(GameCommand.Type.ROTATE);
        assertEquals(GameCommand.Type.ROTATE, cmd.getType());
    }

    @Test
    void testGetTimestamp() {
        long before = System.currentTimeMillis();
        GameCommand cmd = new GameCommand(GameCommand.Type.HARD_DROP);
        long after = System.currentTimeMillis();
        
        assertTrue(cmd.getTimestamp() >= before);
        assertTrue(cmd.getTimestamp() <= after);
    }

    @Test
    void testToString() {
        GameCommand cmd = new GameCommand(GameCommand.Type.LEFT, testTimestamp);
        String str = cmd.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("LEFT"));
        assertTrue(str.contains(String.valueOf(testTimestamp)));
        assertTrue(str.contains("GameCommand"));
    }

    @Test
    void testToString_AllTypes() {
        for (GameCommand.Type type : GameCommand.Type.values()) {
            GameCommand cmd = new GameCommand(type);
            String str = cmd.toString();
            
            assertTrue(str.contains(type.name()));
        }
    }

    @Test
    void testCommandImmutability() {
        GameCommand cmd = new GameCommand(GameCommand.Type.SOFT_DROP, testTimestamp);
        
        // 값이 변경되지 않는지 확인
        assertEquals(GameCommand.Type.SOFT_DROP, cmd.getType());
        assertEquals(testTimestamp, cmd.getTimestamp());
        
        // 동일한 값으로 다시 호출해도 같은 값 반환
        assertEquals(GameCommand.Type.SOFT_DROP, cmd.getType());
        assertEquals(testTimestamp, cmd.getTimestamp());
    }

    @Test
    void testDifferentCommands_DifferentTimestamps() throws InterruptedException {
        GameCommand cmd1 = new GameCommand(GameCommand.Type.LEFT);
        Thread.sleep(2); // 타임스탬프 차이를 보장
        GameCommand cmd2 = new GameCommand(GameCommand.Type.RIGHT);
        
        assertTrue(cmd2.getTimestamp() >= cmd1.getTimestamp());
    }

    @Test
    void testCommandType_EnumValues() {
        GameCommand.Type[] types = GameCommand.Type.values();
        
        assertEquals(7, types.length, "7개의 명령 타입이 존재해야 함");
        
        // 모든 타입 확인
        assertNotNull(GameCommand.Type.valueOf("LEFT"));
        assertNotNull(GameCommand.Type.valueOf("RIGHT"));
        assertNotNull(GameCommand.Type.valueOf("ROTATE"));
        assertNotNull(GameCommand.Type.valueOf("SOFT_DROP"));
        assertNotNull(GameCommand.Type.valueOf("HARD_DROP"));
        assertNotNull(GameCommand.Type.valueOf("PAUSE"));
        assertNotNull(GameCommand.Type.valueOf("RESET"));
    }
}
