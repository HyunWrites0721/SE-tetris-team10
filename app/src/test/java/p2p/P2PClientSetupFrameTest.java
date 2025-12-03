package p2p;

import org.junit.jupiter.api.*;
import javax.swing.SwingUtilities;
import network.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P2PClientSetupFrame GUI 통합 테스트
 */
class P2PClientSetupFrameTest {
    
    private P2PClientSetupFrame clientFrame;
    
    @BeforeEach
    void setUp() {
        // GUI 컴포넌트는 EDT에서 생성
        try {
            SwingUtilities.invokeAndWait(() -> {
                clientFrame = new P2PClientSetupFrame();
                clientFrame.setVisible(false); // 화면에 표시하지 않음
            });
        } catch (Exception e) {
            fail("Failed to create P2PClientSetupFrame: " + e.getMessage());
        }
    }
    
    @AfterEach
    void tearDown() {
        if (clientFrame != null) {
            SwingUtilities.invokeLater(() -> clientFrame.dispose());
        }
    }
    
    @Test
    @DisplayName("P2PClientSetupFrame 생성 테스트")
    void testFrameCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(clientFrame);
            assertEquals("서버에 연결", clientFrame.getTitle());
        });
    }
    
    @Test
    @DisplayName("프레임 크기 테스트")
    void testFrameSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(clientFrame.getWidth() > 0);
            assertTrue(clientFrame.getHeight() > 0);
        });
    }
    
    @Test
    @DisplayName("프레임 dispose 테스트")
    void testFrameDispose() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertDoesNotThrow(() -> clientFrame.dispose());
        });
    }
    
    @Test
    @DisplayName("여러 클라이언트 프레임 동시 생성 테스트")
    void testMultipleFrameCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            P2PClientSetupFrame frame2 = new P2PClientSetupFrame();
            frame2.setVisible(false);
            assertNotNull(frame2);
            frame2.dispose();
        });
    }
    
    @Test
    @DisplayName("프레임 컴포넌트 존재 확인")
    void testFrameComponents() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(clientFrame.getContentPane());
            assertTrue(clientFrame.getComponentCount() > 0);
        });
    }
    
    @Test
    @DisplayName("프레임 기본 속성 테스트")
    void testFrameProperties() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(clientFrame.getTitle());
            assertFalse(clientFrame.isVisible()); // setUp에서 false로 설정
        });
    }
    
    @Test
    @DisplayName("프레임 재사용 테스트")
    void testFrameReusability() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            clientFrame.setVisible(false);
            clientFrame.setVisible(false);
            assertDoesNotThrow(() -> clientFrame.dispose());
        });
    }
    
    @Test
    @DisplayName("프레임 Layout 테스트")
    void testFrameLayout() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(clientFrame.getLayout());
        });
    }
}
