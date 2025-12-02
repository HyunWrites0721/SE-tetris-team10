package p2p;

import javax.swing.*;
import network.NetworkManager;
import network.ConnectionState;

/**
 * P2P 연결 진행 상태 다이얼로그
 * 연결 완료 후 즉시 대기실로 이동
 * (UI는 표시하지 않고 백그라운드에서 상태만 모니터링)
 */
public class P2PConnectionDialog extends JDialog {
    private NetworkManager networkManager;
    private boolean isServer;
    private Timer statusCheckTimer;
    private Timer autoStartTimer;
    private volatile boolean isCancelled = false;  // 취소 플래그
    
    public P2PConnectionDialog(JFrame parent, NetworkManager networkManager, boolean isServer) {
        super(parent, "연결 완료", false);  // 모달 해제
        
        this.networkManager = networkManager;
        this.isServer = isServer;
        
        // 다이얼로그는 표시하지 않음 (백그라운드 모니터링만)
        setSize(0, 0);
        setUndecorated(true);
        setVisible(false);  // UI 표시 안 함
        
        // 연결 상태 모니터링 시작
        startStatusMonitoring();
        
        // 1초 후 자동으로 대기실 진입
        autoStartTimer = new Timer(1000, e -> {
            autoStartTimer.stop();
            enterWaitingRoom();
        });
        autoStartTimer.setRepeats(false);
        autoStartTimer.start();
    }
    
    /**
     * 연결 상태 모니터링 시작
     */
    private void startStatusMonitoring() {
        statusCheckTimer = new Timer(500, e -> {
            ConnectionState state = networkManager.getState();
            
            switch (state) {
                case TIMEOUT:
                case DISCONNECTED:
                    // 연결 끊김 처리 (중복 방지)
                    if (!isCancelled) {
                        statusCheckTimer.stop();
                        if (autoStartTimer != null) {
                            autoStartTimer.stop();
                        }
                        isCancelled = true;  // 플래그 먼저 설정
                        
                        // 다이얼로그 정리
                        dispose();
                        
                        // 연결 종료 알림만 표시 (대기실 진입 안 함!)
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                null,
                                "연결을 종료했습니다.",
                                "연결 종료",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        });
                    }
                    break;
                    
                default:
                    // 정상 상태는 무시
                    break;
            }
        });
        statusCheckTimer.start();
    }
    
    /**
     * 대기실 진입
     */
    private void enterWaitingRoom() {
        // 이미 취소된 경우 대기실로 진입하지 않음
        if (isCancelled) {
            return;
        }
        
        if (statusCheckTimer != null) {
            statusCheckTimer.stop();
        }
        
        // P2P 대기실로 이동
        dispose();
        if (getOwner() != null) {
            getOwner().dispose();
        }

        // 연결 후 즉시 대기실로 이동
        SwingUtilities.invokeLater(() -> {
            new P2PWaitingRoom(networkManager, isServer);
        });
    }
}
