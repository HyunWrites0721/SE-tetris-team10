package game.core;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * GameStateSerializer의 직렬화/역직렬화 동작을 테스트합니다.
 */
public class GameStateSerializerTest {

    @Test
    public void roundtripShouldPreserveNumericAndBoardState() {
        int[][] board = new int[4][4];
        int[][] color = new int[4][4];
        board[1][1] = 7;
        color[1][1] = 2;

        GameState original = new GameState.Builder(board, color, null, null, true)
                .score(42)
                .totalLinesCleared(3)
                .currentLevel(4)
                .lineClearCount(2)
                .itemGenerateCount(1)
                .blocksSpawned(10)
                .isAnimating(true)
                .lastLineClearScore(100)
                .build();

        String json = GameStateSerializer.toJson(original);
        assertNotNull(json);

        GameState restored = GameStateSerializer.fromJson(json);
        assertNotNull(restored);

        assertEquals(original.getScore(), restored.getScore());
        assertEquals(original.getCurrentLevel(), restored.getCurrentLevel());
        assertEquals(original.getTotalLinesCleared(), restored.getTotalLinesCleared());
        assertEquals(original.getLineClearCount(), restored.getLineClearCount());
        assertEquals(original.getItemGenerateCount(), restored.getItemGenerateCount());
        assertEquals(original.getBlocksSpawned(), restored.getBlocksSpawned());
        assertEquals(original.isItemMode(), restored.isItemMode());
        assertEquals(original.isAnimating(), restored.isAnimating());
        assertEquals(original.getLastLineClearScore(), restored.getLastLineClearScore());

        assertTrue(Arrays.deepEquals(original.getBoardArray(), restored.getBoardArray()));
        assertTrue(Arrays.deepEquals(original.getColorBoard(), restored.getColorBoard()));
    }
}
