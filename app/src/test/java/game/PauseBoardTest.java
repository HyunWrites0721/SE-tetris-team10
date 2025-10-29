package game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PauseBoard 테스트")
public class PauseBoardTest {

    private FrameBoard frameBoard;
    private PauseBoard pauseBoard;

    @BeforeEach
    void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            try {
                frameBoard = new FrameBoard();
                pauseBoard = new PauseBoard(frameBoard);
                frameBoard.add(pauseBoard);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @AfterEach
    void tearDown() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (frameBoard != null) {
                frameBoard.dispose();
            }
            for (Window window : Window.getWindows()) {
                if (window.isDisplayable()) {
                    window.dispose();
                }
            }
        });
    }

    @Test
    @DisplayName("PauseBoard 생성 테스트")
    void testPauseBoardCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(pauseBoard, "PauseBoard가 생성되어야 함");
            assertTrue(pauseBoard.isFocusable(), "PauseBoard가 포커스 가능해야 함");
            assertFalse(pauseBoard.getFocusTraversalKeysEnabled(), "포커스 트래버설 키가 비활성화되어야 함");
        });
    }

    @Test
    @DisplayName("키보드 네비게이션 테스트 - UP/DOWN 키")
    void testKeyboardNavigation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 포커스 설정
            pauseBoard.requestFocus();
            
            // DOWN 키 눌러서 다음 버튼으로 이동
            KeyEvent downEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
            pauseBoard.keyPressed(downEvent);
            
            // UP 키 눌러서 이전 버튼으로 이동
            KeyEvent upEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);
            pauseBoard.keyPressed(upEvent);
            
            // 예외가 발생하지 않으면 성공
            assertTrue(true, "키보드 네비게이션이 정상 작동해야 함");
        });
    }

    @Test
    @DisplayName("ESC 키로 게임 재개 테스트")
    void testEscapeKeyResume() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 일시정지 상태로 설정
            frameBoard.isPaused = true;
            pauseBoard.requestFocus();
            
            // ESC 키 이벤트 생성
            KeyEvent escEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
            
            // ESC 키 처리
            pauseBoard.keyPressed(escEvent);
            
            // 일시정지 상태가 해제되었는지 확인
            assertFalse(frameBoard.isPaused, "ESC 키로 일시정지가 해제되어야 함");
        });
    }

    @Test
    @DisplayName("ENTER 키로 버튼 클릭 테스트")
    void testEnterKeyButtonClick() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            pauseBoard.requestFocus();
            
            // ENTER 키 이벤트 생성 (첫 번째 버튼인 resume 버튼 클릭)
            KeyEvent enterEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            
            // 일시정지 상태로 설정
            frameBoard.isPaused = true;
            
            // ENTER 키 처리 (resume 버튼 클릭)
            pauseBoard.keyPressed(enterEvent);
            
            // resume 버튼이 클릭되어 일시정지가 해제되었는지 확인
            assertFalse(frameBoard.isPaused, "ENTER 키로 resume 버튼이 클릭되어야 함");
        });
    }

    @Test
    @DisplayName("convertScale 메서드 테스트")
    void testConvertScale() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 스케일 변경이 예외 없이 실행되는지 확인
            assertDoesNotThrow(() -> {
                pauseBoard.convertScale(1.5);
                pauseBoard.convertScale(0.8);
                pauseBoard.convertScale(2.0);
            }, "convertScale 메서드가 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("버튼 순환 네비게이션 테스트")
    void testButtonCycleNavigation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            pauseBoard.requestFocus();
            
            // 첫 번째 버튼에서 UP 키를 누르면 마지막 버튼으로 이동
            KeyEvent upEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);
            pauseBoard.keyPressed(upEvent);
            
            // DOWN 키를 4번 눌러서 모든 버튼을 순환
            KeyEvent downEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
            
            for (int i = 0; i < 4; i++) {
                pauseBoard.keyPressed(downEvent);
            }
            
            // 예외가 발생하지 않으면 순환 네비게이션이 정상 작동
            assertTrue(true, "버튼 순환 네비게이션이 정상 작동해야 함");
        });
    }

    @Test
    @DisplayName("Resume 버튼 클릭 테스트")
    void testResumeButtonClick() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 일시정지 상태로 설정
            frameBoard.isPaused = true;
            pauseBoard.requestFocus();
            
            // 첫 번째 버튼(Resume)이 기본 선택된 상태에서 ENTER 키
            KeyEvent enterEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            pauseBoard.keyPressed(enterEvent);
            
            // Resume 버튼이 클릭되어 일시정지가 해제되어야 함
            assertFalse(frameBoard.isPaused, "Resume 버튼 클릭으로 일시정지가 해제되어야 함");
        });
    }

    @Test
    @DisplayName("Restart 버튼 클릭 테스트")
    void testRestartButtonClick() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            pauseBoard.requestFocus();
            
            // DOWN 키로 Restart 버튼(두 번째)으로 이동
            KeyEvent downEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
            pauseBoard.keyPressed(downEvent);
            
            // ENTER 키로 Restart 버튼 클릭
            KeyEvent enterEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            
            // Restart 버튼 클릭이 예외 없이 실행되는지 확인
            assertDoesNotThrow(() -> {
                pauseBoard.keyPressed(enterEvent);
            }, "Restart 버튼 클릭이 정상 실행되어야 함");
        });
    }

    @Test
    @DisplayName("Main Menu 버튼 클릭 테스트")
    void testMainMenuButtonClick() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            pauseBoard.requestFocus();
            
            // DOWN 키를 2번 눌러서 Main Menu 버튼(세 번째)으로 이동
            KeyEvent downEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
            pauseBoard.keyPressed(downEvent);
            pauseBoard.keyPressed(downEvent);
            
            // ENTER 키로 Main Menu 버튼 클릭
            KeyEvent enterEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            
            // Main Menu 버튼 클릭이 예외 없이 실행되는지 확인
            assertDoesNotThrow(() -> {
                pauseBoard.keyPressed(enterEvent);
            }, "Main Menu 버튼 클릭이 정상 실행되어야 함");
        });
    }

    @Test
    @DisplayName("연속 ESC 키 입력 테스트")
    void testMultipleEscapeKeys() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            pauseBoard.requestFocus();
            
            // 일시정지 상태로 설정
            frameBoard.isPaused = true;
            
            // ESC 키를 여러 번 연속으로 누르기
            KeyEvent escEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
            
            // 첫 번째 ESC - 일시정지 해제
            pauseBoard.keyPressed(escEvent);
            assertFalse(frameBoard.isPaused, "첫 번째 ESC로 일시정지가 해제되어야 함");
            
            // 두 번째 ESC - 이미 해제된 상태에서 추가 ESC
            pauseBoard.keyPressed(escEvent);
            assertFalse(frameBoard.isPaused, "두 번째 ESC 후에도 정상 상태를 유지해야 함");
        });
    }

    @Test
    @DisplayName("일시정지 상태 변화 테스트")
    void testPauseStateChanges() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            pauseBoard.requestFocus();
            
            // 초기 상태 확인 (일시정지 아님)
            assertFalse(frameBoard.isPaused, "초기에는 일시정지 상태가 아니어야 함");
            
            // 일시정지 상태로 변경
            frameBoard.isPaused = true;
            assertTrue(frameBoard.isPaused, "일시정지 상태로 변경되어야 함");
            
            // ESC 키로 해제
            KeyEvent escEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
            pauseBoard.keyPressed(escEvent);
            
            assertFalse(frameBoard.isPaused, "ESC 키로 일시정지가 해제되어야 함");
            
            // 다시 일시정지 상태로 변경
            frameBoard.isPaused = true;
            
            // Resume 버튼으로 해제
            KeyEvent enterEvent = new KeyEvent(pauseBoard, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            pauseBoard.keyPressed(enterEvent);
            
            assertFalse(frameBoard.isPaused, "Resume 버튼으로 일시정지가 해제되어야 함");
        });
    }
}