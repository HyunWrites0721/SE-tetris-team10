package game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import java.awt.event.KeyEvent;
import java.awt.Component;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameKeyListener 테스트")
public class GameKeyListenerTest {

    private GameKeyListener gameKeyListener;
    private FrameBoard frameBoard;
    private GameView gameBoard;
    private GameModel gameModel;
    private GameTimer gameTimer;
    private Component testComponent;

    @BeforeEach
    void setUp() throws Exception {
        // EDT에서 실제 컴포넌트들 생성
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new FrameBoard(false); // Normal mode
            frameBoard.setVisible(false); // 테스트 중에는 화면에 표시하지 않음
            gameBoard = frameBoard.getGameBoard();
            gameModel = frameBoard.getGameModel();
            gameTimer = frameBoard.getGameTimer();
            
            // GameKeyListener 생성
            gameKeyListener = new GameKeyListener(frameBoard, gameBoard, gameModel, gameTimer);
            
            // 테스트용 컴포넌트 (KeyEvent 생성용)
            testComponent = new JPanel();
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
    @DisplayName("ESC 키 - 일시정지 토글 테스트")
    void testEscapeKeyPause() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 초기 상태 확인 (일시정지 아님)
            assertFalse(frameBoard.isPaused, "초기 상태는 일시정지가 아니어야 함");
            
            // ESC 키 이벤트 생성 및 처리
            KeyEvent escEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, 
                                           System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, '\0');
            gameKeyListener.keyPressed(escEvent);
            
            // 일시정지 상태로 변경되었는지 확인
            assertTrue(frameBoard.isPaused, "ESC 키 후 일시정지 상태여야 함");
            
