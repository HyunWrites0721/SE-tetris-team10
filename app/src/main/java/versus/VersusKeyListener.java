package versus;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import game.core.GameController;

/**
 * 대전 모드용 키 리스너
 * Player 1: WASD + F (하드드롭)
 * Player 2: UHJK + L (하드드롭)
 * ESC: 일시정지
 */
public class VersusKeyListener extends KeyAdapter {
    private VersusFrameBoard frameBoard;
    
    public VersusKeyListener(VersusFrameBoard frameBoard) {
        this.frameBoard = frameBoard;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // ESC - 일시정지
        if (keyCode == KeyEvent.VK_ESCAPE) {
            frameBoard.togglePause();
            return;
        }
        
        // 일시정지 또는 게임오버 상태면 게임 키 입력 무시
        if (frameBoard.isPaused() || frameBoard.isGameOver()) {
            return;
        }
        
        GameController gc1 = frameBoard.getGameController1();
        GameController gc2 = frameBoard.getGameController2();
        
        // Player 1 Controls: WASD + F
        switch (keyCode) {
            case KeyEvent.VK_A:  // 왼쪽 이동
                gc1.moveLeft();
                break;
            case KeyEvent.VK_D:  // 오른쪽 이동
                gc1.moveRight();
                break;
            case KeyEvent.VK_S:  // 소프트 드롭 (아래로 이동)
                gc1.moveDown();
                break;
            case KeyEvent.VK_W:  // 회전
                gc1.rotate();
                break;
            case KeyEvent.VK_F:  // 하드 드롭
                gc1.hardDrop();
                break;
        }
        
        // Player 2 Controls: IJKL + ;
        switch (keyCode) {
            case KeyEvent.VK_J:  // 왼쪽 이동
                gc2.moveLeft();
                break;
            case KeyEvent.VK_L:  // 오른쪽 이동
                gc2.moveRight();
                break;
            case KeyEvent.VK_K:  // 소프트 드롭 (아래로 이동)
                gc2.moveDown();
                break;
            case KeyEvent.VK_I:  // 회전
                gc2.rotate();
                break;
            case KeyEvent.VK_SEMICOLON:  // 하드 드롭
                gc2.hardDrop();
                break;
        }
    }
}
