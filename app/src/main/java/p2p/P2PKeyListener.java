package p2p;

import game.core.GameController;
import network.NetworkManager;
import network.messages.GameControlMessage;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * P2P 네트워크 대전용 키 리스너
 * 로컬 플레이어의 키 입력만 처리합니다 (원격 플레이어 입력은 네트워크로 수신)
 */
public class P2PKeyListener extends KeyAdapter {
    
    private final GameController myController;
    private final NetworkManager networkManager;
    private final P2PVersusFrameBoard frameBoard;
    
    /**
     * P2PKeyListener 생성자
     * 
     * @param myController 내 게임 컨트롤러
     * @param networkManager 네트워크 관리자
     * @param frameBoard P2P 프레임보드
     */
    public P2PKeyListener(GameController myController, NetworkManager networkManager, P2PVersusFrameBoard frameBoard) {
        this.myController = myController;
        this.networkManager = networkManager;
        this.frameBoard = frameBoard;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        // Debug: log raw key presses to help trace whether input reaches listener
        try {
            System.out.println("[DEBUG] Key pressed: code=" + e.getKeyCode() + ", char=" + e.getKeyChar());
            // Also print controller / eventBus identity to verify instances
            try {
                Object eb = null;
                try {
                    eb = myController.getEventBus();
                } catch (Throwable __) {
                    // ignore if getter not available or throws
                }
                System.out.println("[DEBUG P2PKeyListener] controllerId=" + System.identityHashCode(myController)
                    + ", controllerEventBusId=" + (eb != null ? System.identityHashCode(eb) : "null"));
            } catch (Throwable __) {
                // ignore
            }
        } catch (Throwable t) {
            // ignore
        }
        // 일시정지 상태면 ESC/P만 처리
        if (myController.isPaused()) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_P) {
                // 일시정지 해제 및 상대방에게 알림
                frameBoard.handleResumeRequest();
            }
            return;
        }
        
        // 키 입력 처리
        switch (e.getKeyCode()) {
            // 왼쪽 이동
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                myController.moveLeft();
                break;
            
            // 오른쪽 이동
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                myController.moveRight();
                break;
            
            // 아래로 이동 (소프트 드롭)
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                myController.moveDown();
                break;
            
            // 회전
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                myController.rotate();
                break;
            
            // 하드 드롭 (즉시 착지)
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_F:
                myController.hardDrop();
                break;
            
            // 일시정지
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_P:
                // 일시정지 및 상대방에게 알림
                frameBoard.handlePauseRequest();
                break;
        }
    }
}
