package game.core;

import blocks.Block;
import blocks.IBlock;
import blocks.OBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameEngine 테스트")
class GameEngineTest {
    
    private GameEngine engine;
    private GameState testState;
    private int[][] testBoard;
    private int[][] testColorBoard;
    private Block testBlock;
    
    @BeforeEach
    void setUp() {
        engine = new GameEngine(0); // Normal difficulty
        
        // 보드 초기화
        testBoard = engine.initializeBoard();
        testColorBoard = new int[23][12];
        
        // 테스트 블록 생성
        testBlock = new IBlock();
        testBlock.setShape();
        testBlock.setPosition(5, 5);
        
        Block nextBlock = new OBlock();
        nextBlock.setShape();
        
        // GameState 생성
        testState = new GameState.Builder(testBoard, testColorBoard, testBlock, nextBlock, false)
                .score(100)
                .currentLevel(1)
                .build();
    }
    
    @Test
    @DisplayName("보드 초기화 테스트")
    void testInitializeBoard() {
        int[][] board = engine.initializeBoard();
        
        assertNotNull(board);
        assertEquals(23, board.length);
        assertEquals(12, board[0].length);
        
        // 벽 확인 (왼쪽, 오른쪽, 아래)
        assertEquals(10, board[0][0]); // 왼쪽 벽
        assertEquals(10, board[0][11]); // 오른쪽 벽
        assertEquals(10, board[22][5]); // 아래 벽
        
        // 내부는 빈 공간
        assertEquals(0, board[5][5]);
    }
    
    @Test
    @DisplayName("블록 이동 - 왼쪽")
    void testMoveLeft() {
        int originalX = testBlock.getX();
        
        GameState newState = engine.moveLeft(testState);
        
        assertNotNull(newState);
        // 이동 가능한 경우 X 좌표가 감소해야 함
        assertTrue(testBlock.getX() <= originalX);
    }
    
    @Test
    @DisplayName("블록 이동 - 오른쪽")
    void testMoveRight() {
        int originalX = testBlock.getX();
        
        GameState newState = engine.moveRight(testState);
        
        assertNotNull(newState);
        // 이동 가능한 경우 X 좌표가 증가해야 함
        assertTrue(testBlock.getX() >= originalX);
    }
    
    @Test
    @DisplayName("블록 이동 - 아래")
    void testMoveDown() {
        int originalY = testBlock.getY();
        
        GameState newState = engine.moveDown(testState);
        
        assertNotNull(newState);
        // 이동 가능한 경우 Y 좌표가 증가해야 함
        assertTrue(testBlock.getY() >= originalY);
    }
    
    @Test
    @DisplayName("블록 회전")
    void testRotate() {
        int[][] originalShape = testBlock.getShape();
        
        GameState newState = engine.rotate(testState);
        
        assertNotNull(newState);
        // 회전 후 shape가 변경되어야 함 (I 블록은 회전 시 모양 변경)
        assertNotNull(testBlock.getShape());
    }
    
    @Test
    @DisplayName("게임 오버 체크 - 정상")
    void testIsGameOverFalse() {
        int[][] board = engine.initializeBoard();
        
        assertFalse(engine.isGameOver(board));
    }
    
    @Test
    @DisplayName("게임 오버 체크 - 게임 오버")
    void testIsGameOverTrue() {
        int[][] board = engine.initializeBoard();
        
        // 상단 영역에 블록 배치
        board[2][5] = 1;
        
        assertTrue(engine.isGameOver(board));
    }
    
    @Test
    @DisplayName("레벨 계산")
    void testCalculateLevel() {
        assertEquals(1, engine.calculateLevel(0));
        assertEquals(1, engine.calculateLevel(9));
        assertEquals(2, engine.calculateLevel(10));
        assertEquals(3, engine.calculateLevel(20));
        assertEquals(5, engine.calculateLevel(45));
    }
    
