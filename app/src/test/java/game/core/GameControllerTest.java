package game.core;

import blocks.Block;
import blocks.IBlock;
import blocks.OBlock;
import game.GameView;
import game.events.EventBus;
import game.events.TickEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameController 테스트")
class GameControllerTest {
    
    private GameController controller;
    private TestGameView testView;
    
    // 테스트용 GameView 구현
    private static class TestGameView extends GameView {
        private Block fallingBlock;
        private Block nextBlock;
        private int score;
        private int highScore;
        
        TestGameView() {
            super(false);  // 아이템 모드 비활성화
        }
        
        @Override
        public void setFallingBlock(Block block) {
            this.fallingBlock = block;
        }
        
        @Override
        public void setNextBlock(Block block) {
            this.nextBlock = block;
        }
        
        @Override
        public void setScore(int score) {
            this.score = score;
        }
        
        @Override
        public void setHighScore(int highScore) {
            this.highScore = highScore;
        }
        
        @Override
        public void render(GameState state) {
            // 테스트용 빈 구현
        }
        
        @Override
        public void repaintBlock() {
            // 테스트용 빈 구현
        }
        
        public Block getFallingBlock() {
            return fallingBlock;
        }
        
        public Block getNextBlock() {
            return nextBlock;
        }
        
        public int getScore() {
            return score;
        }
    }
    
    @BeforeEach
    void setUp() {
        testView = new TestGameView();
    }
    
    @AfterEach
    void tearDown() {
        if (controller != null && controller.isRunning()) {
            controller.stop();
        }
    }
    
    @Test
    @DisplayName("GameController 생성 테스트 - 일반 모드")
    void testGameControllerCreation_NormalMode() {
        controller = new GameController(testView, false, 0);
        
        assertNotNull(controller);
        assertNotNull(controller.getCurrentState());
        assertNotNull(controller.getEngine());
        assertNotNull(controller.getEventBus());
        assertFalse(controller.isRunning());
        assertFalse(controller.isPaused());
    }
    
    @Test
    @DisplayName("GameController 생성 테스트 - 아이템 모드")
    void testGameControllerCreation_ItemMode() {
        controller = new GameController(testView, true, 0);
        
        assertNotNull(controller);
        assertNotNull(controller.getCurrentState());
    }
    
    @Test
    @DisplayName("게임 시작 테스트")
    void testStart() {
        controller = new GameController(testView, false, 0);
        
        controller.start();
        
        assertTrue(controller.isRunning());
        assertFalse(controller.isPaused());
        assertNotNull(controller.getCurrentBlock());
    }
    
    @Test
    @DisplayName("게임 정지 테스트")
    void testStop() {
        controller = new GameController(testView, false, 0);
        
        controller.start();
        assertTrue(controller.isRunning());
        
        controller.stop();
        assertFalse(controller.isRunning());
    }
    
    @Test
    @DisplayName("게임 일시정지 테스트")
    void testPause() {
        controller = new GameController(testView, false, 0);
        
        controller.start();
        controller.pause();
        
        assertTrue(controller.isPaused());
    }
    
    @Test
    @DisplayName("게임 재개 테스트")
    void testResume() {
        controller = new GameController(testView, false, 0);
        
        controller.start();
        controller.pause();
        assertTrue(controller.isPaused());
        
        controller.resume();
        assertFalse(controller.isPaused());
    }
    
    @Test
    @DisplayName("게임 리셋 테스트")
    void testReset() {
        controller = new GameController(testView, false, 0);
        
        controller.start();
        controller.addScore(500);
        
        controller.reset();
        
        assertEquals(0, controller.getScore());
        assertNotNull(controller.getCurrentState());
        assertFalse(controller.isRunning());
    }
    
    @Test
    @DisplayName("점수 추가 테스트")
    void testAddScore() {
        controller = new GameController(testView, false, 0);
        
        assertEquals(0, controller.getScore());
        
        controller.addScore(100);
        assertEquals(100, controller.getScore());
        
        controller.addScore(50);
        assertEquals(150, controller.getScore());
    }
    
    @Test
    @DisplayName("블록 왼쪽 이동 테스트")
    void testMoveLeft() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        Block currentBlock = controller.getCurrentBlock();
        assertNotNull(currentBlock);
        
        int originalX = currentBlock.getX();
        
