package game;

import java.awt.Window;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import blocks.Block;
import blocks.IBlock;

@DisplayName("GameView 테스트")
public class GameViewTest {

    private GameView gameView;

    @BeforeEach
    void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            try {
                gameView = new GameView(false);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @AfterEach
    void tearDown() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (gameView != null) {
                gameView = null;
            }
            for (Window window : Window.getWindows()) {
                if (window.isDisplayable()) {
                    window.dispose();
                }
            }
        });
    }

    @Test
    @DisplayName("GameView 생성 및 초기화 테스트")
    void testGameViewCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(gameView, "GameView가 생성되어야 함");
            
            // 기본 상수값들 확인
            assertEquals(20, gameView.ROWS, "ROWS는 20이어야 함");
            assertEquals(10, gameView.COLS, "COLS는 10이어야 함");
            // BASE_CELL_SIZE and MARGIN were removed; ensure CELL_SIZE is reasonable
            assertEquals(30, gameView.CELL_SIZE, "CELL_SIZE는 30이어야 함");
            assertTrue(gameView.CELL_SIZE >= 15, "CELL_SIZE는 최소 15 이상이어야 함");
            
            // NEXT 영역 크기 확인
            assertEquals(6, gameView.NEXT_ROWS, "NEXT_ROWS는 6이어야 함");
            assertEquals(6, gameView.NEXT_COLS, "NEXT_COLS는 6이어야 함");
            
            // SCORE 영역 크기 확인 (기본 구현에서 SCORE 패널의 높이는 4 * CELL_SIZE)
            assertEquals(6, gameView.NEXT_ROWS, "NEXT_ROWS는 6이어야 함");
            assertEquals(6, gameView.NEXT_COLS, "NEXT_COLS는 6이어야 함");
            
            // 기본 스케일 확인
            assertEquals(1.0, gameView.scale, 0.001, "기본 스케일은 1.0이어야 함");
        });
    }

    @Test
    @DisplayName("점수 설정 테스트")
    void testScoreSetting() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 점수 설정 테스트
            gameView.setScore(1500);
            // GameView는 점수를 getter로 제공하지 않으므로, 예외 없이 실행되는지만 확인
            assertDoesNotThrow(() -> {
                gameView.setScore(2500);
                gameView.setScore(0);
                gameView.setScore(-100); // 음수도 허용하는지 확인
            }, "점수 설정이 예외 없이 실행되어야 함");
            
            // 최고점수 설정 테스트
            assertDoesNotThrow(() -> {
                gameView.setHighScore(5000);
                gameView.setHighScore(0);
                gameView.setHighScore(-50); // 음수도 허용하는지 확인
            }, "최고점수 설정이 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("블록 설정 테스트")
    void testBlockSetting() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // IBlock 생성 (테스트용)
            Block testBlock = new IBlock();
            
            // 떨어지는 블록 설정 테스트
            assertDoesNotThrow(() -> {
                gameView.setFallingBlock(testBlock);
                gameView.setFallingBlock(null); // null 블록도 허용하는지 확인
            }, "떨어지는 블록 설정이 예외 없이 실행되어야 함");
            
            // 다음 블록 설정 테스트
            assertDoesNotThrow(() -> {
                gameView.setNextBlock(testBlock);
                gameView.setNextBlock(null); // null 블록도 허용하는지 확인
            }, "다음 블록 설정이 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("스케일 변환 테스트")
    void testScaleConversion() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 기본 값 저장
            int originalCellSize = gameView.CELL_SIZE;
            
            // 스케일 1.5로 변경
            assertDoesNotThrow(() -> {
                gameView.convertScale(1.5);
            }, "스케일 변환이 예외 없이 실행되어야 함");
            
            // 스케일 변경 후 값 확인
            assertEquals(1.5, gameView.scale, 0.001, "스케일이 1.5로 설정되어야 함");
            
            // CELL_SIZE가 변경되었는지 확인 (정확한 값보다는 변경 여부 확인)
            assertNotEquals(originalCellSize, gameView.CELL_SIZE, "CELL_SIZE가 변경되어야 함");
            
            // 다양한 스케일 값 테스트
            assertDoesNotThrow(() -> {
                gameView.convertScale(0.8);
                gameView.convertScale(2.0);
                gameView.convertScale(0.5);
            }, "다양한 스케일 값이 예외 없이 처리되어야 함");
            
            // 최소 셀 크기 보장 확인
            gameView.convertScale(0.1); // 매우 작은 스케일
            assertTrue(gameView.CELL_SIZE >= 15, "최소 셀 크기 15가 보장되어야 함");
        });
    }
}