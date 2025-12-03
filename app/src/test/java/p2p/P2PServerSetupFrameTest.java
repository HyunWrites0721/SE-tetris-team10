package p2p;

import org.junit.jupiter.api.*;
import javax.swing.SwingUtilities;
import network.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P2PServerSetupFrame GUI 통합 테스트
 */
class P2PServerSetupFrameTest {
    
    private P2PServerSetupFrame serverFrame;
    
    @BeforeEach
    void setUp() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                serverFrame = new P2PServerSetupFrame();
                serverFrame.setVisible(false);
            });
        } catch (Exception e) {
            fail("Failed to create P2PServerSetupFrame: " + e.getMessage());
        }
    }
    
    @AfterEach
    void tearDown() {
        if (serverFrame != null) {
            SwingUtilities.invokeLater(() -> serverFrame.dispose());
        }
    }
    
    @Test
    @DisplayName("P2PServerSetupFrame 생성 테스트")
    void testFrameCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(serverFrame);
            assertEquals("서버 열기", serverFrame.getTitle());
        });
    }
    
    @Test
    @DisplayName("프레임 크기 테스트")
    void testFrameSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(serverFrame.getWidth() > 0);
            assertTrue(serverFrame.getHeight() > 0);
        });
    }
    
    @Test
    @DisplayName("프레임 dispose 테스트")
    void testFrameDispose() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertDoesNotThrow(() -> serverFrame.dispose());
        });
    }
    
    @Test
    @DisplayName("여러 서버 프레임 동시 생성 테스트")
    void testMultipleFrameCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            P2PServerSetupFrame frame2 = new P2PServerSetupFrame();
            frame2.setVisible(false);
            assertNotNull(frame2);
            frame2.dispose();
        });
    }
    
    @Test
    @DisplayName("프레임 컴포넌트 존재 확인")
    void testFrameComponents() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(serverFrame.getContentPane());
            assertTrue(serverFrame.getComponentCount() > 0);
        });
    }
    
    @Test
    @DisplayName("프레임 기본 속성 테스트")
    void testFrameProperties() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(serverFrame.getTitle());
            assertFalse(serverFrame.isVisible());
        });
    }
    
    @Test
    @DisplayName("프레임 재사용 테스트")
    void testFrameReusability() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            serverFrame.setVisible(false);
            serverFrame.setVisible(false);
            assertDoesNotThrow(() -> serverFrame.dispose());
        });
    }
    
    @Test
    @DisplayName("프레임 Layout 테스트")
    void testFrameLayout() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(serverFrame.getLayout());
        });
    }
    
    @Test
    @DisplayName("WindowAdapter 동작 테스트")
    void testWindowAdapter() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(serverFrame.getWindowListeners().length > 0);
        });
    }
}
