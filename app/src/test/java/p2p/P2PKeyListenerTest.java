package p2p;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;

import game.GameView;
import game.core.GameController;
import network.NetworkManager;
import network.NetworkRole;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P2PKeyListener 통합 테스트
 */
@DisplayName("P2PKeyListener 테스트")
public class P2PKeyListenerTest {

    private P2PKeyListener keyListener;
    private MockGameController mockController;
    private MockGameView mockGameView;
    private MockNetworkManager mockNetwork;
    private MockFrameBoard mockFrameBoard;

    private static class MockGameView extends GameView {
        public MockGameView() {
            super(false);
        }
    }

    private static class MockGameController extends GameController {
        public MockGameController(GameView view) {
            super(view, false, 1);
        }
        
        @Override
        public void pause() {
        }
        
        @Override
        public void resume() {
        }
    }

    private static class MockNetworkManager extends NetworkManager {
        public NetworkRole getRole() {
            return NetworkRole.SERVER;
        }
        
        public boolean isConnected() {
            return true;
        }
        
        public void disconnect() {
        }
        
        public void shutdown() {
        }
    }

    private static class MockFrameBoard extends P2PVersusFrameBoard {
        public MockFrameBoard() {
            super(new MockNetworkManager(), versus.VersusMode.NORMAL, 1);
            setVisible(false);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            mockGameView = new MockGameView();
            mockController = new MockGameController(mockGameView);
            mockNetwork = new MockNetworkManager();
            mockFrameBoard = new MockFrameBoard();
            keyListener = new P2PKeyListener(mockController, mockNetwork, mockFrameBoard);
        });
    }

    @Test
    @DisplayName("P2PKeyListener 생성 테스트")
    void testKeyListenerCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(keyListener);
        });
    }

    @Test
    @DisplayName("키 입력 이벤트 처리 - 예외 없음")
    void testKeyPressedNoException() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            KeyEvent leftArrow = new KeyEvent(
                mockGameView,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_LEFT,
                KeyEvent.CHAR_UNDEFINED
            );
            
            assertDoesNotThrow(() -> {
                keyListener.keyPressed(leftArrow);
            });
        });
    }

    @Test
    @DisplayName("방향키 입력 테스트")
    void testArrowKeys() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            int[] arrowKeys = {
                KeyEvent.VK_LEFT,
                KeyEvent.VK_RIGHT,
                KeyEvent.VK_UP,
                KeyEvent.VK_DOWN
            };
            
            for (int keyCode : arrowKeys) {
                KeyEvent event = new KeyEvent(
                    mockGameView,
                    KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(),
                    0,
                    keyCode,
                    KeyEvent.CHAR_UNDEFINED
                );
                
                assertDoesNotThrow(() -> {
                    keyListener.keyPressed(event);
                });
            }
        });
    }

    @Test
    @DisplayName("스페이스바 입력 테스트")
    void testSpaceKey() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            KeyEvent spaceKey = new KeyEvent(
                mockGameView,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_SPACE,
                ' '
            );
            
            assertDoesNotThrow(() -> {
                keyListener.keyPressed(spaceKey);
            });
        });
    }

    @Test
    @DisplayName("ESC 키 입력 테스트")
    void testEscapeKey() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            KeyEvent escKey = new KeyEvent(
                mockGameView,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_ESCAPE,
                KeyEvent.CHAR_UNDEFINED
            );
            
            assertDoesNotThrow(() -> {
                keyListener.keyPressed(escKey);
            });
        });
    }

    @Test
    @DisplayName("keyReleased 이벤트 테스트")
    void testKeyReleased() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            KeyEvent releaseEvent = new KeyEvent(
                mockGameView,
                KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_LEFT,
                KeyEvent.CHAR_UNDEFINED
            );
            
            assertDoesNotThrow(() -> {
                keyListener.keyReleased(releaseEvent);
            });
        });
    }
}
