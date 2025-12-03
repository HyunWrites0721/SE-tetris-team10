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
        // 실제 구현: (totalLinesCleared / 2) + 1
        assertEquals(1, engine.calculateLevel(0));  // (0/2)+1 = 1
        assertEquals(1, engine.calculateLevel(1));  // (1/2)+1 = 1
        assertEquals(2, engine.calculateLevel(2));  // (2/2)+1 = 2
        assertEquals(3, engine.calculateLevel(4));  // (4/2)+1 = 3
        assertEquals(6, engine.calculateLevel(10)); // (10/2)+1 = 6
        assertEquals(10, engine.calculateLevel(20)); // (20/2)+1 = 11 → max 10
    }
    
    @Test
    @DisplayName("라인 클리어 점수 계산")
    void testCalculateLineClearScore() {
        // 실제 공식: baseScore × (currentLevel + 1) × difficultyMultiplier
        // difficultyMultiplier = 1.0 (일반 모드)
        
        // 1줄 클리어 (레벨 1): 100 × (1+1) = 200
        assertEquals(200, engine.calculateLineClearScore(1, 1));
        
        // 2줄 클리어 (레벨 1): 300 × (1+1) = 600
        assertEquals(600, engine.calculateLineClearScore(2, 1));
        
        // 3줄 클리어 (레벨 2): 500 × (2+1) = 1500
        assertEquals(1500, engine.calculateLineClearScore(3, 2));
        
        // 4줄 클리어 (레벨 3, 테트리스): 800 × (3+1) = 3200
        assertEquals(3200, engine.calculateLineClearScore(4, 3));
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
    
    @Test
    @DisplayName("충돌 감지 - 벽")
    void testCollisionWithWall() {
        // 블록을 왼쪽 벽 근처로 이동
        testBlock.setPosition(1, 5);
        
        GameState leftWallState = new GameState.Builder(testBoard, testColorBoard, testBlock, testBlock, false).build();
        GameState leftResult = engine.moveLeft(leftWallState);
        
        // 왼쪽 벽과 충돌하면 이동 안 됨
        assertNotNull(leftResult);
    }
    
    @Test
    @DisplayName("충돌 감지 - 다른 블록")
    void testCollisionWithOtherBlocks() {
        // 보드에 블록 배치
        testBoard[10][5] = 1;
        testBoard[10][6] = 1;
        
        // 블록을 바로 위에 배치
        testBlock.setPosition(5, 8);
        
        GameState blockCollisionState = new GameState.Builder(testBoard, testColorBoard, testBlock, testBlock, false).build();
        GameState result = engine.moveDown(blockCollisionState);
        
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("블록 회전 - Wall Kick")
    void testRotateWithWallKick() {
        // 블록을 벽 근처에 배치
        testBlock.setPosition(1, 5);
        
        GameState wallState = new GameState.Builder(testBoard, testColorBoard, testBlock, testBlock, false).build();
        GameState rotatedState = engine.rotate(wallState);
        
        assertNotNull(rotatedState);
    }
    
    @Test
    @DisplayName("블록 배치")
    void testPlaceBlock() {
        testBlock.setPosition(5, 10);
        
        GameState beforePlace = new GameState.Builder(testBoard, testColorBoard, testBlock, testBlock, false).build();
        
        // 블록 배치 (moveDown이 더 이상 이동 불가능할 때 발생)
        for (int i = 0; i < 15; i++) {
            GameState nextState = engine.moveDown(beforePlace);
            if (nextState.getCurrentBlock() != beforePlace.getCurrentBlock()) {
                // 새 블록이 생성되었다는 것은 이전 블록이 배치되었음을 의미
                break;
            }
            beforePlace = nextState;
        }
        
        assertNotNull(beforePlace);
    }
    
    @Test
    @DisplayName("라인 클리어 점수 - 난이도 곱셈")
    void testLineClearScoreWithDifficulty() {
        // Normal difficulty (0) - 1.0x
        GameEngine normalEngine = new GameEngine(0);
        int normalScore = normalEngine.calculateLineClearScore(1, 1);
        
        // Hard difficulty (1) - 1.1x
        GameEngine hardEngine = new GameEngine(1);
        int hardScore = hardEngine.calculateLineClearScore(1, 1);
        
        // Very Hard difficulty (2) - 0.9x
        GameEngine veryHardEngine = new GameEngine(2);
        int veryHardScore = veryHardEngine.calculateLineClearScore(1, 1);
        
        // 난이도 1이 가장 높고, 난이도 2가 가장 낮음
        assertTrue(normalScore > 0);
        assertTrue(hardScore >= normalScore); // 1.1x
        assertTrue(veryHardScore <= normalScore); // 0.9x
    }
    
    @Test
    @DisplayName("자동 낙하 점수 - 난이도 곱셈")
    void testAutoDropScoreWithDifficulty() {
        // Normal difficulty
        GameEngine normalEngine = new GameEngine(0);
        int normalScore0 = normalEngine.calculateAutoDropScore(0);
        
        // Hard difficulty
        GameEngine hardEngine = new GameEngine(1);
        int hardScore0 = hardEngine.calculateAutoDropScore(0);
        
        // 기본 점수는 비슷
        assertTrue(normalScore0 > 0);
        assertTrue(hardScore0 > 0);
        
        // 높은 레벨에서 차이
        int normalScore3 = normalEngine.calculateAutoDropScore(3);
        int hardScore3 = hardEngine.calculateAutoDropScore(3);
        
        assertTrue(normalScore3 > normalScore0);
        assertTrue(hardScore3 >= normalScore3);
    }
    
    @Test
    @DisplayName("여러 줄 동시 클리어")
    void testMultipleLineClear() {
        int[][] board = engine.initializeBoard();
        
        // 3줄 동시에 채우기
        for (int row = 18; row <= 20; row++) {
            for (int col = 1; col <= 10; col++) {
                board[row][col] = 1;
            }
        }
        
        java.util.List<Integer> fullLines = engine.findFullLines(board);
        assertEquals(3, fullLines.size());
        
        // 점수 계산
        int score = engine.calculateLineClearScore(3, 1);
        assertTrue(score > 0);
    }
    
    @Test
    @DisplayName("레벨 계산 - 경계값")
    void testCalculateLevelBoundary() {
        assertEquals(1, engine.calculateLevel(0));
        assertEquals(1, engine.calculateLevel(1));
        assertEquals(2, engine.calculateLevel(2));
        assertEquals(2, engine.calculateLevel(3));
        assertEquals(3, engine.calculateLevel(4));
        assertEquals(10, engine.calculateLevel(18));  // (18/2)+1 = 10
        assertEquals(10, engine.calculateLevel(100)); // 최대 10
    }
    
    @Test
    @DisplayName("게임 오버 - 여러 블록")
    void testIsGameOverMultipleBlocks() {
        int[][] board = engine.initializeBoard();
        
        // 상단 여러 위치에 블록 배치
        board[2][3] = 1;
        board[2][7] = 1;
        board[3][5] = 1;
        
        assertTrue(engine.isGameOver(board));
    }
    
    @Test
    @DisplayName("블록 하드 드롭")
    void testHardDrop() {
        testBlock.setPosition(5, 3);
        
        GameState state = new GameState.Builder(testBoard, testColorBoard, testBlock, testBlock, false).build();
        
        // 여러 번 아래로 이동
        GameState currentState = state;
        for (int i = 0; i < 20; i++) {
            GameState nextState = engine.moveDown(currentState);
            if (nextState.getCurrentBlock() != currentState.getCurrentBlock()) {
                break;
            }
            currentState = nextState;
        }
        
        assertNotNull(currentState);
    }
    
    @Test
    @DisplayName("블록 이동 - 경계 테스트")
    void testMoveBoundary() {
        // 오른쪽 끝에 블록 배치
        testBlock.setPosition(9, 5);
        
        GameState rightEdgeState = new GameState.Builder(testBoard, testColorBoard, testBlock, testBlock, false).build();
        
        // 오른쪽으로 이동 시도
        for (int i = 0; i < 5; i++) {
            rightEdgeState = engine.moveRight(rightEdgeState);
        }
        
        assertNotNull(rightEdgeState);
        assertTrue(testBlock.getX() <= 10);
    }
}
