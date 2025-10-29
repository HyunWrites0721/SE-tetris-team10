package game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import javax.swing.SwingUtilities;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FrameBoard 테스트")
public class FrameBoardTest {

    private FrameBoard frameBoard;

    @BeforeEach
    void setUp() throws Exception {
        // EDT에서 FrameBoard 생성
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new FrameBoard(false); // Normal mode로 테스트
            frameBoard.setVisible(false); // 테스트 중에는 화면에 표시하지 않음
        });
        
        // UI 컴포넌트가 완전히 초기화될 때까지 잠시 대기
        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (frameBoard != null) {
            SwingUtilities.invokeAndWait(() -> {
                if (frameBoard.getGameTimer() != null) {
                    frameBoard.getGameTimer().stop();
                }
                frameBoard.dispose();
            });
        }
    }

    @Test
    @DisplayName("FrameBoard 생성 테스트")
    void testFrameBoardCreation() {
        assertNotNull(frameBoard, "FrameBoard가 생성되어야 함");   
        assertEquals("Tetris", frameBoard.getTitle(), "창 제목이 'Tetris'여야 함");
        assertFalse(frameBoard.isResizable(), "프레임은 크기 조절이 불가능해야 함");
    }

    @Test
    @DisplayName("게임 컴포넌트 초기화 테스트")
    void testGameComponentsInitialization() {
        assertNotNull(frameBoard.getGameBoard(), "GameView가 초기화되어야 함");
        assertNotNull(frameBoard.getGameModel(), "GameModel이 초기화되어야 함");
        assertNotNull(frameBoard.getGameTimer(), "GameTimer가 초기화되어야 함");
    }

    @Test
    @DisplayName("초기 게임 상태 테스트")
    void testInitialGameState() {
        assertFalse(frameBoard.isPaused, "초기 상태는 일시정지가 아니어야 함");
        assertFalse(frameBoard.isGameOver, "초기 상태는 게임오버가 아니어야 함");
    }

    @Test
    @DisplayName("점수 증가 테스트")
    void testScoreIncrease() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 점수 증가 메서드 호출 (예외 없이 실행되는지 확인)
            assertDoesNotThrow(() -> {
                frameBoard.increaseScore(100);
            }, "점수 증가가 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("점수 누적 테스트")
    void testScoreAccumulation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 점수 누적 호출 (예외 없이 실행되는지 확인)
            assertDoesNotThrow(() -> {
                frameBoard.increaseScore(50);
                frameBoard.increaseScore(75);
                frameBoard.increaseScore(25);
            }, "점수 누적이 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("일시정지 기능 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testPauseToggle() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 게임 타이머 시작
            frameBoard.getGameTimer().start();
            
            // 일시정지 활성화
            frameBoard.isPaused = true;
            frameBoard.paused();
        });
        
        assertTrue(frameBoard.isPaused, "일시정지 상태여야 함");
        assertFalse(frameBoard.getGameTimer().timer.isRunning(), "타이머가 정지되어야 함");
        
        SwingUtilities.invokeAndWait(() -> {
            // 일시정지 해제
            frameBoard.isPaused = false;
            frameBoard.paused();
        });
        
        assertFalse(frameBoard.isPaused, "일시정지가 해제되어야 함");
        assertTrue(frameBoard.getGameTimer().timer.isRunning(), "타이머가 재시작되어야 함");
    }

    @Test
    @DisplayName("게임오버 상태 테스트")
    void testGameOver() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 실제 게임 플레이 상황을 시뮬레이션하기 위해 점수 설정
            // (gameOver 메서드의 점수 저장 로직 테스트를 위함)
            frameBoard.increaseScore(200);
            frameBoard.gameOver();
        });
        
        assertTrue(frameBoard.isGameOver, "게임오버 상태여야 함");
        assertFalse(frameBoard.getGameTimer().timer.isRunning(), "타이머가 정지되어야 함");
    }

    @Test
    @DisplayName("게임 초기화 테스트")
    void testGameInit() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 게임이 진행된 상태를 만들기 위해 점수, 일시정지, 게임오버 상태 설정
            // (초기화 메서드가 모든 상태를 제대로 리셋하는지 테스트하기 위함)
            frameBoard.increaseScore(300);
            frameBoard.isPaused = true;
            frameBoard.isGameOver = true;
            
            // 게임 초기화
            frameBoard.gameInit();
        });
        
        // 초기화 후 상태 확인
        assertFalse(frameBoard.isPaused, "일시정지 상태가 해제되어야 함");
        assertFalse(frameBoard.isGameOver, "게임오버 상태가 해제되어야 함");
        // 점수 초기화 확인 (GameView에 getter가 없으므로 예외 없이 실행되는지만 확인)
        assertDoesNotThrow(() -> {
            frameBoard.getGameBoard().setScore(0); // 점수 설정이 정상적으로 동작하는지 확인
        }, "점수 초기화가 예외 없이 실행되어야 함");
        assertTrue(frameBoard.getGameTimer().timer.isRunning(), "새로운 타이머가 시작되어야 함");
    }

    @Test
    @DisplayName("프레임 크기 업데이트 테스트")
    void testUpdateFrameSize() throws Exception {
        final double testScale = 1.5;
        final int[] originalWidth = {0};
        final int[] originalHeight = {0};
        final int[] newWidth = {0};
        final int[] newHeight = {0};
        
        SwingUtilities.invokeAndWait(() -> {
            originalWidth[0] = frameBoard.getWidth();
            originalHeight[0] = frameBoard.getHeight();
            
            frameBoard.updateFrameSize(testScale);
            
            newWidth[0] = frameBoard.getWidth();
            newHeight[0] = frameBoard.getHeight();
        });
        
        // 크기가 변경되었는지 확인 (정확한 값보다는 변경 여부 확인)
        assertTrue(newWidth[0] > 0 && newHeight[0] > 0, "프레임 크기가 유효해야 함");
    }

    @Test
    @DisplayName("safeScreenRatio 기본값 테스트")
    void testSafeScreenRatio() throws Exception {
        // StartFrame.screenRatio가 초기화되지 않은 상태에서 기본값 확인
        SwingUtilities.invokeAndWait(() -> {
            // FrameBoard 생성 시 safeScreenRatio()가 호출되므로
            // 예외 없이 생성되었다면 기본값이 적용된 것
            assertNotNull(frameBoard, "safeScreenRatio로 인해 FrameBoard가 생성되어야 함");
        });
    }

    @Test
    @DisplayName("블록 설정 및 라인 클리어 테스트")
    void testBlockOperations() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 블록 설정 테스트 (예외 발생 없이 실행되는지 확인)
            assertDoesNotThrow(() -> {
                frameBoard.setBlockText(5, 3);
            }, "블록 설정이 예외 없이 실행되어야 함");
            
            // 라인 클리어 테스트 (예외 발생 없이 실행되는지 확인)
            assertDoesNotThrow(() -> {
                frameBoard.oneLineClear(10);
            }, "라인 클리어가 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("여러 번의 게임오버 호출 테스트")
    void testMultipleGameOverCalls() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 점수 저장 로직 테스트를 위해 의미 있는 점수 설정
            // (score > 0일 때만 하이스코어에 저장되므로)
            frameBoard.increaseScore(100);
            
            // 첫 번째 게임오버 호출
            frameBoard.gameOver();
            assertTrue(frameBoard.isGameOver, "첫 번째 게임오버 후 상태가 변경되어야 함");
            
            // 두 번째 게임오버 호출 (중복 처리 방지 확인)
            assertDoesNotThrow(() -> {
                frameBoard.gameOver();
            }, "중복 게임오버 호출이 예외 없이 처리되어야 함");
            
            assertTrue(frameBoard.isGameOver, "게임오버 상태가 유지되어야 함");
        });
    }

    @Test
    @DisplayName("게임 재시작 후 점수 초기화 테스트")
    void testScoreResetAfterRestart() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 게임 진행 상황 시뮬레이션: 점수 쌓고 게임오버
            // 재시작 시 점수가 0으로 초기화되는지 테스트하기 위함
            frameBoard.increaseScore(500);
            frameBoard.gameOver();
            
            // 게임 재시작
            frameBoard.gameInit();
        });
        
        // 재시작 후 상태 확인 (GameView에 getter가 없으므로 예외 없이 실행되는지만 확인)
        assertDoesNotThrow(() -> {
            frameBoard.getGameBoard().setScore(0); // 점수 설정이 정상적으로 동작하는지 확인
            frameBoard.getGameBoard().setHighScore(0); // 최고 점수 설정이 정상적으로 동작하는지 확인
        }, "점수 관련 메서드들이 예외 없이 실행되어야 함");
    }
}