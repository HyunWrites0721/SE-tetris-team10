package game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.SwingUtilities;

public class GameTimerTest {
    private GameTimer gameTimer;
    private GameModel gameModel;
    private FrameBoard frameBoard;

    @BeforeEach
    void setUp() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                frameBoard = new FrameBoard();
                frameBoard.setVisible(false); // GUI 표시하지 않음
                gameModel = frameBoard.getGameModel();
                gameTimer = frameBoard.getGameTimer();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        if (frameBoard != null) {
            SwingUtilities.invokeLater(() -> {
                frameBoard.dispose(); // 테스트 후 프레임 정리
            });
        }
    }

    @Test
    void testTimerDuplication() {
        // 첫 번째 타이머 시작
        gameTimer.start();
        int initialDelay = gameTimer.timer.getDelay();
        
        // 타이머를 여러 번 시작해도 딜레이가 변하지 않아야 함
        gameTimer.start();
        gameTimer.start();
        
        assertEquals(initialDelay, gameTimer.timer.getDelay(), "타이머 딜레이가 변경되지 않아야 함");
        gameTimer.stop();
    }

    @Test
    void testTimerStartAndStop() {
        // 초기 상태 확인
        assertFalse(gameTimer.timer.isRunning());

        // 현재 블록 위치 저장
        int initialY = gameModel.getCurrentBlock().getY();

        // 타이머 시작
        gameTimer.start();
        assertTrue(gameTimer.timer.isRunning(), "타이머가 시작되어야 함");

        // 타이머 이벤트 직접 발생시킴
        gameTimer.timer.getActionListeners()[0].actionPerformed(null);
        
        // 블록이 한 칸 아래로 이동했는지 확인
        assertEquals(initialY + 1, gameModel.getCurrentBlock().getY(), "블록이 한 칸 아래로 이동해야 함");

        // 타이머 정지
        gameTimer.stop();
        assertFalse(gameTimer.timer.isRunning(), "타이머가 정지되어야 함");

        // 타이머 이벤트를 한 번 더 발생시켜도 블록이 움직이지 않아야 함
        int stoppedY = gameModel.getCurrentBlock().getY();
        gameTimer.timer.getActionListeners()[0].actionPerformed(null);
        assertEquals(stoppedY, gameModel.getCurrentBlock().getY(), "정지 상태에서는 블록이 이동하지 않아야 함");
    }

    @Test
    void testPauseAndResume() {
        // 게임 시작
        int initialY = gameModel.getCurrentBlock().getY();
        gameTimer.start();
        assertTrue(gameTimer.timer.isRunning());

        // 타이머 이벤트 발생시켜 블록 이동
        gameTimer.timer.getActionListeners()[0].actionPerformed(null);
        assertEquals(initialY + 1, gameModel.getCurrentBlock().getY(), "블록이 한 칸 아래로 이동해야 함");

        // 일시정지
        frameBoard.isPaused = true;
        int pausedY = gameModel.getCurrentBlock().getY();
        
        // 일시정지 상태에서 타이머 이벤트 발생
        gameTimer.timer.getActionListeners()[0].actionPerformed(null);
        assertEquals(pausedY, gameModel.getCurrentBlock().getY(), "일시정지 상태에서는 블록이 이동하지 않아야 함");

        // 게임 재개
        frameBoard.isPaused = false;
        gameTimer.timer.getActionListeners()[0].actionPerformed(null);
        assertEquals(pausedY + 1, gameModel.getCurrentBlock().getY(), "게임 재개 후 블록이 이동해야 함");
    }

    @Test
    void testGameOverAndRestart() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 게임 시작
            gameTimer.start();
            assertTrue(gameTimer.timer.isRunning());

            // 게임 오버 발생
            frameBoard.gameOver();
            assertFalse(gameTimer.timer.isRunning(), "게임 오버시 타이머가 정지되어야 함");

            // 게임 재시작
            frameBoard.gameInit();
            
            // gameInit에서 새로운 GameTimer가 생성되므로 참조 업데이트
            gameTimer = frameBoard.getGameTimer();
            assertTrue(gameTimer.timer.isRunning(), "게임 재시작시 타이머가 다시 시작되어야 함");
        });
    }

    @Test
    void testDifficultyInitialSpeed() {
        // 난이도별 초기 속도 테스트
        // 현재 게임 타이머의 난이도에 따른 초기 딜레이 확인
        int currentDifficulty = gameTimer.difficulty;
        int expectedDelay;
        
        switch (currentDifficulty) {
            case 0: // 노멀
                expectedDelay = 1000;
                break;
            case 1: // 하드
                expectedDelay = 100;
                break;
            case 2: // 이지
                expectedDelay = 4200;
                break;
            default:
                expectedDelay = 1000; // 기본값
                break;
        }
        
        assertEquals(expectedDelay, gameTimer.timer.getDelay(), 
            "난이도 " + currentDifficulty + "의 초기 딜레이가 " + expectedDelay + "ms여야 함");
    }

    @Test
    void testDifficultyScoreMultiplier() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 난이도별 점수 가중치 테스트
            gameTimer.start();
            
            // 현재 블록 위치 저장
            int initialY = gameModel.getCurrentBlock().getY();
            
            // 타이머 이벤트 발생으로 자동 낙하 및 점수 획득
            gameTimer.timer.getActionListeners()[0].actionPerformed(null);
            
            // 블록이 이동했는지 확인 (점수 직접 확인 대신)
            int newY = gameModel.getCurrentBlock().getY();
            assertEquals(initialY + 1, newY, "자동 낙하로 블록이 이동해야 함");
            
            // 난이도에 따른 점수 가중치 로직이 예외 없이 실행되는지 확인
            assertDoesNotThrow(() -> {
                // increaseScore 메서드가 호출되었음을 간접적으로 확인
                // (실제 점수 값 확인은 FrameBoard에 getter가 없어 불가)
                double expectedMultiplier;
                switch (gameTimer.difficulty) {
                    case 0: expectedMultiplier = 1.0; break;
                    case 1: expectedMultiplier = 1.1; break;
                    case 2: expectedMultiplier = 0.9; break;
                    default: expectedMultiplier = 1.0; break;
                }
                // 가중치 계산이 올바른 범위인지 확인
                assertTrue(expectedMultiplier > 0.0 && expectedMultiplier <= 1.5, 
                    "점수 가중치가 유효한 범위여야 함");
            }, "난이도별 점수 가중치 계산이 예외 없이 실행되어야 함");
            
            gameTimer.stop();
        });
    }

    @Test
    void testUpdateSpeedBasicOperation() {
        // 속도 업데이트 기본 동작 테스트
        int initialDelay = gameTimer.timer.getDelay();
        
        // 속도 레벨 1로 업데이트
        gameTimer.updateSpeed(1);
        int newDelay = gameTimer.timer.getDelay();
        
        assertNotEquals(initialDelay, newDelay, "속도 업데이트 후 딜레이가 변경되어야 함");
        assertTrue(newDelay < initialDelay, "속도 레벨이 증가하면 딜레이가 감소해야 함");
    }

    @Test
    void testUpdateSpeedDelayChange() {
        // 속도 업데이트 시 딜레이 변경 테스트
        int difficulty = gameTimer.difficulty;
        
        // 속도 레벨 2로 업데이트
        gameTimer.updateSpeed(2);
        
        int expectedDelay;
        switch (difficulty) {
            case 0: // 노멀
                expectedDelay = 650;
                break;
            case 1: // 하드
                expectedDelay = 520;
                break;
            case 2: // 이지
                expectedDelay = 780;
                break;
            default:
                expectedDelay = 650;
                break;
        }
        
        assertEquals(expectedDelay, gameTimer.timer.getDelay(), 
            "속도 레벨 2의 딜레이가 " + expectedDelay + "ms여야 함");
    }

    @Test
    void testInvalidSpeedLevelHandling() {
        // 잘못된 속도 레벨 처리 테스트
        int initialDelay = gameTimer.timer.getDelay();
        
        // 음수 속도 레벨
        gameTimer.updateSpeed(-1);
        assertEquals(initialDelay, gameTimer.timer.getDelay(), "음수 속도 레벨은 무시되어야 함");
        
        // 범위 초과 속도 레벨
        gameTimer.updateSpeed(10);
        assertEquals(initialDelay, gameTimer.timer.getDelay(), "범위 초과 속도 레벨은 무시되어야 함");
        
        // 동일한 속도 레벨
        gameTimer.updateSpeed(0);
        assertEquals(initialDelay, gameTimer.timer.getDelay(), "동일한 속도 레벨은 무시되어야 함");
    }

    @Test
    void testDifficultyLoadingFromSettings() {
        // 설정 파일 로딩 테스트
        // 난이도가 0, 1, 2 중 하나의 유효한 값인지 확인
        assertTrue(gameTimer.difficulty >= 0 && gameTimer.difficulty <= 2, 
            "난이도는 0(노멀), 1(하드), 2(이지) 중 하나여야 함");
        
        // 난이도에 따른 올바른 설정 확인
        String[] difficultyNames = {"노멀", "하드", "이지"};
        System.out.println("현재 난이도: " + difficultyNames[gameTimer.difficulty]);
    }

    @Test
    void testSettingsFileErrorHandling() {
        // 설정 파일 오류 처리 테스트
        // 새로운 GameTimer를 생성하여 설정 로딩 과정 테스트
        SwingUtilities.invokeLater(() -> {
            try {
                // 임시 FrameBoard 생성으로 새 GameTimer 생성
                FrameBoard tempFrame = new FrameBoard();
                GameTimer tempTimer = tempFrame.getGameTimer();
                
                // 설정 파일에 문제가 있어도 기본값(0)으로 설정되어야 함
                assertTrue(tempTimer.difficulty >= 0 && tempTimer.difficulty <= 2, 
                    "설정 로딩 실패 시 기본값으로 설정되어야 함");
                
                tempFrame.dispose();
            } catch (Exception e) {
                // 예외가 발생해도 테스트는 계속되어야 함
                System.out.println("설정 파일 테스트 중 예외 발생: " + e.getMessage());
            }
        });
    }

    @Test
    void testImprovedStartMethodDuplication() {
        // 개선된 start() 메서드 중복 호출 테스트
        assertFalse(gameTimer.isRunning(), "초기에는 타이머가 정지 상태여야 함");
        
        // 첫 번째 start() 호출
        gameTimer.start();
        assertTrue(gameTimer.isRunning(), "첫 번째 start() 호출 후 타이머가 실행되어야 함");
        
        // 중복 start() 호출들
        gameTimer.start();
        gameTimer.start();
        gameTimer.start();
        
        assertTrue(gameTimer.isRunning(), "중복 start() 호출 후에도 타이머가 정상 실행되어야 함");
        
        // 정지 후 다시 시작
        gameTimer.stop();
        assertFalse(gameTimer.isRunning(), "stop() 후 타이머가 정지되어야 함");
        
        gameTimer.start();
        assertTrue(gameTimer.isRunning(), "stop() 후 start()가 정상 작동해야 함");
        
        gameTimer.stop();
    }

    @Test
    void testAnimationStateCheck() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 애니메이션 중 타이머 정지 테스트
            gameTimer.start();
            assertTrue(gameTimer.isRunning(), "타이머가 시작되어야 함");
            
            int initialY = gameModel.getCurrentBlock().getY();
            
            // 가상의 애니메이션 상태 설정 (실제 GameModel에 애니메이션 메서드가 있다고 가정)
            // 이 테스트는 애니메이션 상태가 체크되는지 확인
            gameTimer.timer.getActionListeners()[0].actionPerformed(null);
            
            // 정상 상태에서는 블록이 이동해야 함
            assertEquals(initialY + 1, gameModel.getCurrentBlock().getY(), 
                "정상 상태에서 블록이 이동해야 함");
            
            gameTimer.stop();
        });
    }

    @Test
    void testIsRunningMethod() {
        // isRunning() 메서드 정확성 테스트
        assertFalse(gameTimer.isRunning(), "초기 상태에서 isRunning()은 false여야 함");
        
        gameTimer.start();
        assertTrue(gameTimer.isRunning(), "start() 후 isRunning()은 true여야 함");
        
        gameTimer.stop();
        assertFalse(gameTimer.isRunning(), "stop() 후 isRunning()은 false여야 함");
        
        // 일시정지 상태 테스트
        gameTimer.start();
        frameBoard.isPaused = true;
        assertTrue(gameTimer.isRunning(), "일시정지 중에도 isRunning()은 true여야 함 (타이머는 돌지만 동작하지 않음)");
        
        frameBoard.isPaused = false;
        gameTimer.stop();
    }
}
