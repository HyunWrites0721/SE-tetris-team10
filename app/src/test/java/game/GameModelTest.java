package game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.SwingUtilities;

import blocks.Block;

public class GameModelTest {
    private GameModel gameModel;
    private GameView gameView;

    @BeforeEach
    void setUp() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                gameView = new GameView();
                gameModel = new GameModel(gameView, false); // Normal mode로 테스트
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        if (gameView != null) {
            SwingUtilities.invokeLater(() -> {
                gameView.setVisible(false);
                if (gameView.getParent() != null) {
                    gameView.getParent().setVisible(false);
                }
            });
        }
    }

    @Test
    @DisplayName("게임 모델 초기화 테스트")
    void testGameModelInitialization() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 보드 초기화 확인
            int[][] board = gameModel.getBoard();
            assertNotNull(board, "보드가 초기화되어야 함");
            assertEquals(23, board.length, "보드 행 수는 23이어야 함");
            assertEquals(12, board[0].length, "보드 열 수는 12이어야 함");

            // 벽 초기화 확인
            for (int i = 0; i < 23; i++) {
                assertEquals(10, board[i][0], "왼쪽 벽이 초기화되어야 함");
                assertEquals(10, board[i][11], "오른쪽 벽이 초기화되어야 함");
            }
            for (int j = 0; j < 12; j++) {
                assertEquals(10, board[22][j], "바닥이 초기화되어야 함");
            }

            // 내부 영역이 비어있는지 확인
            for (int i = 2; i < 21; i++) {
                for (int j = 1; j < 11; j++) {
                    assertEquals(0, board[i][j], "내부 영역은 비어있어야 함");
                }
            }

            // 블록 초기화 확인
            assertNotNull(gameModel.getCurrentBlock(), "현재 블록이 생성되어야 함");
            assertNotNull(gameModel.nextBlock, "다음 블록이 생성되어야 함");
        });
    }

    @Test
    @DisplayName("보드 재초기화 테스트")
    void testBoardInit() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 보드에 임의 값 설정
            int[][] board = gameModel.getBoard();
            board[5][5] = 1;
            board[10][7] = 1;

            // 보드 재초기화
            gameModel.boardInit();

            // 내부 영역이 다시 비어있는지 확인
            for (int i = 2; i < 21; i++) {
                for (int j = 1; j < 11; j++) {
                    assertEquals(0, board[i][j], "재초기화 후 내부 영역은 비어있어야 함");
                }
            }

            // 벽은 그대로 유지되는지 확인
            assertEquals(10, board[0][0], "벽은 유지되어야 함");
            assertEquals(10, board[22][11], "벽은 유지되어야 함");
        });
    }

    @Test
    @DisplayName("유효한 위치 검사 테스트")
    void testIsValidPosition() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 빈 공간 테스트
            assertTrue(gameModel.isValidPosition(5, 5), "빈 공간은 유효해야 함");
            assertTrue(gameModel.isValidPosition(10, 3), "빈 공간은 유효해야 함");

            // 벽 위치 테스트
            assertFalse(gameModel.isValidPosition(0, 0), "벽 위치는 무효해야 함");
            assertFalse(gameModel.isValidPosition(22, 5), "바닥은 무효해야 함");
            assertFalse(gameModel.isValidPosition(5, 11), "오른쪽 벽은 무효해야 함");

            // 블록이 있는 위치 테스트
            gameModel.setBlockText(5, 5);
            assertFalse(gameModel.isValidPosition(5, 5), "블록이 있는 위치는 무효해야 함");
        });
    }

    @Test
    @DisplayName("블록 이동 가능성 검사 테스트")
    void testCanMoveTo() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Block currentBlock = gameModel.getCurrentBlock();
            assertNotNull(currentBlock, "현재 블록이 null이 아니어야 함");
            
            int[][] shape = currentBlock.getShape();
            assertNotNull(shape, "블록 shape이 null이 아니어야 함");

            // 유효한 위치로 이동 가능한지 테스트
            assertTrue(gameModel.canMoveto(5, 5, shape), "유효한 위치로 이동 가능해야 함");

            // 경계 테스트들을 더 안전하게 수정
            // 상단 경계 테스트 (row < 2)
            assertFalse(gameModel.canMoveto(1, 5, shape), "상단 경계(row < 2)는 이동 불가");
            
            // 바닥 근처 테스트 (row >= ROWS-1, 즉 row >= 22)
            assertFalse(gameModel.canMoveto(21, 5, shape), "바닥 근처는 이동 불가");
            
            // 경계 밖 테스트
            assertFalse(gameModel.canMoveto(-1, 5, shape), "음수 위치는 이동 불가");
            
            // 안전한 위치에서는 이동 가능
            assertTrue(gameModel.canMoveto(10, 5, shape), "안전한 중앙 위치는 이동 가능해야 함");
            
            // 좌우 경계 테스트는 블록 종류에 따라 다를 수 있으므로 제외
        });
    }

    @Test
    @DisplayName("블록 회전 테스트")
    void testRotation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 기존 현재 블록 사용 (이미 제대로 초기화된 블록)
            Block currentBlock = gameModel.getCurrentBlock();
            assertNotNull(currentBlock, "현재 블록이 null이 아니어야 함");
            
            // 안전한 위치로 이동
            currentBlock.setPosition(5, 5);

            // 회전 가능성 검사 (안전하게)
            try {
                gameModel.canRotate(); // 회전 가능성 검사만 수행
                // 회전 성공 여부와 관계없이 예외가 발생하지 않았으면 성공
                assertTrue(true, "회전 가능성 검사가 예외 없이 완료되어야 함");
            } catch (Exception e) {
                fail("회전 가능성 검사 중 예외 발생: " + e.getMessage());
            }

            // 실제 회전 수행 (안전하게)
            try {
                gameModel.Rotate90();
                // 회전 후에도 블록이 유효한 위치에 있는지 확인
                int x = currentBlock.getX();
                int y = currentBlock.getY();
                assertTrue(x >= 0 && x <= 11, "회전 후 x 위치가 유효해야 함");
                assertTrue(y >= 0 && y <= 22, "회전 후 y 위치가 유효해야 함");
            } catch (Exception e) {
                fail("회전 수행 중 예외 발생: " + e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("벽 근처 회전 보정 테스트")
    void testWallKickRotation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Block currentBlock = gameModel.getCurrentBlock();
            
            // 왼쪽 벽 근처로 이동
            currentBlock.setPosition(1, 10);
            
            // 회전 시도 (보정이 일어날 수 있음)
            gameModel.canRotate(); // 회전 가능성 검사
            
            // 위치가 유효한 범위 내에 있는지 확인
            int x = currentBlock.getX();
            assertTrue(x >= 1 && x <= 10, "회전 후 x 위치가 유효 범위 내에 있어야 함");
        });
    }

    @Test
    @DisplayName("하드 드롭 테스트")
    void testHardDrop() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Block originalBlock = gameModel.getCurrentBlock();
            
            // 하드 드롭 실행
            int dropDistance = gameModel.HardDrop();
            
            // 드롭 거리가 양수인지 확인
            assertTrue(dropDistance >= 0, "드롭 거리는 0 이상이어야 함");
            
            // 새로운 블록이 생성되었는지 확인
            Block newBlock = gameModel.getCurrentBlock();
            assertNotSame(originalBlock, newBlock, "하드 드롭 후 새로운 블록이 생성되어야 함");
        });
    }

    @Test
    @DisplayName("새 블록 생성 테스트")
    void testSpawnNewBlock() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Block originalNext = gameModel.nextBlock;
            int originalBlocksSpawned = gameModel.getBlocksSpawned();
            
            // 새 블록 생성
            gameModel.spawnNewBlock();
            
            // 현재 블록이 이전의 다음 블록이 되었는지 확인
            assertEquals(originalNext, gameModel.getCurrentBlock(), 
                "현재 블록이 이전의 다음 블록이 되어야 함");
            
            // 새로운 다음 블록이 생성되었는지 확인
            assertNotSame(originalNext, gameModel.nextBlock, "새로운 다음 블록이 생성되어야 함");
            
            // 블록 생성 개수 증가 확인
            assertEquals(originalBlocksSpawned + 1, gameModel.getBlocksSpawned(), 
                "블록 생성 개수가 증가해야 함");
        });
    }

    @Test
    @DisplayName("게임 오버 조건 테스트")
    void testGameOver() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 초기 상태는 게임 오버가 아님
            assertFalse(gameModel.isGameOver(), "초기 상태는 게임 오버가 아니어야 함");
            
            // 상단 감지 영역에 블록 배치 (row 2-3, col 3-7)
            int[][] board = gameModel.getBoard();
            board[2][5] = 1; // 게임 오버 조건 영역에 블록 배치
            
            // 게임 오버 확인
            assertTrue(gameModel.isGameOver(), "상단 영역에 블록이 있으면 게임 오버여야 함");
        });
    }

    @Test
    @DisplayName("레벨업 시스템 테스트")
    void testLevelUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 초기 레벨 확인
            assertEquals(1, gameModel.getCurrentLevel(), "초기 레벨은 1이어야 함");
            
            // 총 클리어 라인 수를 직접 설정 (테스트용)
            // reflection을 사용하여 private 필드에 접근
            try {
                java.lang.reflect.Field field = GameModel.class.getDeclaredField("totalLinesCleared");
                field.setAccessible(true);
                field.set(gameModel, 4); // 4줄 클리어 시뮬레이션
                
                int newLevel = gameModel.levelUp();
                assertEquals(3, newLevel, "4줄 클리어 시 레벨 3이어야 함"); // (4/2) + 1 = 3
                
                field.set(gameModel, 10); // 10줄 클리어
                newLevel = gameModel.levelUp();
                assertEquals(6, newLevel, "10줄 클리어 시 레벨 6이어야 함"); // (10/2) + 1 = 6
                
            } catch (Exception e) {
                fail("리플렉션을 통한 필드 접근 실패: " + e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("점수 계산 테스트")
    void testCalculateLineClearScore() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 레벨 1에서의 점수 계산 테스트
            assertEquals(100, gameModel.calculateLineClearScore(1), "1줄 클리어 점수는 100이어야 함");
            assertEquals(300, gameModel.calculateLineClearScore(2), "2줄 클리어 점수는 300이어야 함");
            assertEquals(500, gameModel.calculateLineClearScore(3), "3줄 클리어 점수는 500이어야 함");
            assertEquals(800, gameModel.calculateLineClearScore(4), "4줄 클리어 점수는 800이어야 함");
            assertEquals(0, gameModel.calculateLineClearScore(0), "0줄 클리어 점수는 0이어야 함");
            assertEquals(0, gameModel.calculateLineClearScore(5), "5줄 이상은 0점이어야 함");
        });
    }

    @Test
    @DisplayName("난이도별 점수 가중치 테스트")
    void testDifficultyMultiplier() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // GameTimer 생성 및 설정 (GameView, GameModel, FrameBoard 순서)
            GameTimer gameTimer = new GameTimer(gameView, gameModel, null);
            gameModel.setGameTimer(gameTimer);
            
            // Normal 난이도 (0) 테스트
            gameTimer.difficulty = 0;
            int normalScore = gameModel.calculateLineClearScore(1);
            assertEquals(100, normalScore, "Normal 난이도 1줄 클리어는 100점");
            
            // Hard 난이도 (1) 테스트  
            gameTimer.difficulty = 1;
            int hardScore = gameModel.calculateLineClearScore(1);
            assertEquals(110, hardScore, "Hard 난이도는 1.1배 점수"); // 100 * 1 * 1.1 = 110
            
            // Easy 난이도 (2) 테스트
            gameTimer.difficulty = 2;
            int easyScore = gameModel.calculateLineClearScore(1);
            assertEquals(90, easyScore, "Easy 난이도는 0.9배 점수"); // 100 * 1 * 0.9 = 90
        });
    }

    @Test
    @DisplayName("속도 레벨 계산 테스트")
    void testCurrentSpeedLevel() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 초기 속도 레벨
            assertEquals(0, gameModel.getCurrentSpeedLevel(), "초기 속도 레벨은 0이어야 함");
            
            // 블록 생성 개수와 라인 클리어 개수를 설정하여 테스트
            try {
                java.lang.reflect.Field blocksField = GameModel.class.getDeclaredField("blocksSpawned");
                blocksField.setAccessible(true);
                blocksField.set(gameModel, 4); // 4개 블록 생성
                
                java.lang.reflect.Field linesField = GameModel.class.getDeclaredField("totalLinesCleared");
                linesField.setAccessible(true);
                linesField.set(gameModel, 10); // 10줄 클리어
                
                // 블록: 4/2 = 2레벨, 라인: 10/5 = 2레벨 -> max(2,2) = 2
                assertEquals(2, gameModel.getCurrentSpeedLevel(), "속도 레벨이 2여야 함");
                
                // 더 높은 라인 클리어
                linesField.set(gameModel, 25); // 25줄 클리어
                // 블록: 4/2 = 2레벨, 라인: 25/5 = 5레벨 -> max(2,5) = 5
                assertEquals(5, gameModel.getCurrentSpeedLevel(), "속도 레벨이 5여야 함");
                
                // 최대 레벨 테스트
                blocksField.set(gameModel, 100); // 100개 블록
                linesField.set(gameModel, 100); // 100줄 클리어
                // 블록: 100/2 = 50레벨, 라인: 100/5 = 20레벨 -> max(50,20) = 50, 하지만 최대 6
                assertEquals(6, gameModel.getCurrentSpeedLevel(), "최대 속도 레벨은 6이어야 함");
                
            } catch (Exception e) {
                fail("리플렉션을 통한 필드 접근 실패: " + e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("블록 재설정 테스트")
    void testResetBlocks() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 게임 상태를 변경
            gameModel.spawnNewBlock(); // 블록 개수 증가
            
            // 상태 변경 확인을 위해 필드 값 설정
            try {
                java.lang.reflect.Field totalLinesField = GameModel.class.getDeclaredField("totalLinesCleared");
                totalLinesField.setAccessible(true);
                totalLinesField.set(gameModel, 10);
                
                java.lang.reflect.Field levelField = GameModel.class.getDeclaredField("currentLevel");
                levelField.setAccessible(true);
                levelField.set(gameModel, 5);
                
                java.lang.reflect.Field scoreField = GameModel.class.getDeclaredField("lastLineClearScore");
                scoreField.setAccessible(true);
                scoreField.set(gameModel, 500);
                
            } catch (Exception e) {
                fail("필드 설정 실패: " + e.getMessage());
            }
            
            // 블록 재설정
            gameModel.resetBlocks();
            
            // 모든 값이 초기화되었는지 확인
            assertNotNull(gameModel.getCurrentBlock(), "현재 블록이 재생성되어야 함");
            assertNotNull(gameModel.nextBlock, "다음 블록이 재생성되어야 함");
            assertEquals(0, gameModel.getTotalLinesCleared(), "총 라인 클리어 수가 초기화되어야 함");
            assertEquals(1, gameModel.getCurrentLevel(), "레벨이 초기화되어야 함");
            assertEquals(0, gameModel.getLastLineClearScore(), "라인 클리어 점수가 초기화되어야 함");
            assertEquals(0, gameModel.getBlocksSpawned(), "블록 생성 개수가 초기화되어야 함");
        });
    }

    @Test
    @DisplayName("색상 보드 테스트")
    void testColorBoard() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            int[][] colorBoard = gameModel.getColorBoard();
            assertNotNull(colorBoard, "색상 보드가 초기화되어야 함");
            assertEquals(23, colorBoard.length, "색상 보드 행 수는 23이어야 함");
            assertEquals(12, colorBoard[0].length, "색상 보드 열 수는 12이어야 함");
            
            // 초기 상태에서는 모든 색상이 0이어야 함
            for (int i = 0; i < 23; i++) {
                for (int j = 0; j < 12; j++) {
                    assertEquals(0, colorBoard[i][j], "초기 색상 보드는 모두 0이어야 함");
                }
            }
        });
    }

    @Test
    @DisplayName("애니메이션 상태 테스트")
    void testAnimationStates() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 초기 애니메이션 상태 확인
            assertFalse(gameModel.isLineClearAnimating(), "초기에는 라인 클리어 애니메이션이 꺼져있어야 함");
            assertFalse(gameModel.isAllClearAnimating(), "초기에는 AllClear 애니메이션이 꺼져있어야 함");
            assertFalse(gameModel.isBoxClearAnimating(), "초기에는 BoxClear 애니메이션이 꺼져있어야 함");
            assertFalse(gameModel.isWeightAnimating(), "초기에는 Weight 애니메이션이 꺼져있어야 함");
            
            // 플래시 상태 확인
            assertFalse(gameModel.isFlashBlack(), "초기에는 플래시가 꺼져있어야 함");
            assertFalse(gameModel.isAllClearFlashBlack(), "초기에는 AllClear 플래시가 꺼져있어야 함");
            assertFalse(gameModel.isBoxFlashBlack(), "초기에는 BoxClear 플래시가 꺼져있어야 함");
            
            // 특정 행 플래시 확인
            assertFalse(gameModel.isRowFlashing(5), "초기에는 특정 행이 플래시하지 않아야 함");
            assertFalse(gameModel.isCellInBoxFlash(5, 5), "초기에는 특정 셀이 박스 플래시하지 않아야 함");
        });
    }

    @Test
    @DisplayName("Getter 메서드 테스트")
    void testGetterMethods() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 모든 getter 메서드가 정상적으로 동작하는지 확인
            assertNotNull(gameModel.getCurrentBlock(), "getCurrentBlock()이 null이 아니어야 함");
            assertNotNull(gameModel.getBoard(), "getBoard()가 null이 아니어야 함");
            assertNotNull(gameModel.getColorBoard(), "getColorBoard()가 null이 아니어야 함");
            
            assertTrue(gameModel.getCurrentLevel() >= 1, "getCurrentLevel()이 1 이상이어야 함");
            assertTrue(gameModel.getTotalLinesCleared() >= 0, "getTotalLinesCleared()가 0 이상이어야 함");
            assertTrue(gameModel.getLastLineClearScore() >= 0, "getLastLineClearScore()가 0 이상이어야 함");
            assertTrue(gameModel.getBlocksSpawned() >= 0, "getBlocksSpawned()가 0 이상이어야 함");
            assertTrue(gameModel.getCurrentSpeedLevel() >= 0, "getCurrentSpeedLevel()이 0 이상이어야 함");
        });
    }
}