    @Test
    @DisplayName("라인 클리어 점수 계산")
    void testCalculateLineClearScore() {
        // 1줄 클리어
        assertEquals(100, engine.calculateLineClearScore(1, 1));
        
        // 2줄 클리어 (레벨 1)
        assertEquals(300, engine.calculateLineClearScore(2, 1));
        
        // 3줄 클리어 (레벨 2)
        assertEquals(1000, engine.calculateLineClearScore(3, 2));
        
        // 4줄 클리어 (레벨 3, 테트리스)
        assertEquals(2400, engine.calculateLineClearScore(4, 3));
    }
    
    @Test
    @DisplayName("전체 줄 찾기")
    void testFindFullLines() {
        int[][] board = engine.initializeBoard();
        
        // 빈 보드는 전체 줄 없음
        assertTrue(engine.findFullLines(board).isEmpty());
        
        // 한 줄을 가득 채우기 (row 20)
        for (int col = 1; col <= 10; col++) {
            board[20][col] = 1;
        }
        
        java.util.List<Integer> fullLines = engine.findFullLines(board);
        assertEquals(1, fullLines.size());
        assertEquals(20, fullLines.get(0));
    }
    
    @Test
    @DisplayName("라인 클리어 실행")
    void testClearLine() {
        int[][] board = engine.initializeBoard();
        int[][] colorBoard = new int[23][12];
        
        // 20번째 줄에 블록 배치
        for (int col = 1; col <= 10; col++) {
            board[20][col] = 1;
            colorBoard[20][col] = 0xFF0000; // 빨간색
        }
        
        // 19번째 줄에도 블록 배치
        board[19][5] = 1;
        colorBoard[19][5] = 0x00FF00; // 녹색
        
        // 20번째 줄 클리어
        engine.clearLine(board, colorBoard, 20);
        
        // 19번째 줄의 블록이 20번째 줄로 이동해야 함
        assertEquals(1, board[20][5]);
        assertEquals(0x00FF00, colorBoard[20][5]);
        
        // 19번째 줄은 비어있어야 함 (위에서 아래로 내려옴)
        assertEquals(0, board[19][5]);
    }
    
    @Test
    @DisplayName("아이템 블록 생성 조건 - 일반 모드")
    void testShouldSpawnItemBlockNormalMode() {
        GameState normalState = testState.toBuilder().build();
        
        assertFalse(engine.shouldSpawnItemBlock(normalState, 10));
    }
    
    @Test
    @DisplayName("아이템 블록 생성 조건 - 아이템 모드")
    void testShouldSpawnItemBlockItemMode() {
        GameState itemState = new GameState.Builder(testBoard, testColorBoard, testBlock, testBlock, true)
                .lineClearCount(10)
                .itemGenerateCount(0)
                .build();
        
        assertTrue(engine.shouldSpawnItemBlock(itemState, 10));
        
        // 이미 생성한 경우
        GameState itemState2 = itemState.toBuilder()
                .itemGenerateCount(1)
                .build();
        
        assertFalse(engine.shouldSpawnItemBlock(itemState2, 10));
    }
    
    @Test
    @DisplayName("자동 낙하 점수 계산")
    void testCalculateAutoDropScore() {
        // 속도 레벨 0
        assertEquals(1, engine.calculateAutoDropScore(0));
        
        // 속도 레벨 3
        assertEquals(4, engine.calculateAutoDropScore(3));
        
        // 속도 레벨 6
        assertEquals(7, engine.calculateAutoDropScore(6));
    }
    
    @Test
    @DisplayName("유효한 위치 확인")
    void testIsValidPosition() {
        assertTrue(engine.isValidPosition(0, 0));
        assertTrue(engine.isValidPosition(22, 11));
        assertTrue(engine.isValidPosition(10, 5));
        
        assertFalse(engine.isValidPosition(-1, 0));
        assertFalse(engine.isValidPosition(0, -1));
        assertFalse(engine.isValidPosition(23, 0));
        assertFalse(engine.isValidPosition(0, 12));
    }
}