            // 다시 ESC 키 누르면 일시정지 해제
            gameKeyListener.keyPressed(escEvent);
            assertFalse(frameBoard.isPaused, "두 번째 ESC 키 후 일시정지가 해제되어야 함");
        });
    }

    @Test
    @DisplayName("숫자 키 1,2,3 - 스케일 변경 테스트")
    void testScaleKeys() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 1 키 - 0.5 스케일
            KeyEvent key1Event = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_1, '1');
            gameKeyListener.keyPressed(key1Event);
            assertEquals(0.5, gameBoard.scale, 0.01, "1 키 후 스케일이 0.5여야 함");
            
            // 2 키 - 1.0 스케일
            KeyEvent key2Event = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_2, '2');
            gameKeyListener.keyPressed(key2Event);
            assertEquals(1.0, gameBoard.scale, 0.01, "2 키 후 스케일이 1.0이어야 함");
            
            // 3 키 - 1.5 스케일
            KeyEvent key3Event = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_3, '3');
            gameKeyListener.keyPressed(key3Event);
            assertEquals(1.5, gameBoard.scale, 0.01, "3 키 후 스케일이 1.5여야 함");
        });
    }

    @Test
    @DisplayName("UP/W 키 - 블록 회전 테스트 (Arrow 모드)")
    void testRotateKeysArrowMode() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 게임이 일시정지 상태가 아닌지 확인
            frameBoard.isPaused = false;
            
            // UP 키로 블록 회전 (Arrow 모드에서 유효한 키)
            KeyEvent upEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                          System.currentTimeMillis(), 0, KeyEvent.VK_UP, '\0');
            
            // 회전 기능이 예외 없이 실행되는지 확인
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(upEvent);
            }, "UP 키로 블록 회전이 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("방향키 - 블록 이동 테스트")
    void testMovementKeys() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 게임이 일시정지 상태가 아닌지 확인
            frameBoard.isPaused = false;
            
            // LEFT 키 테스트
            KeyEvent leftEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, '\0');
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(leftEvent);
            }, "LEFT 키로 블록 이동이 예외 없이 실행되어야 함");
            
            // RIGHT 키 테스트
            KeyEvent rightEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                             System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, '\0');
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(rightEvent);
            }, "RIGHT 키로 블록 이동이 예외 없이 실행되어야 함");
            
            // DOWN 키 테스트
            KeyEvent downEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, '\0');
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(downEvent);
            }, "DOWN 키로 블록 이동이 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("SPACE 키 - 하드드롭 테스트")
    void testSpaceKeyHardDrop() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 게임이 일시정지 상태가 아닌지 확인
            frameBoard.isPaused = false;
            
            // SPACE 키로 하드드롭
            KeyEvent spaceEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                             System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ');
            
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(spaceEvent);
            }, "SPACE 키로 하드드롭이 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("일시정지 상태에서 이동 키 무시 테스트")
    void testKeysIgnoredWhenPaused() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 일시정지 상태로 설정
            frameBoard.isPaused = true;
            
            // 일시정지 상태에서 이동 키들이 무시되는지 확인
            KeyEvent leftEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, '\0');
            KeyEvent rightEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                             System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, '\0');
            KeyEvent downEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, '\0');
            KeyEvent spaceEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                             System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ');
            
            // 예외 없이 실행되지만 실제로는 무시되어야 함
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(leftEvent);
                gameKeyListener.keyPressed(rightEvent);
                gameKeyListener.keyPressed(downEvent);
                gameKeyListener.keyPressed(spaceEvent);
            }, "일시정지 상태에서 이동 키들이 예외 없이 무시되어야 함");
        });
    }

    @Test
    @DisplayName("WASD 키 - 블록 조작 테스트 (WASD 모드)")
    void testWASDKeys() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 게임이 일시정지 상태가 아닌지 확인
            frameBoard.isPaused = false;
            
            // controlType을 wasd로 변경 후 테스트하기 위해 reloadControlType 호출
            // 단, 설정 파일이 없을 수 있으므로 예외 처리
            try {
                gameKeyListener.reloadControlType();
            } catch (Exception e) {
                // 설정 로드 실패 시 무시 (기본값 arrow 사용)
            }
            
            // W 키 (회전) - WASD 모드에서만 유효
            KeyEvent wEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                         System.currentTimeMillis(), 0, KeyEvent.VK_W, 'w');
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(wEvent);
            }, "W 키 처리가 예외 없이 실행되어야 함");
            
            // A 키 (왼쪽 이동) - WASD 모드에서만 유효
            KeyEvent aEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                         System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a');
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(aEvent);
            }, "A 키 처리가 예외 없이 실행되어야 함");
            
            // S 키 (아래 이동) - WASD 모드에서만 유효
            KeyEvent sEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                         System.currentTimeMillis(), 0, KeyEvent.VK_S, 's');
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(sEvent);
            }, "S 키 처리가 예외 없이 실행되어야 함");
            
            // D 키 (오른쪽 이동) - WASD 모드에서만 유효
            KeyEvent dEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                         System.currentTimeMillis(), 0, KeyEvent.VK_D, 'd');
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(dEvent);
            }, "D 키 처리가 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("무효한 키 입력 테스트")
    void testInvalidKeys() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 정의되지 않은 키들이 예외 없이 무시되는지 확인
            KeyEvent[] invalidEvents = {
                new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_Q, 'q'),
                new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_E, 'e'),
                new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_R, 'r'),
                new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_T, 't'),
                new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F1, '\0')
            };
            
            for (KeyEvent event : invalidEvents) {
                assertDoesNotThrow(() -> {
                    gameKeyListener.keyPressed(event);
                }, "무효한 키 " + event.getKeyCode() + "가 예외 없이 무시되어야 함");
            }
        });
    }

    @Test
    @DisplayName("controlType 재로드 테스트")
    void testControlTypeReload() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // controlType 재로드가 예외 없이 실행되는지 확인
            assertDoesNotThrow(() -> {
                gameKeyListener.reloadControlType();
            }, "controlType 재로드가 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("게임 상태별 키 처리 테스트")
    void testKeyHandlingByGameState() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 정상 게임 상태
            frameBoard.isPaused = false;
            frameBoard.isGameOver = false;
            
            KeyEvent leftEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, '\0');
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(leftEvent);
            }, "정상 게임 상태에서 키 처리가 예외 없이 실행되어야 함");
            
            // 게임오버 상태에서도 ESC는 작동해야 함
            frameBoard.isGameOver = true;
            KeyEvent escEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                           System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, '\0');
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(escEvent);
            }, "게임오버 상태에서도 ESC 키는 처리되어야 함");
        });
    }

    @Test
    @DisplayName("연속 키 입력 테스트")
    void testContinuousKeyPress() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frameBoard.isPaused = false;
            
            // 연속으로 같은 키를 여러 번 누르기
            KeyEvent leftEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, '\0');
            
            // 연속 키 입력이 예외 없이 처리되는지 확인
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 5; i++) {
                    gameKeyListener.keyPressed(leftEvent);
                }
            }, "연속 키 입력이 예외 없이 처리되어야 함");
        });
    }

    @Test
    @DisplayName("스케일 변경 후 키 입력 테스트")
    void testKeyInputAfterScaleChange() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 스케일 변경
            KeyEvent key3Event = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_3, '3');
            gameKeyListener.keyPressed(key3Event);
            
            // 스케일 변경 후에도 블록 조작이 정상 작동하는지 확인
            frameBoard.isPaused = false;
            KeyEvent leftEvent = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, '\0');
            
            assertDoesNotThrow(() -> {
                gameKeyListener.keyPressed(leftEvent);
            }, "스케일 변경 후에도 블록 조작이 예외 없이 실행되어야 함");
        });
    }
}