package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 창 관리 유틸리티
 * 모든 창이 닫히면 프로세스를 종료합니다.
 */
public class WindowManager {
    
    /**
     * 프레임에 자동 종료 리스너 추가
     * 모든 창이 닫히면 프로세스를 종료합니다.
     * 
     * @param frame 리스너를 추가할 프레임
     */
    public static void addAutoExitListener(JFrame frame) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                checkAndExitIfNoWindows();
            }
        });
    }
    
    /**
     * 열린 창이 없으면 프로세스 종료
     */
    private static void checkAndExitIfNoWindows() {
        // 모든 창 확인
        Window[] windows = Window.getWindows();
        
        int visibleWindowCount = 0;
        for (Window window : windows) {
            // JFrame이고 표시 가능한 창만 카운트
            if (window instanceof JFrame && window.isDisplayable()) {
                visibleWindowCount++;
            }
        }
        
        System.out.println("현재 열린 창 개수: " + visibleWindowCount);
        
        // 열린 창이 없으면 프로세스 종료
        if (visibleWindowCount == 0) {
            System.out.println("모든 창이 닫혔습니다. 프로세스를 종료합니다.");
            System.exit(0);
        }
    }
}
