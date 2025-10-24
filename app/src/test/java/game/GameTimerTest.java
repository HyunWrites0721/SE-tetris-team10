package game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;

public class GameTimerTest {
    private GameTimer gameTimer;
    private GameView gameView;
    private GameModel gameModel;
    private FrameBoard frameBoard;

    @BeforeEach
    void setUp() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                frameBoard = new FrameBoard();
                frameBoard.setVisible(false); // GUI 표시하지 않음
                gameView = frameBoard.getGameBoard();
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
    void testGameOverAndRestart() {
        // 게임 시작
        gameTimer.start();
        assertTrue(gameTimer.timer.isRunning());

        // 게임 오버 발생
        frameBoard.gameOver();
        assertFalse(gameTimer.timer.isRunning(), "게임 오버시 타이머가 정지되어야 함");

        // 게임 재시작
        frameBoard.gameInit();
        assertTrue(gameTimer.timer.isRunning(), "게임 재시작시 타이머가 다시 시작되어야 함");
    }
}
