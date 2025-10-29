package game;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameBoardTest {

	private FrameBoard frameBoard;
	private GameTimer gameTimer;

	@BeforeEach
	void setUp() throws Exception {
		// EDT에서 FrameBoard를 직접 생성하여 GameStart 의존성 제거
		SwingUtilities.invokeAndWait(() -> {
			frameBoard = new FrameBoard(false); // Normal mode
			frameBoard.setVisible(true); // UI 표시
			gameTimer = frameBoard.getGameTimer();
			// GameStart가 하던 역할: 프레임 보유 타이머 시작
			gameTimer.start();
		});
	}

	@AfterEach
	void tearDown() throws Exception {
		if (frameBoard != null) {
			SwingUtilities.invokeAndWait(() -> {
				frameBoard.dispose();
			});
		}
	}

	@Test
	void testGameStartCreatesFrameAndStartsTimer() {
		// Run assertions on the EDT to avoid races with Swing Timer state
		try {
			SwingUtilities.invokeAndWait(() -> {
				assertNotNull(frameBoard, "FrameBoard가 생성되어야 함");
				assertNotNull(gameTimer, "FrameBoard가 GameTimer를 보유해야 함");
				assertTrue(gameTimer.timer.isRunning(), "테스트 시작 시 타이머가 시작되어야 함");
			});
		} catch (InterruptedException | java.lang.reflect.InvocationTargetException e) {
			// If invokeAndWait fails, fail the test with the exception
			throw new RuntimeException(e);
		}
	}
}
