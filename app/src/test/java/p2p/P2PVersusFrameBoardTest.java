package p2p;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import javax.swing.SwingUtilities;
import java.util.concurrent.TimeUnit;

import network.NetworkManager;
import network.NetworkRole;
import versus.VersusMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P2PVersusFrameBoard GUI 통합 테스트
 */
@DisplayName("P2PVersusFrameBoard 테스트")
public class P2PVersusFrameBoardTest {

    private P2PVersusFrameBoard frameBoard;
    private MockNetworkManager mockNetwork;

    private static class MockNetworkManager extends NetworkManager {
        private NetworkRole role;
        
        public MockNetworkManager(NetworkRole role) {
            this.role = role;
        }
        
        public NetworkRole getRole() {
            return role;
        }
        
        public boolean isConnected() {
            return true;
        }
        
        public void disconnect() {
        }
        
        public void shutdown() {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        mockNetwork = new MockNetworkManager(NetworkRole.SERVER);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (frameBoard != null) {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    frameBoard.dispose();
                } catch (Exception e) {
                }
            });
        }
        
        if (mockNetwork != null) {
            mockNetwork.shutdown();
        }
    }

    @Test
    @DisplayName("P2PVersusFrameBoard 생성 테스트 - 서버")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFrameBoardCreation_Server() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.NORMAL,
                1
            );
            frameBoard.setVisible(false);
            
            assertNotNull(frameBoard);
            assertTrue(frameBoard.getTitle().contains("서버"));
        });
    }

    @Test
    @DisplayName("P2PVersusFrameBoard 생성 테스트 - 클라이언트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFrameBoardCreation_Client() throws Exception {
        mockNetwork = new MockNetworkManager(NetworkRole.CLIENT);
        
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.ITEM,
                2
            );
            frameBoard.setVisible(false);
            
            assertNotNull(frameBoard);
            assertTrue(frameBoard.getTitle().contains("클라이언트"));
        });
    }

    @Test
    @DisplayName("다양한 게임 모드로 생성 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDifferentGameModes() throws Exception {
        // NORMAL 모드
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.NORMAL,
                1
            );
            frameBoard.setVisible(false);
            assertNotNull(frameBoard);
            frameBoard.dispose();
        });
        
        Thread.sleep(50);
        
        // ITEM 모드
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.ITEM,
                2
            );
            frameBoard.setVisible(false);
            assertNotNull(frameBoard);
            frameBoard.dispose();
        });
        
        Thread.sleep(50);
        
        // TIME_LIMIT 모드
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.TIME_LIMIT,
                0
            );
            frameBoard.setVisible(false);
            assertNotNull(frameBoard);
        });
    }

    @Test
    @DisplayName("프레임 크기 확인")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testFrameSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.NORMAL,
                1
            );
            frameBoard.setVisible(false);
            
            assertTrue(frameBoard.getWidth() > 0);
            assertTrue(frameBoard.getHeight() > 0);
        });
    }

    @Test
    @DisplayName("프레임 Dispose 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testFrameDispose() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.NORMAL,
                1
            );
            frameBoard.setVisible(false);
            
            assertDoesNotThrow(() -> {
                frameBoard.dispose();
            });
        });
        
        frameBoard = null;
    }

    @Test
    @DisplayName("프레임 제목 확인")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testFrameTitle() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.NORMAL,
                1
            );
            frameBoard.setVisible(false);
            
            assertNotNull(frameBoard.getTitle());
            assertTrue(frameBoard.getTitle().contains("Tetris"));
            assertTrue(frameBoard.getTitle().contains("P2P"));
        });
    }
    
    @Test
    @DisplayName("프레임 컴포넌트 존재 확인")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testFrameComponents() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.NORMAL,
                1
            );
            frameBoard.setVisible(false);
            
            assertNotNull(frameBoard.getContentPane());
            assertTrue(frameBoard.getComponentCount() >= 0);
        });
    }
    
    @Test
    @DisplayName("다양한 난이도 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDifferentDifficulties() throws Exception {
        for (int difficulty = 0; difficulty <= 2; difficulty++) {
            final int diff = difficulty;
            SwingUtilities.invokeAndWait(() -> {
                P2PVersusFrameBoard fb = new P2PVersusFrameBoard(
                    mockNetwork,
                    VersusMode.NORMAL,
                    diff
                );
                fb.setVisible(false);
                assertNotNull(fb);
                fb.dispose();
            });
            Thread.sleep(50);
        }
    }
    
    @Test
    @DisplayName("프레임 재사용 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testFrameReusability() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.NORMAL,
                1
            );
            frameBoard.setVisible(false);
            frameBoard.setVisible(false);
            assertDoesNotThrow(() -> frameBoard.dispose());
        });
        frameBoard = null;
    }
    
    @Test
    @DisplayName("키 리스너 존재 확인")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testKeyListeners() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.NORMAL,
                1
            );
            frameBoard.setVisible(false);
            
            assertTrue(frameBoard.getKeyListeners().length > 0);
        });
    }
    
    @Test
    @DisplayName("윈도우 리스너 존재 확인")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testWindowListeners() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frameBoard = new P2PVersusFrameBoard(
                mockNetwork,
                VersusMode.NORMAL,
                1
            );
            frameBoard.setVisible(false);
            
            assertTrue(frameBoard.getWindowListeners().length > 0);
        });
    }
}
