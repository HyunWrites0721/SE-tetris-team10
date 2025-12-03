package game.core;

import blocks.Block;
import blocks.IBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameState 테스트")
class GameStateTest {
    
    private int[][] testBoard;
    private int[][] testColorBoard;
    private Block testCurrentBlock;
    private Block testNextBlock;
    
    @BeforeEach
    void setUp() {
        // 23x12 보드 초기화
        testBoard = new int[23][12];
        testColorBoard = new int[23][12];
        
        // 벽 설정
        for (int i = 0; i < 23; i++) {
            for (int j = 0; j < 12; j++) {
                if (j == 0 || j == 11 || i == 22) {
                    testBoard[i][j] = 10;
                }
            }
        }
        
        testCurrentBlock = new IBlock();
        testCurrentBlock.setShape();
        testNextBlock = new IBlock();
        testNextBlock.setShape();
    }
    
    @Test
    @DisplayName("GameState 생성 테스트")
    void testGameStateCreation() {
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .score(100)
                .currentLevel(3)
                .totalLinesCleared(10)
                .build();
        
        assertNotNull(state);
        assertEquals(100, state.getScore());
        assertEquals(3, state.getCurrentLevel());
        assertEquals(10, state.getTotalLinesCleared());
        assertFalse(state.isItemMode());
    }
    
    @Test
    @DisplayName("GameState 깊은 복사 테스트")
    void testDeepCopy() {
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .build();
        
        int[][] boardCopy = state.getBoardArray();
        
        // 원본 수정
        testBoard[5][5] = 99;
        
        // 복사본은 변경되지 않아야 함
        assertNotEquals(99, boardCopy[5][5]);
    }
    
    @Test
    @DisplayName("GameState 불변성 테스트")
    void testImmutability() {
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .score(50)
                .build();
        
        int[][] retrievedBoard = state.getBoardArray();
        retrievedBoard[5][5] = 999;
        
        // 내부 보드는 변경되지 않아야 함
        int[][] retrievedBoardAgain = state.getBoardArray();
        assertEquals(0, retrievedBoardAgain[5][5]);
    }
    
    @Test
    @DisplayName("Builder 패턴 체이닝 테스트")
    void testBuilderChaining() {
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, true)
                .score(200)
                .currentLevel(5)
                .totalLinesCleared(20)
                .lineClearCount(15)
                .itemGenerateCount(2)
                .blocksSpawned(30)
                .isAnimating(true)
                .lastLineClearScore(800)
                .build();
        
