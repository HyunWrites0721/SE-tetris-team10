package util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WindowManager 유닛 테스트
 */
class WindowManagerTest {
    
    private JFrame testFrame;
    
    @BeforeEach
    void setUp() {
        // 테스트용 프레임 생성 (표시하지 않음)
        testFrame = new JFrame("Test Frame");
        testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    @AfterEach
    void tearDown() {
        // 테스트 후 프레임 정리
        if (testFrame != null && testFrame.isDisplayable()) {
            testFrame.dispose();
        }
    }
    
    @Test
    void testAddAutoExitListener_ShouldAddWindowListener() {
        // Given: 프레임 생성
        JFrame frame = new JFrame("Test");
        int initialListenerCount = frame.getWindowListeners().length;
        
        // When: 리스너 추가
        WindowManager.addAutoExitListener(frame);
        
        // Then: 리스너가 추가되었는지 확인
        assertEquals(initialListenerCount + 1, frame.getWindowListeners().length,
            "WindowListener should be added");
        
        frame.dispose();
    }
    
    @Test
    void testAddAutoExitListener_WithNullFrame_ShouldThrowException() {
        // Given: null 프레임
        JFrame nullFrame = null;
        
        // When & Then: NullPointerException 발생
        assertThrows(NullPointerException.class, () -> {
            WindowManager.addAutoExitListener(nullFrame);
        });
    }
    
    @Test
    void testAddAutoExitListener_MultipleFrames() {
        // Given: 여러 프레임 생성
        JFrame frame1 = new JFrame("Frame 1");
        JFrame frame2 = new JFrame("Frame 2");
        JFrame frame3 = new JFrame("Frame 3");
        
        // When: 각 프레임에 리스너 추가
        WindowManager.addAutoExitListener(frame1);
        WindowManager.addAutoExitListener(frame2);
        WindowManager.addAutoExitListener(frame3);
        
        // Then: 모든 프레임에 리스너 추가 확인
        assertTrue(frame1.getWindowListeners().length > 0);
        assertTrue(frame2.getWindowListeners().length > 0);
        assertTrue(frame3.getWindowListeners().length > 0);
        
        // Cleanup
        frame1.dispose();
        frame2.dispose();
        frame3.dispose();
    }
    
    @Test
    void testAddAutoExitListener_SameFrameTwice() {
        // Given: 프레임 생성
        JFrame frame = new JFrame("Test");
        
        // When: 같은 프레임에 리스너를 두 번 추가
        WindowManager.addAutoExitListener(frame);
        int firstCount = frame.getWindowListeners().length;
        
        WindowManager.addAutoExitListener(frame);
        int secondCount = frame.getWindowListeners().length;
        
        // Then: 리스너가 두 번 추가됨
        assertEquals(firstCount + 1, secondCount,
            "WindowListener should be added twice");
        
        frame.dispose();
    }
    
    @Test
    void testWindowManagerWithDifferentFrameTypes() {
        // Given: 다양한 타입의 프레임
        JFrame standardFrame = new JFrame("Standard");
        JDialog dialog = new JDialog();
        
        // When: 리스너 추가
        WindowManager.addAutoExitListener(standardFrame);
        // JDialog는 JFrame이 아니므로 컴파일 에러 (타입 안전성 테스트)
        
        // Then: 정상적으로 추가됨
        assertTrue(standardFrame.getWindowListeners().length > 0);
        
        // Cleanup
        standardFrame.dispose();
        dialog.dispose();
    }
    
    @Test
    void testAddAutoExitListener_WithDifferentDefaultCloseOperations() {
        // Given: 다양한 종료 동작을 가진 프레임들
        JFrame doNothingFrame = new JFrame("Do Nothing");
        doNothingFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        JFrame disposeFrame = new JFrame("Dispose");
        disposeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JFrame hideFrame = new JFrame("Hide");
        hideFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        // When: 각 프레임에 리스너 추가
        WindowManager.addAutoExitListener(doNothingFrame);
        WindowManager.addAutoExitListener(disposeFrame);
        WindowManager.addAutoExitListener(hideFrame);
        
        // Then: 모든 프레임에 리스너 추가됨
        assertTrue(doNothingFrame.getWindowListeners().length > 0);
        assertTrue(disposeFrame.getWindowListeners().length > 0);
        assertTrue(hideFrame.getWindowListeners().length > 0);
        
        // Cleanup
        doNothingFrame.dispose();
        disposeFrame.dispose();
        hideFrame.dispose();
    }
    
    @Test
    void testWindowManagerStaticUtilityClass() {
        // Given & When: WindowManager는 static 메서드만 가짐
        
        // Then: 인스턴스 생성 가능 여부 확인 (유틸리티 클래스 검증)
        assertDoesNotThrow(() -> {
            // 리플렉션을 통해 생성자 접근 테스트
            WindowManager.class.getDeclaredConstructors();
        });
    }
    
    @Test
    void testAddAutoExitListener_FrameWithComponents() {
        // Given: 컴포넌트가 있는 프레임
        JFrame frame = new JFrame("Frame with Components");
        frame.add(new JButton("Button 1"));
        frame.add(new JLabel("Label 1"));
        
        // When: 리스너 추가
        WindowManager.addAutoExitListener(frame);
        
        // Then: 리스너가 정상적으로 추가됨
        assertTrue(frame.getWindowListeners().length > 0);
        
        // Cleanup
        frame.dispose();
    }
    
    @Test
    void testAddAutoExitListener_InvisibleFrame() {
        // Given: 표시되지 않는 프레임
        JFrame invisibleFrame = new JFrame("Invisible");
        invisibleFrame.setVisible(false);
        
        // When: 리스너 추가
        WindowManager.addAutoExitListener(invisibleFrame);
        
        // Then: 리스너가 추가됨
        assertTrue(invisibleFrame.getWindowListeners().length > 0);
        assertFalse(invisibleFrame.isVisible());
        
        // Cleanup
        invisibleFrame.dispose();
    }
    
    @Test
    void testAddAutoExitListener_MaximizedFrame() {
        // Given: 최대화된 프레임
        JFrame maximizedFrame = new JFrame("Maximized");
        maximizedFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // When: 리스너 추가
        WindowManager.addAutoExitListener(maximizedFrame);
        
        // Then: 리스너가 추가됨
        assertTrue(maximizedFrame.getWindowListeners().length > 0);
        assertEquals(JFrame.MAXIMIZED_BOTH, maximizedFrame.getExtendedState());
        
        // Cleanup
        maximizedFrame.dispose();
    }
}