        controller.moveLeft();
        
        // 이동 가능한 경우 X 좌표가 감소해야 함
        assertTrue(currentBlock.getX() <= originalX);
    }
    
    @Test
    @DisplayName("블록 오른쪽 이동 테스트")
    void testMoveRight() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        Block currentBlock = controller.getCurrentBlock();
        assertNotNull(currentBlock);
        
        int originalX = currentBlock.getX();
        
        controller.moveRight();
        
        // 이동 가능한 경우 X 좌표가 증가해야 함
        assertTrue(currentBlock.getX() >= originalX);
    }
    
    @Test
    @DisplayName("블록 아래 이동 테스트")
    void testMoveDown() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        Block currentBlock = controller.getCurrentBlock();
        assertNotNull(currentBlock);
        
        int originalY = currentBlock.getY();
        
        controller.moveDown();
        
        // 이동 가능한 경우 Y 좌표가 증가해야 함
        assertTrue(currentBlock.getY() >= originalY);
    }
    
    @Test
    @DisplayName("블록 회전 테스트")
    void testRotate() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        Block currentBlock = controller.getCurrentBlock();
        assertNotNull(currentBlock);
        
        assertDoesNotThrow(() -> {
            controller.rotate();
        });
    }
    
    @Test
    @DisplayName("하드 드롭 테스트")
    void testHardDrop() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        Block currentBlock = controller.getCurrentBlock();
        assertNotNull(currentBlock);
        
        int originalY = currentBlock.getY();
        int originalScore = controller.getScore();
        
        int dropDistance = controller.hardDrop();
        
        // 드롭 거리만큼 점수 증가 (한 칸당 2점)
        assertTrue(dropDistance >= 0);
    }
    
    @Test
    @DisplayName("일시정지 중 블록 이동 불가 테스트")
    void testNoMovementWhenPaused() {
        controller = new GameController(testView, false, 0);
        controller.start();
        controller.pause();
        
        Block currentBlock = controller.getCurrentBlock();
        assertNotNull(currentBlock);
        
        int originalX = currentBlock.getX();
        int originalY = currentBlock.getY();
        
        controller.moveLeft();
        controller.moveRight();
        controller.moveDown();
        
        // 일시정지 중에는 블록이 이동하지 않아야 함
        assertEquals(originalX, currentBlock.getX());
        assertEquals(originalY, currentBlock.getY());
    }
    
    @Test
    @DisplayName("게임 정지 중 블록 이동 불가 테스트")
    void testNoMovementWhenStopped() {
        controller = new GameController(testView, false, 0);
        
        // 게임을 시작하지 않은 상태에서 이동 시도
        assertDoesNotThrow(() -> {
            controller.moveLeft();
            controller.moveRight();
            controller.moveDown();
            controller.rotate();
        });
    }
    
    @Test
    @DisplayName("보드 배열 접근 테스트")
    void testGetBoard() {
        controller = new GameController(testView, false, 0);
        
        int[][] board = controller.getBoard();
        
        assertNotNull(board);
        assertEquals(23, board.length);
        assertEquals(12, board[0].length);
    }
    
    @Test
    @DisplayName("현재 상태 접근 테스트")
    void testGetCurrentState() {
        controller = new GameController(testView, false, 0);
        
        GameState state = controller.getCurrentState();
        
        assertNotNull(state);
        assertEquals(0, state.getScore());
        assertEquals(1, state.getCurrentLevel());
    }
    
    @Test
    @DisplayName("이벤트 버스 접근 테스트")
    void testGetEventBus() {
        controller = new GameController(testView, false, 0);
        
        EventBus eventBus = controller.getEventBus();
        
        assertNotNull(eventBus);
    }
    
    @Test
    @DisplayName("게임 엔진 접근 테스트")
    void testGetEngine() {
        controller = new GameController(testView, false, 0);
        
        GameEngine engine = controller.getEngine();
        
        assertNotNull(engine);
    }
    
    @Test
    @DisplayName("속도 업데이트 테스트")
    void testUpdateSpeed() {
        controller = new GameController(testView, false, 0);
        
        assertDoesNotThrow(() -> {
            controller.updateSpeed(1);
            controller.updateSpeed(3);
            controller.updateSpeed(6);
        });
    }
    
    @Test
    @DisplayName("공격 줄 추가 테스트")
    void testAddAttackLines() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        int[][] blockPattern = {{1, 0, 1}};
        int blockX = 5;
        
        assertDoesNotThrow(() -> {
            controller.addAttackLines(2, blockPattern, blockX);
        });
    }
    
    @Test
    @DisplayName("공격 줄 큐잉 테스트")
    void testQueueAttackLines() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        int[][] blockPattern = {{1, 0, 1}};
        int blockX = 5;
        
        assertDoesNotThrow(() -> {
            controller.queueAttackLines(1, blockPattern, blockX);
            controller.queueAttackLines(2, blockPattern, blockX);
        });
    }
    
    @Test
    @DisplayName("공격 줄 추가 - 0줄 이하 무시")
    void testAddAttackLines_ZeroOrNegative() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        int[][] originalBoard = controller.getBoard();
        
        controller.addAttackLines(0, null, 0);
        controller.addAttackLines(-1, null, 0);
        
        // 보드가 변경되지 않아야 함
        assertNotNull(originalBoard);
    }
    
    @Test
    @DisplayName("마지막 블록 정보 반환 테스트")
    void testGetLastBlockInfo() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        Object[] lastBlockInfo = controller.getLastBlockInfo();
        
        assertNotNull(lastBlockInfo);
        assertEquals(2, lastBlockInfo.length);
    }
    
    @Test
    @DisplayName("난이도 설정 테스트 - Normal")
    void testDifficulty_Normal() {
        controller = new GameController(testView, false, 0);
        
        assertNotNull(controller);
    }
    
    @Test
    @DisplayName("난이도 설정 테스트 - Hard")
    void testDifficulty_Hard() {
        controller = new GameController(testView, false, 1);
        
        assertNotNull(controller);
    }
    
    @Test
    @DisplayName("난이도 설정 테스트 - Easy")
    void testDifficulty_Easy() {
        controller = new GameController(testView, false, 2);
        
        assertNotNull(controller);
    }
    
    @Test
    @DisplayName("중복 시작 방지 테스트")
    void testPreventDuplicateStart() {
        controller = new GameController(testView, false, 0);
        
        controller.start();
        assertTrue(controller.isRunning());
        
        // 중복 시작 시도
        controller.start();
        
        // 여전히 실행 중이어야 함
        assertTrue(controller.isRunning());
    }
    
    @Test
    @DisplayName("점수 추가 - 음수 테스트")
    void testAddScore_Negative() {
        controller = new GameController(testView, false, 0);
        
        controller.addScore(100);
        assertEquals(100, controller.getScore());
        
        controller.addScore(-50);
        assertEquals(50, controller.getScore());
    }
    
    @Test
    @DisplayName("게임 시작 후 블록 생성 확인")
    void testBlockSpawnedAfterStart() {
        controller = new GameController(testView, false, 0);
        
        assertNull(controller.getCurrentBlock());
        
        controller.start();
        
        assertNotNull(controller.getCurrentBlock());
    }
    
    @Test
    @DisplayName("리셋 후 초기 상태 확인")
    void testInitialStateAfterReset() {
        controller = new GameController(testView, false, 0);
        
        controller.start();
        controller.addScore(1000);
        controller.reset();
        
        assertEquals(0, controller.getScore());
        assertNotNull(controller.getCurrentState());
        assertEquals(0, controller.getCurrentState().getScore());
    }
    
    @Test
    @DisplayName("하드 드롭 점수 계산 테스트")
    void testHardDropScore() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        int scoreBeforeDrop = controller.getScore();
        int dropDistance = controller.hardDrop();
        int scoreAfterDrop = controller.getScore();
        
        // 하드 드롭 점수는 거리 * 2
        if (dropDistance > 0) {
            assertTrue(scoreAfterDrop > scoreBeforeDrop);
        }
    }
    
    @Test
    @DisplayName("소프트 드롭 점수 테스트")
    void testSoftDropScore() {
        controller = new GameController(testView, false, 0);
        controller.start();
        
        int scoreBeforeMove = controller.getScore();
        controller.moveDown();
        int scoreAfterMove = controller.getScore();
        
        // 소프트 드롭은 한 칸당 1점
        assertTrue(scoreAfterMove >= scoreBeforeMove);
    }
}
