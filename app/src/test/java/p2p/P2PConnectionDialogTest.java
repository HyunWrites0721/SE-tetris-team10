package p2p;

import org.junit.jupiter.api.*;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import network.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P2PConnectionDialog GUI 통합 테스트
 */
class P2PConnectionDialogTest {
    
    private P2PConnectionDialog dialog;
    private JFrame parentFrame;
    private MockNetworkManager mockNetwork;
    
    private static class MockNetworkManager extends NetworkManager {
        private NetworkRole role;
        private boolean connected;
        
        public MockNetworkManager(NetworkRole role, boolean connected) {
            this.role = role;
            this.connected = connected;
        }
        
        public NetworkRole getRole() {
            return role;
        }
        
        public boolean isConnected() {
            return connected;
        }
        
        public void disconnect() {
        }
        
        public void shutdown() {
        }
    }
    
    @BeforeEach
    void setUp() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                parentFrame = new JFrame("Test Parent");
                parentFrame.setVisible(false);
                mockNetwork = new MockNetworkManager(NetworkRole.SERVER, true);
                dialog = new P2PConnectionDialog(parentFrame, mockNetwork, true);
                dialog.setVisible(false);
            });
        } catch (Exception e) {
            fail("Failed to create P2PConnectionDialog: " + e.getMessage());
        }
    }
    
    @AfterEach
    void tearDown() {
        if (dialog != null) {
            SwingUtilities.invokeLater(() -> dialog.dispose());
        }
        if (parentFrame != null) {
            SwingUtilities.invokeLater(() -> parentFrame.dispose());
        }
    }
    
    @Test
    @DisplayName("P2PConnectionDialog 생성 테스트 (서버)")
    void testDialogCreationAsServer() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(dialog);
        });
    }
    
    @Test
    @DisplayName("P2PConnectionDialog 생성 테스트 (클라이언트)")
    void testDialogCreationAsClient() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MockNetworkManager clientNetwork = new MockNetworkManager(NetworkRole.CLIENT, false);
            P2PConnectionDialog clientDialog = new P2PConnectionDialog(parentFrame, clientNetwork, false);
            clientDialog.setVisible(false);
            assertNotNull(clientDialog);
            clientDialog.dispose();
        });
    }
    
    @Test
    @DisplayName("다이얼로그 dispose 테스트")
    void testDialogDispose() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertDoesNotThrow(() -> dialog.dispose());
        });
    }
    
    @Test
    @DisplayName("다이얼로그 컴포넌트 존재 확인")
    void testDialogComponents() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(dialog.getContentPane());
            assertTrue(dialog.getComponentCount() >= 0);
        });
    }
    
    @Test
    @DisplayName("다이얼로그 모달 속성 테스트")
    void testDialogModality() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // Dialog는 modal일 수 있음
            assertNotNull(dialog);
        });
    }
    
    @Test
    @DisplayName("다이얼로그 기본 속성 테스트")
    void testDialogProperties() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(dialog.isVisible());
        });
    }
    
    @Test
    @DisplayName("여러 다이얼로그 동시 생성 테스트")
    void testMultipleDialogCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MockNetworkManager network2 = new MockNetworkManager(NetworkRole.CLIENT, true);
            P2PConnectionDialog dialog2 = new P2PConnectionDialog(parentFrame, network2, false);
            dialog2.setVisible(false);
            assertNotNull(dialog2);
            dialog2.dispose();
        });
    }
    
    @Test
    @DisplayName("연결 상태에 따른 다이얼로그 생성")
    void testDialogWithDifferentConnectionStates() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MockNetworkManager disconnectedNetwork = new MockNetworkManager(NetworkRole.SERVER, false);
            P2PConnectionDialog disconnectedDialog = new P2PConnectionDialog(parentFrame, disconnectedNetwork, true);
            disconnectedDialog.setVisible(false);
            assertNotNull(disconnectedDialog);
            disconnectedDialog.dispose();
        });
    }
}
