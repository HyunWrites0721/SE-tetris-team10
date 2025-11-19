package game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Color;

import game.core.GameController;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameOverBoard 테스트")
public class GameOverBoardTest {

    private GameOverBoard gameOverBoard;
    private FrameBoard frameBoard;
    private GameController gameController;

    @BeforeEach
    void setUp() throws Exception {
        // EDT에서 컴포넌트들 생성
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new FrameBoard(false); // Normal mode
            frameBoard.setVisible(false); // 테스트 중에는 화면에 표시하지 않음
            gameController = frameBoard.getGameController();
            gameOverBoard = new GameOverBoard(frameBoard);
        });
        
        // UI 컴포넌트가 완전히 초기화될 때까지 잠시 대기
        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (frameBoard != null) {
            SwingUtilities.invokeAndWait(() -> {
                if (gameController != null && gameController.isRunning()) {
                    gameController.stop();
                }
                frameBoard.dispose();
            });
        }
    }

    @Test
    @DisplayName("GameOverBoard 생성 테스트")
    void testGameOverBoardCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(gameOverBoard, "GameOverBoard가 생성되어야 함");
            assertTrue(gameOverBoard.isFocusable(), "GameOverBoard는 포커스 가능해야 함");
            assertFalse(gameOverBoard.getFocusTraversalKeysEnabled(), "탭키 포커스 이동이 비활성화되어야 함");
        });
    }

    @Test
    @DisplayName("키 리스너 등록 테스트")
    void testKeyListenerRegistration() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 키 리스너가 등록되었는지 확인
            boolean hasKeyListener = false;
            for (var listener : gameOverBoard.getKeyListeners()) {
                if (listener == gameOverBoard) {
                    hasKeyListener = true;
                    break;
                }
            }
            assertTrue(hasKeyListener, "GameOverBoard가 자신을 키 리스너로 등록해야 함");
        });
    }

    @Test
    @DisplayName("버튼 초기화 테스트")
    void testButtonInitialization() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 버튼들이 올바르게 생성되었는지 확인
            Component[] components = gameOverBoard.getComponents();
            
            // GameOverBoard가 컴포넌트를 포함하고 있는지 확인
            assertTrue(components.length > 0, "GameOverBoard가 컴포넌트들을 포함해야 함");
        });
    }

    @Test
    @DisplayName("UP 키 - 버튼 선택 위로 이동 테스트")
    void testUpKeyButtonSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 포커스 설정
            gameOverBoard.requestFocusInWindow();
            
            // UP 키 이벤트 생성 및 처리
            KeyEvent upEvent = new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED,
                                          System.currentTimeMillis(), 0, KeyEvent.VK_UP, '\0');
            
            // UP 키 처리가 예외 없이 실행되는지 확인
            assertDoesNotThrow(() -> {
                gameOverBoard.keyPressed(upEvent);
            }, "UP 키 처리가 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("DOWN 키 - 버튼 선택 아래로 이동 테스트")
    void testDownKeyButtonSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 포커스 설정
            gameOverBoard.requestFocusInWindow();
            
            // DOWN 키 이벤트 생성 및 처리
            KeyEvent downEvent = new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, '\0');
            
            // DOWN 키 처리가 예외 없이 실행되는지 확인
            assertDoesNotThrow(() -> {
                gameOverBoard.keyPressed(downEvent);
            }, "DOWN 키 처리가 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("ENTER 키 - 버튼 클릭 테스트")
    void testEnterKeyButtonClick() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 포커스 설정
            gameOverBoard.requestFocusInWindow();
            
            // ENTER 키 이벤트 생성 및 처리
            KeyEvent enterEvent = new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED,
                                             System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n');
            
            // ENTER 키 처리가 예외 없이 실행되는지 확인
            assertDoesNotThrow(() -> {
                gameOverBoard.keyPressed(enterEvent);
            }, "ENTER 키 처리가 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("키 입력 무시 - keyTyped 테스트")
    void testKeyTypedIgnored() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // keyTyped 이벤트가 예외 없이 무시되는지 확인
            KeyEvent typedEvent = new KeyEvent(gameOverBoard, KeyEvent.KEY_TYPED,
                                             System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'a');
            
            assertDoesNotThrow(() -> {
                gameOverBoard.keyTyped(typedEvent);
            }, "keyTyped 이벤트가 예외 없이 무시되어야 함");
        });
    }

    @Test
    @DisplayName("키 입력 무시 - keyReleased 테스트")
    void testKeyReleasedIgnored() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // keyReleased 이벤트가 예외 없이 무시되는지 확인
            KeyEvent releasedEvent = new KeyEvent(gameOverBoard, KeyEvent.KEY_RELEASED,
                                                System.currentTimeMillis(), 0, KeyEvent.VK_UP, '\0');
            
            assertDoesNotThrow(() -> {
                gameOverBoard.keyReleased(releasedEvent);
            }, "keyReleased 이벤트가 예외 없이 무시되어야 함");
        });
    }

    @Test
    @DisplayName("스케일 변환 테스트")
    void testConvertScale() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            double testScale = 1.5;
            
            // 스케일 변환이 예외 없이 실행되는지 확인
            assertDoesNotThrow(() -> {
                gameOverBoard.convertScale(testScale);
            }, "스케일 변환이 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("연속 키 입력 테스트")
    void testContinuousKeyInput() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 포커스 설정
            gameOverBoard.requestFocusInWindow();
            
            // 연속으로 DOWN 키를 여러 번 누르기
            KeyEvent downEvent = new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, '\0');
            
            // 연속 키 입력이 예외 없이 처리되는지 확인
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 5; i++) {
                    gameOverBoard.keyPressed(downEvent);
                }
            }, "연속 키 입력이 예외 없이 처리되어야 함");
        });
    }

    @Test
    @DisplayName("UP/DOWN 키 순환 테스트")
    void testButtonSelectionCycle() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 포커스 설정
            gameOverBoard.requestFocusInWindow();
            
            KeyEvent upEvent = new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED,
                                          System.currentTimeMillis(), 0, KeyEvent.VK_UP, '\0');
            KeyEvent downEvent = new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, '\0');
            
            // UP/DOWN 키를 번갈아 누르면서 순환하는지 확인
            assertDoesNotThrow(() -> {
                // DOWN으로 아래로 이동
                gameOverBoard.keyPressed(downEvent);
                gameOverBoard.keyPressed(downEvent);
                gameOverBoard.keyPressed(downEvent); // 마지막 버튼에서 첫 번째로 순환
                
                // UP으로 위로 이동
                gameOverBoard.keyPressed(upEvent);
                gameOverBoard.keyPressed(upEvent);
                gameOverBoard.keyPressed(upEvent); // 첫 번째 버튼에서 마지막으로 순환
            }, "버튼 선택 순환이 예외 없이 처리되어야 함");
        });
    }

    @Test
    @DisplayName("무효한 키 입력 테스트")
    void testInvalidKeyInput() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 포커스 설정
            gameOverBoard.requestFocusInWindow();
            
            // 정의되지 않은 키들이 예외 없이 무시되는지 확인
            KeyEvent[] invalidEvents = {
                new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a'),
                new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' '),
                new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, '\0'),
                new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F1, '\0')
            };
            
            for (KeyEvent event : invalidEvents) {
                assertDoesNotThrow(() -> {
                    gameOverBoard.keyPressed(event);
                }, "무효한 키 " + event.getKeyCode() + "가 예외 없이 무시되어야 함");
            }
        });
    }

    @Test
    @DisplayName("스케일 변환 후 키 입력 테스트")
    void testKeyInputAfterScaleChange() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 스케일 변환
            gameOverBoard.convertScale(2.0);
            
            // 포커스 설정
            gameOverBoard.requestFocusInWindow();
            
            // 스케일 변환 후에도 키 입력이 정상 작동하는지 확인
            KeyEvent downEvent = new KeyEvent(gameOverBoard, KeyEvent.KEY_PRESSED,
                                            System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, '\0');
            
            assertDoesNotThrow(() -> {
                gameOverBoard.keyPressed(downEvent);
            }, "스케일 변환 후에도 키 입력이 예외 없이 실행되어야 함");
        });
    }

    @Test
    @DisplayName("GameOverBoard 가시성 테스트")
    void testVisibility() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 직접 생성된 GameOverBoard는 기본적으로 visible하므로 
            // 가시성 변경 기능만 테스트
            assertNotNull(gameOverBoard, "GameOverBoard가 생성되어야 함");
            
            // 가시성 변경 테스트
            gameOverBoard.setVisible(false);
            assertFalse(gameOverBoard.isVisible(), "setVisible(false) 후에는 보이지 않아야 함");
            
            gameOverBoard.setVisible(true);
            assertTrue(gameOverBoard.isVisible(), "setVisible(true) 후에는 보여야 함");
        });
    }

    @Test
    @DisplayName("포커스 관련 속성 테스트")
    void testFocusProperties() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 포커스 관련 속성들이 올바르게 설정되었는지 확인
            assertTrue(gameOverBoard.isFocusable(), "GameOverBoard는 포커스 가능해야 함");
            assertFalse(gameOverBoard.getFocusTraversalKeysEnabled(), "탭키 포커스 이동이 비활성화되어야 함");
            assertTrue(gameOverBoard.isOpaque(), "GameOverBoard는 불투명해야 함");
        });
    }

    @Test
    @DisplayName("배경색 테스트")
    void testBackgroundColor() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // 배경색이 검은색(불투명)으로 설정되었는지 확인
            Color backgroundColor = gameOverBoard.getBackground();
            assertNotNull(backgroundColor, "배경색이 설정되어야 함");
            
            // RGB 값이 검은색인지 확인 (0,0,0,255)
            assertEquals(0, backgroundColor.getRed(), "배경색의 Red 값이 0이어야 함");
            assertEquals(0, backgroundColor.getGreen(), "배경색의 Green 값이 0이어야 함");
            assertEquals(0, backgroundColor.getBlue(), "배경색의 Blue 값이 0이어야 함");
        });
    }
}