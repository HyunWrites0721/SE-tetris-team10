package game.core;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * GameStateSerializer의 직렬화/역직렬화 동작을 테스트합니다.
 */
@DisplayName("GameStateSerializer 테스트")
public class GameStateSerializerTest {

    @Test
    @DisplayName("기본 직렬화/역직렬화 테스트")
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
    
    @Test
    @DisplayName("애니메이션 상태 직렬화 - 라인 클리어")
    public void testLineClearAnimationSerialization() {
        int[][] board = new int[23][12];
        int[][] color = new int[23][12];
        List<Integer> flashingRows = Arrays.asList(5, 10, 15);
        
        GameState original = new GameState.Builder(board, color, null, null, false)
                .lineClearAnimating(true)
                .flashBlack(true)
                .flashingRows(flashingRows)
                .build();
        
        String json = GameStateSerializer.toJson(original);
        GameState restored = GameStateSerializer.fromJson(json);
        
        assertTrue(restored.isLineClearAnimating());
        assertTrue(restored.isFlashBlack());
        assertEquals(3, restored.getFlashingRows().size());
        assertTrue(restored.isRowFlashing(5));
        assertTrue(restored.isRowFlashing(10));
        assertTrue(restored.isRowFlashing(15));
    }
    
    @Test
    @DisplayName("애니메이션 상태 직렬화 - AllClear")
    public void testAllClearAnimationSerialization() {
        int[][] board = new int[23][12];
        int[][] color = new int[23][12];
        
        GameState original = new GameState.Builder(board, color, null, null, false)
                .allClearAnimating(true)
                .allClearFlashBlack(true)
                .build();
        
        String json = GameStateSerializer.toJson(original);
        GameState restored = GameStateSerializer.fromJson(json);
        
        assertTrue(restored.isAllClearAnimating());
        assertTrue(restored.isAllClearFlashBlack());
    }
    
    @Test
    @DisplayName("애니메이션 상태 직렬화 - BoxClear")
    public void testBoxClearAnimationSerialization() {
        int[][] board = new int[23][12];
        int[][] color = new int[23][12];
        List<int[]> centers = Arrays.asList(new int[]{5, 5}, new int[]{10, 10});
        
        GameState original = new GameState.Builder(board, color, null, null, false)
                .boxClearAnimating(true)
                .boxFlashBlack(true)
                .boxFlashCenters(centers)
                .build();
        
        String json = GameStateSerializer.toJson(original);
        GameState restored = GameStateSerializer.fromJson(json);
        
        assertTrue(restored.isBoxClearAnimating());
        assertTrue(restored.isBoxFlashBlack());
        assertEquals(2, restored.getBoxFlashCenters().size());
    }
    
    @Test
    @DisplayName("애니메이션 상태 직렬화 - Weight")
    public void testWeightAnimationSerialization() {
        int[][] board = new int[23][12];
        int[][] color = new int[23][12];
        
        GameState original = new GameState.Builder(board, color, null, null, false)
                .weightAnimating(true)
                .build();
        
        String json = GameStateSerializer.toJson(original);
        GameState restored = GameStateSerializer.fromJson(json);
        
        assertTrue(restored.isWeightAnimating());
    }
    
    @Test
    @DisplayName("null Block 직렬화")
    public void testNullBlocksSerialization() {
        int[][] board = new int[23][12];
        int[][] color = new int[23][12];
        
        GameState original = new GameState.Builder(board, color, null, null, false)
                .score(100)
                .build();
        
        String json = GameStateSerializer.toJson(original);
        GameState restored = GameStateSerializer.fromJson(json);
        
        assertNull(restored.getCurrentBlock());
        assertNull(restored.getNextBlock());
        assertEquals(100, restored.getScore());
    }
    
    @Test
    @DisplayName("큰 보드 직렬화")
    public void testLargeBoardSerialization() {
        int[][] board = new int[23][12];
        int[][] color = new int[23][12];
        
        // 보드 전체에 데이터 채우기
        for (int i = 0; i < 23; i++) {
            for (int j = 0; j < 12; j++) {
                board[i][j] = (i + j) % 11;
                color[i][j] = 0xFF0000 + (i * 1000) + j;
            }
        }
        
        GameState original = new GameState.Builder(board, color, null, null, false).build();
        
        String json = GameStateSerializer.toJson(original);
        GameState restored = GameStateSerializer.fromJson(json);
        
        assertTrue(Arrays.deepEquals(original.getBoardArray(), restored.getBoardArray()));
        assertTrue(Arrays.deepEquals(original.getColorBoard(), restored.getColorBoard()));
    }
    