        assertEquals(200, state.getScore());
        assertEquals(5, state.getCurrentLevel());
        assertEquals(20, state.getTotalLinesCleared());
        assertEquals(15, state.getLineClearCount());
        assertEquals(2, state.getItemGenerateCount());
        assertEquals(30, state.getBlocksSpawned());
        assertTrue(state.isAnimating());
        assertEquals(800, state.getLastLineClearScore());
        assertTrue(state.isItemMode());
    }
    
    @Test
    @DisplayName("toBuilder 테스트 - 상태 복사 및 수정")
    void testToBuilder() {
        GameState originalState = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .score(100)
                .currentLevel(3)
                .build();
        
        GameState modifiedState = originalState.toBuilder()
                .score(200)
                .currentLevel(4)
                .build();
        
        // 원본은 변경되지 않음
        assertEquals(100, originalState.getScore());
        assertEquals(3, originalState.getCurrentLevel());
        
        // 새 상태는 변경됨
        assertEquals(200, modifiedState.getScore());
        assertEquals(4, modifiedState.getCurrentLevel());
    }
    
    @Test
    @DisplayName("기본값 테스트")
    void testDefaultValues() {
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .build();
        
        assertEquals(0, state.getScore());
        assertEquals(1, state.getCurrentLevel());
        assertEquals(0, state.getTotalLinesCleared());
        assertEquals(0, state.getLineClearCount());
        assertEquals(0, state.getItemGenerateCount());
        assertEquals(0, state.getBlocksSpawned());
        assertFalse(state.isAnimating());
        assertEquals(0, state.getLastLineClearScore());
    }
    
    @Test
    @DisplayName("toString 테스트")
    void testToString() {
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, true)
                .score(500)
                .currentLevel(7)
                .totalLinesCleared(35)
                .build();
        
        String str = state.toString();
        assertTrue(str.contains("score=500"));
        assertTrue(str.contains("level=7"));
        assertTrue(str.contains("linesCleared=35"));
        assertTrue(str.contains("itemMode=true"));
    }
    
    @Test
    @DisplayName("Block 참조 테스트")
    void testBlockReferences() {
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .build();
        
        assertNotNull(state.getCurrentBlock());
        assertNotNull(state.getNextBlock());
        assertSame(testCurrentBlock, state.getCurrentBlock());
        assertSame(testNextBlock, state.getNextBlock());
    }
    
    @Test
    @DisplayName("null Block 허용 테스트")
    void testNullBlocks() {
        GameState state = new GameState.Builder(testBoard, testColorBoard, null, null, false)
                .build();
        
        assertNull(state.getCurrentBlock());
        assertNull(state.getNextBlock());
    }
    
    @Test
    @DisplayName("애니메이션 상태 - 라인 클리어")
    void testLineClearAnimationState() {
        java.util.List<Integer> flashingRows = java.util.Arrays.asList(5, 10, 15);
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .lineClearAnimating(true)
                .flashBlack(true)
                .flashingRows(flashingRows)
                .build();
        
        assertTrue(state.isLineClearAnimating());
        assertTrue(state.isFlashBlack());
        assertEquals(3, state.getFlashingRows().size());
        assertTrue(state.isRowFlashing(5));
        assertTrue(state.isRowFlashing(10));
        assertFalse(state.isRowFlashing(20));
    }
    
    @Test
    @DisplayName("애니메이션 상태 - AllClear")
    void testAllClearAnimationState() {
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .allClearAnimating(true)
                .allClearFlashBlack(true)
                .build();
        
        assertTrue(state.isAllClearAnimating());
        assertTrue(state.isAllClearFlashBlack());
    }
    
    @Test
    @DisplayName("애니메이션 상태 - BoxClear")
    void testBoxClearAnimationState() {
        java.util.List<int[]> centers = java.util.Arrays.asList(new int[]{5, 5}, new int[]{10, 10});
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .boxClearAnimating(true)
                .boxFlashBlack(true)
                .boxFlashCenters(centers)
                .build();
        
        assertTrue(state.isBoxClearAnimating());
        assertTrue(state.isBoxFlashBlack());
        assertEquals(2, state.getBoxFlashCenters().size());
    }
    
    @Test
    @DisplayName("애니메이션 상태 - Weight")
    void testWeightAnimationState() {
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .weightAnimating(true)
                .build();
        
        assertTrue(state.isWeightAnimating());
    }
    
    @Test
    @DisplayName("flashingRows 불변성 테스트")
    void testFlashingRowsImmutability() {
        java.util.List<Integer> flashingRows = new java.util.ArrayList<>();
        flashingRows.add(5);
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .flashingRows(flashingRows)
                .build();
        
        java.util.List<Integer> retrieved = state.getFlashingRows();
        retrieved.add(10);
        
        assertEquals(1, state.getFlashingRows().size());
    }
    
    @Test
    @DisplayName("boxFlashCenters 불변성 테스트")
    void testBoxFlashCentersImmutability() {
        java.util.List<int[]> centers = new java.util.ArrayList<>();
        centers.add(new int[]{5, 5});
        GameState state = new GameState.Builder(testBoard, testColorBoard, testCurrentBlock, testNextBlock, false)
                .boxFlashCenters(centers)
                .build();
        
        java.util.List<int[]> retrieved = state.getBoxFlashCenters();
        retrieved.add(new int[]{10, 10});
        
        assertEquals(1, state.getBoxFlashCenters().size());
    }
}
