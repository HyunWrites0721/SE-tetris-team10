package network;

import javax.swing.SwingUtilities;
import java.awt.Window;

/**
 * 연결 끊김 상황을 처리하는 핸들러 클래스
 * 연결이 끊어졌을 때 사용자에게 알림을 표시하고
 * 적절한 UI 상태로 복귀합니다.
 */
public class DisconnectionHandler {
    
    private final Runnable onDisconnect;
    private boolean isHandled = false;
    
    /**
     * DisconnectionHandler 생성자
     * @param onDisconnect 연결 끊김 시 실행할 콜백 (예: P2P 메뉴로 복귀)
     */
    public DisconnectionHandler(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }
    
    /**
     * 연결 끊김 처리
     * @param reason 끊김 이유 (예: "상대방과의 연결이 끊어졌습니다")
     * @param currentWindow 현재 창 (다이얼로그 표시용)
     */
    public void handleDisconnection(String reason, Window currentWindow) {
        // 중복 처리 방지
        if (isHandled) {
            return;
        }
        isHandled = true;
        
        System.out.println("❌ 연결 끊김 처리: " + reason);
        
        // UI 스레드에서 콜백만 실행 (다이얼로그는 P2PVersusFrameBoard에서 표시)
        SwingUtilities.invokeLater(() -> {
            // 콜백 실행 (P2PVersusFrameBoard.handleOpponentDisconnected)
            if (onDisconnect != null) {
                onDisconnect.run();
            }
        });
    }
    
    /**
     * 타임아웃으로 인한 연결 끊김 처리
     * @param currentWindow 현재 창
     */
    public void handleTimeout(Window currentWindow) {
        handleDisconnection(
            "상대방으로부터 응답이 없습니다.\n네트워크 연결을 확인해주세요.",
            currentWindow
        );
    }
    
    /**
     * 예외 발생으로 인한 연결 끊김 처리
     * @param exception 발생한 예외
     * @param currentWindow 현재 창
     */
    public void handleException(Exception exception, Window currentWindow) {
        String message = "네트워크 오류가 발생했습니다.";
        
        // 예외 메시지가 있으면 포함
        if (exception.getMessage() != null && !exception.getMessage().isEmpty()) {
            message += "\n(" + exception.getMessage() + ")";
        }
        
        exception.printStackTrace();
        handleDisconnection(message, currentWindow);
    }
    
    /**
     * 정상 종료 처리 (에러 메시지 없음)
     * @param currentWindow 현재 창
     */
    public void handleNormalDisconnection(Window currentWindow) {
        System.out.println("✓ 정상 연결 종료");
        
        SwingUtilities.invokeLater(() -> {
            // 창 닫기
            if (currentWindow != null) {
                currentWindow.dispose();
            }
            
            // 콜백 실행
            if (onDisconnect != null) {
                onDisconnect.run();
            }
        });
    }
    
    /**
     * 처리 여부 확인
     * @return 이미 처리되었으면 true
     */
    public boolean isHandled() {
        return isHandled;
    }
    
    /**
     * 핸들러 재사용을 위한 리셋
     */
    public void reset() {
        isHandled = false;
    }
}
