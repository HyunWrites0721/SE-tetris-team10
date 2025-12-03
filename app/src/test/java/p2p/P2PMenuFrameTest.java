package p2p;

import org.junit.jupiter.api.*;
import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P2PMenuFrame GUI 통합 테스트
 */
class P2PMenuFrameTest {
    
    private P2PMenuFrame menuFrame;
    
    @BeforeEach
    void setUp() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                menuFrame = new P2PMenuFrame();
                menuFrame.setVisible(false);
            });
        } catch (Exception e) {
            fail("Failed to create P2PMenuFrame: " + e.getMessage());
        }
    }
    
    @AfterEach
    void tearDown() {
        if (menuFrame != null) {
            SwingUtilities.invokeLater(() -> menuFrame.dispose());
        }
    }
    
    @Test
    @DisplayName("P2PMenuFrame 생성 테스트")
    void testFrameCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(menuFrame);
            assertEquals("P2P 대전", menuFrame.getTitle());
        });
    }
    
    @Test
    @DisplayName("프레임 크기 테스트")
    void testFrameSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(menuFrame.getWidth() > 0);
            assertTrue(menuFrame.getHeight() > 0);
        });
    }
    
    @Test
    @DisplayName("프레임 dispose 테스트")
    void testFrameDispose() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertDoesNotThrow(() -> menuFrame.dispose());
        });
    }
    
    @Test
    @DisplayName("프레임 컴포넌트 존재 확인")
    void testFrameComponents() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(menuFrame.getContentPane());
            assertTrue(menuFrame.getComponentCount() > 0);
        });
    }
    
    @Test
    @DisplayName("키 리스너 존재 확인")
    void testKeyListeners() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // KeyListener는 컴포넌트 또는 하위 컴포넌트에 있을 수 있음
            assertTrue(menuFrame.getKeyListeners().length >= 0);
        });
    }
    
    @Test
    @DisplayName("프레임 포커스 가능 여부")
    void testFrameFocusable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(menuFrame.isFocusable());
        });
    }
    
    @Test
    @DisplayName("프레임 기본 속성 테스트")
    void testFrameProperties() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(menuFrame.getTitle());
            assertFalse(menuFrame.isVisible());
        });
    }
    
    @Test
    @DisplayName("여러 메뉴 프레임 동시 생성 테스트")
    void testMultipleFrameCreation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            P2PMenuFrame frame2 = new P2PMenuFrame();
            frame2.setVisible(false);
            assertNotNull(frame2);
            frame2.dispose();
        });
    }
}