    @Test
    @DisplayName("최대값 직렬화")
    public void testMaxValuesSerialization() {
        int[][] board = new int[23][12];
        int[][] color = new int[23][12];
        
        GameState original = new GameState.Builder(board, color, null, null, false)
                .score(Integer.MAX_VALUE)
                .totalLinesCleared(9999)
                .currentLevel(100)
                .lineClearCount(1000)
                .itemGenerateCount(500)
                .blocksSpawned(100000)
                .build();
        
        String json = GameStateSerializer.toJson(original);
        GameState restored = GameStateSerializer.fromJson(json);
        
        assertEquals(Integer.MAX_VALUE, restored.getScore());
        assertEquals(9999, restored.getTotalLinesCleared());
        assertEquals(100, restored.getCurrentLevel());
        assertEquals(1000, restored.getLineClearCount());
        assertEquals(500, restored.getItemGenerateCount());
        assertEquals(100000, restored.getBlocksSpawned());
    }
    
    @Test
    @DisplayName("빈 리스트 직렬화")
    public void testEmptyListsSerialization() {
        int[][] board = new int[23][12];
        int[][] color = new int[23][12];
        
        GameState original = new GameState.Builder(board, color, null, null, false)
                .flashingRows(Arrays.asList())
                .boxFlashCenters(Arrays.asList())
                .build();
        
        String json = GameStateSerializer.toJson(original);
        GameState restored = GameStateSerializer.fromJson(json);
        
        assertNotNull(restored.getFlashingRows());
        assertNotNull(restored.getBoxFlashCenters());
        assertTrue(restored.getFlashingRows().isEmpty());
        assertTrue(restored.getBoxFlashCenters().isEmpty());
    }
    
    @Test
    @DisplayName("JSON 형식 검증")
    public void testJsonFormat() {
        int[][] board = new int[4][4];
        int[][] color = new int[4][4];
        
        GameState original = new GameState.Builder(board, color, null, null, true)
                .score(100)
                .build();
        
        String json = GameStateSerializer.toJson(original);
        
        assertNotNull(json);
        assertTrue(json.contains("\"score\":100"));
        assertTrue(json.contains("\"itemMode\":true"));
        assertTrue(json.contains("\"boardArray\":"));
        assertTrue(json.contains("\"colorBoard\":"));
    }
    
    @Test
    @DisplayName("복잡한 상태 직렬화")
    public void testComplexStateSerialization() {
        int[][] board = new int[23][12];
        int[][] color = new int[23][12];
        List<Integer> flashingRows = Arrays.asList(1, 2, 3);
        List<int[]> centers = Arrays.asList(new int[]{5, 5});
        
        GameState original = new GameState.Builder(board, color, null, null, true)
                .score(5000)
                .totalLinesCleared(50)
                .currentLevel(6)
                .lineClearCount(15)
                .itemGenerateCount(3)
                .blocksSpawned(200)
                .isAnimating(true)
                .lineClearAnimating(true)
                .flashBlack(true)
                .flashingRows(flashingRows)
                .boxClearAnimating(true)
                .boxFlashBlack(true)
                .boxFlashCenters(centers)
                .weightAnimating(false)
                .allClearAnimating(false)
                .lastLineClearScore(400)
                .build();
        
        String json = GameStateSerializer.toJson(original);
        GameState restored = GameStateSerializer.fromJson(json);
        
        assertEquals(original.getScore(), restored.getScore());
        assertEquals(original.getTotalLinesCleared(), restored.getTotalLinesCleared());
        assertEquals(original.getCurrentLevel(), restored.getCurrentLevel());
        assertEquals(original.getLineClearCount(), restored.getLineClearCount());
        assertEquals(original.getItemGenerateCount(), restored.getItemGenerateCount());
        assertEquals(original.getBlocksSpawned(), restored.getBlocksSpawned());
        assertEquals(original.isItemMode(), restored.isItemMode());
        assertEquals(original.isAnimating(), restored.isAnimating());
        assertEquals(original.isLineClearAnimating(), restored.isLineClearAnimating());
        assertEquals(original.isFlashBlack(), restored.isFlashBlack());
        assertEquals(original.isBoxClearAnimating(), restored.isBoxClearAnimating());
        assertEquals(original.isBoxFlashBlack(), restored.isBoxFlashBlack());
        assertEquals(original.isWeightAnimating(), restored.isWeightAnimating());
        assertEquals(original.isAllClearAnimating(), restored.isAllClearAnimating());
        assertEquals(original.getLastLineClearScore(), restored.getLastLineClearScore());
    }
}
