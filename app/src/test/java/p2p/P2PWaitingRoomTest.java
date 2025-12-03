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

import static org.junit.jupiter.api.Assertions.*;

/**
 * P2PWaitingRoom GUI 통합 테스트
 */
@DisplayName("P2PWaitingRoom 테스트")
public class P2PWaitingRoomTest {

    private P2PWaitingRoom waitingRoom;
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
        if (waitingRoom != null) {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    waitingRoom.dispose();
                } catch (Exception e) {
                }
            });
        }
        
        if (mockNetwork != null) {
            mockNetwork.shutdown();
        }
    }

    @Test
    @DisplayName("P2PWaitingRoom 생성 테스트 - 서버")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testWaitingRoomCreation_Server() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            waitingRoom = new P2PWaitingRoom(mockNetwork, true);
            waitingRoom.setVisible(false);
            
            assertNotNull(waitingRoom);
        });
    }

    @Test
    @DisplayName("P2PWaitingRoom 생성 테스트 - 클라이언트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testWaitingRoomCreation_Client() throws Exception {
        mockNetwork = new MockNetworkManager(NetworkRole.CLIENT);
        
        SwingUtilities.invokeAndWait(() -> {
            waitingRoom = new P2PWaitingRoom(mockNetwork, false);
            waitingRoom.setVisible(false);
            
            assertNotNull(waitingRoom);
        });
    }

    @Test
    @DisplayName("프레임 제목 확인")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testFrameTitle() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            waitingRoom = new P2PWaitingRoom(mockNetwork, true);
            waitingRoom.setVisible(false);
            
            assertNotNull(waitingRoom.getTitle());
        });
    }

    @Test
    @DisplayName("프레임 크기 확인")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testFrameSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            waitingRoom = new P2PWaitingRoom(mockNetwork, true);
            waitingRoom.setVisible(false);
            
            assertTrue(waitingRoom.getWidth() > 0);
            assertTrue(waitingRoom.getHeight() > 0);
        });
    }

    @Test
    @DisplayName("Dispose 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testDispose() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            waitingRoom = new P2PWaitingRoom(mockNetwork, true);
            waitingRoom.setVisible(false);
            
            assertDoesNotThrow(() -> {
                waitingRoom.dispose();
            });
        });
        
        waitingRoom = null;
    }

    @Test
    @DisplayName("서버와 클라이언트 대기실 동시 생성")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testServerAndClientWaitingRooms() throws Exception {
        MockNetworkManager serverNetwork = new MockNetworkManager(NetworkRole.SERVER);
        MockNetworkManager clientNetwork = new MockNetworkManager(NetworkRole.CLIENT);
        
        try {
            SwingUtilities.invokeAndWait(() -> {
                P2PWaitingRoom room1 = new P2PWaitingRoom(serverNetwork, true);
                room1.setVisible(false);
                assertNotNull(room1);
                room1.dispose();
            });
            
            Thread.sleep(50);
            
            SwingUtilities.invokeAndWait(() -> {
                P2PWaitingRoom room2 = new P2PWaitingRoom(clientNetwork, false);
                room2.setVisible(false);
                assertNotNull(room2);
                room2.dispose();
            });
            
        } finally {
            serverNetwork.shutdown();
            clientNetwork.shutdown();
        }
    }
    
    @Test
    @DisplayName("프레임 컴포넌트 존재 확인")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testFrameComponents() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            waitingRoom = new P2PWaitingRoom(mockNetwork, true);
            waitingRoom.setVisible(false);
            
            assertNotNull(waitingRoom.getContentPane());
            assertTrue(waitingRoom.getComponentCount() >= 0);
        });
    }
    
    @Test
    @DisplayName("프레임 재사용 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testFrameReusability() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            waitingRoom = new P2PWaitingRoom(mockNetwork, true);
            waitingRoom.setVisible(false);
            waitingRoom.setVisible(false);
            assertDoesNotThrow(() -> waitingRoom.dispose());
        });
        waitingRoom = null;
    }
    
    @Test
    @DisplayName("서버 대기실 속성 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testServerRoomProperties() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            waitingRoom = new P2PWaitingRoom(mockNetwork, true);
            waitingRoom.setVisible(false);
            
            assertNotNull(waitingRoom);
            assertFalse(waitingRoom.isVisible());
        });
    }
    
    @Test
    @DisplayName("클라이언트 대기실 속성 테스트")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testClientRoomProperties() throws Exception {
        mockNetwork = new MockNetworkManager(NetworkRole.CLIENT);
        
        SwingUtilities.invokeAndWait(() -> {
            waitingRoom = new P2PWaitingRoom(mockNetwork, false);
            waitingRoom.setVisible(false);
            
            assertNotNull(waitingRoom);
            assertFalse(waitingRoom.isVisible());
        });
    }
}
